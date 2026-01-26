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
import android.view.LayoutInflater
import android.webkit.WebView
import android.widget.Toast
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.FullScreenInteractions
import com.instructure.interactions.Identity
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAGE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getModuleItemId
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.loadHtmlWithIframes
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.databinding.FragmentPageDetailsBinding
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.PageDeletedEvent
import com.instructure.teacher.events.PageUpdatedEvent
import com.instructure.teacher.factory.PageDetailsPresenterFactory
import com.instructure.teacher.presenters.PageDetailsPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import com.instructure.teacher.viewinterface.PageDetailsView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@PageView
@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_PAGE_DETAILS)
class PageDetailsFragment : BasePresenterFragment<
        PageDetailsPresenter,
        PageDetailsView,
        FragmentPageDetailsBinding>(),
    PageDetailsView,
    Identity {

    private var canvasContext: CanvasContext by ParcelableArg(default = Course())
    private var page: Page by ParcelableArg(Page(), PAGE)
    private var pageId: String by StringArg(key = PAGE_ID)

    private var downloadUrl: String? = null
    var downloadFileName: String? = null

    private var loadHtmlJob: Job? = null

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    @PageViewUrl
    @Suppress("unused")
    fun makePageViewUrl(): String {
        val url = StringBuilder(ApiPrefs.fullDomain)
        page.let {
            url.append(canvasContext.toAPIString())
            if (!it.frontPage) url.append("/pages/${page.url}")
            getModuleItemId()?.let { url.append("?module_item_id=$it") }
        }
        return url.toString()
    }

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
        binding.loading.setGone()
    }

    override fun onRefreshStarted() {
        binding.loading.setVisible()
    }

    override fun onReadySetGo(presenter: PageDetailsPresenter): Unit = with(binding) {
        if (page.frontPage) {
            presenter.getFrontPage(canvasContext, true)
        } else if (!pageId.isBlank()) {
            presenter.getPage(pageId, canvasContext, true)
        } else {
            presenter.getPage(page.url ?: "", canvasContext, true)
        }
        setupToolbar()

        canvasWebViewWraper.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(activity, url)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                loading.setGone()
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                loading.setVisible()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }
        }

        canvasWebViewWraper.webView.addVideoClient(requireActivity())
        canvasWebViewWraper.webView.canvasWebChromeClientCallback = object : CanvasWebView.CanvasWebChromeClientCallback {
            override fun onProgressChangedCallback(view: WebView?, newProgress: Int) {
                if (newProgress >= 100) {
                    loading.setGone()
                }
            }
        }

        canvasWebViewWraper.webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), url, getString(R.string.utils_externalToolTitle), true))
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = !RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)
        }

        canvasWebViewWraper.webView.setMediaDownloadCallback(object : CanvasWebView.MediaDownloadCallback {
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
            if (it.id == page.id) {
                if (activity is MasterDetailInteractions) {
                    (activity as MasterDetailInteractions).popFragment(canvasContext)
                } else if (activity is FullScreenInteractions) {
                    requireActivity().finish()
                }
            }
        }
    }

    override fun getPresenterFactory() = PageDetailsPresenterFactory(canvasContext, page)
    override fun onPresenterPrepared(presenter: PageDetailsPresenter) = Unit

    override val bindingInflater: (LayoutInflater) -> FragmentPageDetailsBinding = FragmentPageDetailsBinding::inflate

    override val identity: Long? get() = page.id
    override val skipCheck: Boolean get() = false

    override fun populatePageDetails(page: Page) {
        this.page = page
        loadHtmlJob = binding.canvasWebViewWraper.webView.loadHtmlWithIframes(requireContext(), featureFlagProvider, page.body, {
            if (view != null) binding.canvasWebViewWraper.loadHtml(it, page.title, baseUrl = page.htmlUrl)
        }, onLtiButtonPressed = {
            RouteMatcher.route(requireActivity(), LtiLaunchFragment.makeSessionlessLtiUrlRoute(requireActivity(), canvasContext, it))
        }, courseId = canvasContext.id)
        setupToolbar()
    }

    override fun onError(stringId: Int) {
        Toast.makeText(requireContext(), stringId, Toast.LENGTH_SHORT).show()
        binding.loading.setGone()
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupMenu(R.menu.menu_page_details) { openEditPage(page) }

        toolbar.setupBackButtonWithExpandCollapseAndBack(this@PageDetailsFragment) {
            toolbar.updateToolbarExpandCollapseIcon(this@PageDetailsFragment)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext.color, requireContext().getColor(R.color.textLightest))
            (activity as MasterDetailInteractions).toggleExpandCollapse()
        }

        toolbar.title = page.title
        if (!isTablet) {
            toolbar.subtitle = canvasContext.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext.color, requireContext().getColor(R.color.textLightest))
    }

    private fun openEditPage(page: Page) {
        if (APIHelper.hasNetworkConnection()) {
            val args = CreateOrEditPageDetailsFragment.newInstanceEdit(canvasContext, page).nonNullArgs
            RouteMatcher.route(requireActivity(), Route(CreateOrEditPageDetailsFragment::class.java, canvasContext, args))
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    /**
     * Used for Studio video downloads
     */
    private fun downloadFile() {
        if (downloadFileName != null && downloadUrl != null) {
            WorkManager.getInstance(requireContext()).enqueue(FileDownloadWorker.createOneTimeWorkRequest(downloadFileName, downloadUrl!!))
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onPageUpdated(event: PageUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            page = it
        }
    }

    companion object {
        const val PAGE = "pageDetailsPage"

        const val PAGE_ID = "pageDetailsId"

        fun makeBundle(page: Page): Bundle = Bundle().apply { putParcelable(PAGE, page) }

        fun makeBundle(pageId: String): Bundle = Bundle().apply { putString(PAGE_ID, pageId) }


        fun newInstance(canvasContext: CanvasContext, args: Bundle) = PageDetailsFragment().withArgs(args).apply {
            this.canvasContext = canvasContext
        }
    }
}
