/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.teacher.fragments

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.FullScreenInteractions
import com.instructure.interactions.Identity
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAGE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.PageDeletedEvent
import com.instructure.teacher.events.PageUpdatedEvent
import com.instructure.teacher.factory.PageDetailsPresenterFactory
import com.instructure.teacher.presenters.PageDetailsPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.services.FileDownloadService
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import com.instructure.teacher.viewinterface.PageDetailsView
import kotlinx.android.synthetic.main.fragment_page_details.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URLDecoder

@ScreenView(SCREEN_VIEW_PAGE_DETAILS)
class PageDetailsFragment : BasePresenterFragment<
        PageDetailsPresenter,
        PageDetailsView>(),
        PageDetailsView, Identity {

    private var mCanvasContext: CanvasContext by ParcelableArg(default = Course())
    private var mPage: Page by ParcelableArg(Page(), PAGE)
    private var mPageId: String by StringArg(key = PAGE_ID)

    private var downloadUrl: String? = null
    var downloadFileName: String? = null

    private var loadHtmlJob: Job? = null

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onRefreshFinished() {
        loading.setGone()
    }

    override fun onRefreshStarted() {
        loading.setVisible()
    }

    override fun onReadySetGo(presenter: PageDetailsPresenter) {

        if (mPage.frontPage) {
            presenter.getFrontPage(mCanvasContext, true)
        } else if (!mPageId.isBlank()) {
            presenter.getPage(mPageId, mCanvasContext, true)
        } else {
            presenter.getPage(mPage.url ?: "", mCanvasContext, true)
        }
        setupToolbar()

        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(activity, url)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                loading?.setGone()
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                loading?.setVisible()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }
        }

        canvasWebView.webChromeClient = (object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress >= 100) {
                    loading?.setGone()
                }
            }
        })

        canvasWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), url, getString(R.string.utils_externalToolTitle), true))
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = !RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)
        }

        canvasWebView.setMediaDownloadCallback (object : CanvasWebView.MediaDownloadCallback{
            override fun downloadMedia(mime: String?, url: String?, filename: String?) {
                downloadUrl = url
                downloadFileName = filename

                if (PermissionUtils.hasPermissions(activity!!, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                    downloadFile()
                } else {
                    requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
                }
            }

        })

        EventBus.getDefault().getStickyEvent(PageDeletedEvent::class.java)?.once(javaClass.simpleName + ".onResume()") {
            if (it.id == mPage.id) {
                if (activity is MasterDetailInteractions) {
                    (activity as MasterDetailInteractions).popFragment(mCanvasContext)
                } else if (activity is FullScreenInteractions) {
                    requireActivity().finish()
                }
            }
        }
    }

    override fun getPresenterFactory() = PageDetailsPresenterFactory(mCanvasContext, mPage)
    override fun onPresenterPrepared(presenter: PageDetailsPresenter) = Unit

    override fun layoutResId() = R.layout.fragment_page_details

    override val identity: Long? get() = mPage.id
    override val skipCheck: Boolean get() = false

    override fun populatePageDetails(page: Page) {
        mPage = page
        loadHtmlJob = canvasWebView.loadHtmlWithIframes(requireContext(), isTablet, page.body.orEmpty(), ::loadPageHtml, {
            val args = LtiLaunchFragment.makeBundle(mCanvasContext, URLDecoder.decode(it, "utf-8"), getString(R.string.utils_externalToolTitle), true)
            RouteMatcher.route(requireContext(), Route(LtiLaunchFragment::class.java, canvasContext, args))
        }, page.title)
        setupToolbar()
    }

    private fun loadPageHtml(html: String, contentDescription: String?) {
        canvasWebView.loadHtml(html, contentDescription, baseUrl = mPage.htmlUrl)
    }

    override fun onError(stringId: Int) {
        Toast.makeText(requireContext(), stringId, Toast.LENGTH_SHORT).show()
    }

    private fun setupToolbar() {
        toolbar.setupMenu(R.menu.menu_page_details) { openEditPage(mPage) }

        toolbar.setupBackButtonWithExpandCollapseAndBack(this) {
            toolbar.updateToolbarExpandCollapseIcon(this)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, mCanvasContext.color, requireContext().getColor(R.color.white))
            (activity as MasterDetailInteractions).toggleExpandCollapse()
        }

        toolbar.title = mPage.title
        if (!isTablet) {
            toolbar.subtitle = mCanvasContext.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, mCanvasContext.color, requireContext().getColor(R.color.white))
    }

    private fun openEditPage(page: Page) {
        if (APIHelper.hasNetworkConnection()) {
            val args = CreateOrEditPageDetailsFragment.newInstanceEdit(mCanvasContext, page).nonNullArgs
            RouteMatcher.route(requireContext(), Route(CreateOrEditPageDetailsFragment::class.java, mCanvasContext, args))
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    /**
     * Used for Studio video downloads
     */
    private fun downloadFile() {
        if (downloadFileName != null && downloadUrl != null) {
            FileDownloadService.scheduleDownloadJob(requireContext(), downloadUrl!!, downloadFileName!!)
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onPageUpdated(event: PageUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            mPage = it
        }
    }

    companion object {
        const val PAGE = "pageDetailsPage"

        const val PAGE_ID = "pageDetailsId"

        fun makeBundle(page: Page): Bundle = Bundle().apply { putParcelable(PageDetailsFragment.PAGE, page) }

        fun makeBundle(pageId: String): Bundle = Bundle().apply { putString(PageDetailsFragment.PAGE_ID, pageId) }


        fun newInstance(canvasContext: CanvasContext, args: Bundle) = PageDetailsFragment().withArgs(args).apply {
            mCanvasContext = canvasContext
        }
    }
}
