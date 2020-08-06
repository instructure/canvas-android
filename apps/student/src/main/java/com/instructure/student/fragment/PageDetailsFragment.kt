/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.fragment

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.BeforePageView
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteType
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.Const.PAGE
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.events.PageUpdatedEvent
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.LockInfoHTMLHelper
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern

@PageView
class PageDetailsFragment : InternalWebviewFragment(), Bookmarkable {

    private var fetchDataJob: WeaveJob? = null
    private var loadHtmlJob: Job? = null
    private var pageName: String? by NullableStringArg(key = PAGE_NAME)
    private var page: Page by ParcelableArg(default = Page(), key = PAGE)
    private var pageUrl: String? by NullableStringArg(key = PAGE_URL)

    // Flag for the webview client to know whether or not we should clear the history
    private var isUpdated = false

    @PageViewUrl
    @Suppress("unused")
    private fun makePageViewUrl(): String {
        val url = StringBuilder(ApiPrefs.fullDomain)
        page.let {
            url.append(canvasContext.toAPIString())
            if (!it.frontPage) url.append("/pages/${pageUrl ?: page.url ?: pageName}")
            getModuleItemId()?.let { url.append("?module_item_id=$it") }
        }
        return url.toString()
    }

    override fun title(): String = pageName ?: page.title ?: getString(R.string.pages)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShouldLoadUrl(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getPageDetails()
    }

    override fun onPause() {
        super.onPause()
        canvasWebView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fetchDataJob?.cancel()
        loadHtmlJob?.cancel()
        canvasWebView?.destroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCanvasWebView()?.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            override fun launchInternalWebViewFragment(url: String) = InternalWebviewFragment.loadInternalWebView(activity, InternalWebviewFragment.makeRoute(canvasContext, url, isLTITool))
        }

        // Add to the webview client for clearing webview history after an update to prevent going back to old data
        val callback = getCanvasWebView()?.canvasWebViewClientCallback
        callback?.let {
            getCanvasWebView()?.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback by it {
                override fun onPageFinishedCallback(webView: WebView?, url: String?) {
                    it.onPageFinishedCallback(webView, url)
                    // Only clear history after an update
                    if (isUpdated) getCanvasWebView()?.clearHistory()
                }

                override fun openMediaFromWebView(mime: String?, url: String?, filename: String?) {
                    RouteMatcher.openMedia(activity, url)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> {
                openEditPage(page)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getPageDetails() {
        if (page.id != 0L) {
            if (page.body != null) {
                if (pageName == null) {
                    // If we don't set page name, we have problems when trying to set up the bookmark.
                    // pageName is null when we call the bookmark property below.
                    pageName = page.title
                }
                loadPage(page)
            } else if (!page.title.isNullOrBlank()) {
                pageName = page.title
                fetchPageDetails()
            } else {
                loadFailedPageInfo(null)
            }
        } else if (pageName == null || pageName == Page.FRONT_PAGE_NAME) fetchFontPage()
        else fetchPageDetails()
    }

    private fun fetchFontPage() {
        fetchDataJob = tryWeave {
            val response = awaitApiResponse<Page> { PageManager.getFrontPage(canvasContext, true, it) }
            response.body()?.let {
                nonNullArgs.putParcelable(PAGE, it)
                loadPage(it)
            }
            if (response.body() == null) loadFailedPageInfo(response)
        } catch {
            Logger.e("Page Fetch Error ${it.message}")
            loadFailedPageInfo()
        }
    }

    private fun fetchPageDetails() {
        fetchDataJob = tryWeave {
            val pageUrl = pageUrl ?: page.url ?: pageName ?: throw Exception("Page url/name null!")
            val response = awaitApiResponse<Page> { PageManager.getPageDetails(canvasContext, pageUrl, true, it) }
            response.body()?.let {
                nonNullArgs.putParcelable(PAGE, it)
                loadPage(it)
            }
            if (response.body() == null) loadFailedPageInfo(response)
        } catch {
            Logger.e("Page Fetch Error ${it.message}")
            loadFailedPageInfo()
        }
    }

    private fun loadPage(page: Page) {
        setPageObject(page)

        if (page.lockInfo != null) {
            val lockedMessage = LockInfoHTMLHelper.getLockedInfoHTML(page.lockInfo, activity, R.string.lockedPageDesc)
            populateWebView(lockedMessage, getString(R.string.pages))
            return
        }

        if (page.body != null && page.body != "null" && page.body != "") {
            // Add RTL support
            if (canvasWebView.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                page.body = "<body dir=\"rtl\">${page.body}</body>"
            }

            // Some pages need to know the course ID, so we set it on window.ENV.COURSE.id (See MBL-14324)
            val body = """<script>window.ENV = { COURSE: { id: "${canvasContext.id}" } };</script>""" + page.body.orEmpty()

            // Load the html with the helper function to handle iframe cases
            loadHtmlJob = canvasWebView.loadHtmlWithIframes(requireContext(), isTablet, body, ::loadPageHtml, {
                val args = LTIWebViewFragment.makeLTIBundle(
                        URLDecoder.decode(it, "utf-8"), getString(R.string.utils_externalToolTitle), true)
                RouteMatcher.route(requireContext(), Route(LTIWebViewFragment::class.java, canvasContext, args))
            }, page.title)
        } else if (page.body == null || page.body?.endsWith("") == true) {
            loadHtml(resources.getString(R.string.noPageFound), "text/html", "utf-8", null)
        }

        toolbar.title = title()

        checkCanEdit()
    }

    private fun loadPageHtml(html: String, contentDescrption: String?) {
        canvasWebView.loadHtml(html, contentDescrption)
    }

    /**
     * DO NOT REMOVE
     * This is a special case specific to some school districts and how they are handling iframes
     * It will check for an ID in the iframe, and if found, will authenticate the source
     */
    private suspend fun addAuthForIframeIfNecessary(htmlContent: String): String {
        var newHtml = htmlContent
        val iframeMatcher = Pattern.compile("<iframe(.|\\n)*?iframe>").matcher(htmlContent)

        var sourceUrl: String?
        while (iframeMatcher.find()) {
            val iframe = iframeMatcher.group(0)
            val matcher = Pattern.compile("id=\"cnvs_content\"").matcher(iframe)
            // Confirm the id
            if (matcher.find()) {
                val srcMatcher = Pattern.compile("src=\"([^\"]+)\"").matcher(iframe)
                if (srcMatcher.find()) {
                    sourceUrl = srcMatcher.group(1)
                    val authenticatedUrl = awaitApi<AuthenticatedSession> { OAuthManager.getAuthenticatedSession(sourceUrl, it) }.sessionUrl
                    val newIframe = iframe.replace(sourceUrl, authenticatedUrl)

                    newHtml = newHtml.replace(iframe, newIframe)
                }
            }
        }
        return newHtml
    }

    private fun loadFailedPageInfo(response: Response<Page>? = null) {
        if (response != null && response.code() >= 400 && response.code() < 500 && pageName != null && pageName == Page.FRONT_PAGE_NAME) {

            var context: String = if (canvasContext.type == CanvasContext.Type.COURSE) {
                getString(R.string.course)
            } else {
                getString(R.string.group)
            }

            // We want a complete sentence.
            context += "."

            // We want it to be lowercase.
            context = context.toLowerCase(Locale.getDefault())

            loadHtml(resources.getString(R.string.noPagesInContext) + " " + context, "text/html", "utf-8", null)
        } else {
            loadHtml(resources.getString(R.string.noPageFound), "text/html", "utf-8", null)
        }
    }

    override fun applyTheme() {
        toolbar?.let {
            setupToolbarMenu(it, R.menu.menu_page_details)
            it.title = title()
            it.setupAsBackButton(this)
            // Set the edit option false by default
            it.menu.findItem(R.id.menu_edit).isVisible = false
            checkCanEdit()

            ViewStyler.themeToolbar(requireActivity(), it, canvasContext)
        }
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext).withParam(RouterParams.PAGE_ID, if (Page.FRONT_PAGE_NAME == pageName) Page.FRONT_PAGE_NAME else pageName!!)

    private fun openEditPage(page: Page) {
        if (APIHelper.hasNetworkConnection()) {
            val route = EditPageDetailsFragment.makeRoute(canvasContext, page).apply { routeType = RouteType.DIALOG }
            RouteMatcher.route(requireContext(), route)
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    private fun checkCanEdit() {
        if (page.editingRoles?.contains("public") == true) {
            toolbar?.menu?.findItem(R.id.menu_edit)?.isVisible = true
        } else if (page.editingRoles?.contains("student") == true && (canvasContext as? Course)?.isStudent == true) {
            toolbar?.menu?.findItem(R.id.menu_edit)?.isVisible = true
        } else if (page.editingRoles?.contains("teacher") == true && (canvasContext as? Course)?.isTeacher == true) {
            toolbar?.menu?.findItem(R.id.menu_edit)?.isVisible = true
        }
    }

    @BeforePageView
    private fun setPageObject(page: Page) {
        this.page = page
    }

    @Suppress("unused")
    @Subscribe
    fun onUpdatePage(event: PageUpdatedEvent) {
        event.once(page.id.toString()) {
            isUpdated = true
            page = Page(title = page.title) // Reset to empty page (except for title) so getPageDetails() will pull from the network
            getPageDetails()
        }
    }

    companion object {
        const val PAGE_NAME = "pageDetailsName"
        const val PAGE = "pageDetails"
        const val PAGE_URL = "pageUrl"

        @JvmStatic
        fun newInstance(route: Route): PageDetailsFragment? {
            return if (validRoute(route)) PageDetailsFragment().apply {
                arguments = route.arguments
                with(nonNullArgs) {
                    if (containsKey(PAGE_NAME)) pageName = getString(PAGE_NAME)
                    if (route.paramsHash.containsKey(RouterParams.PAGE_ID)) pageName = route.paramsHash[RouterParams.PAGE_ID]
                }
            } else null
        }

        @JvmStatic
        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null &&
                    (route.arguments.containsKey(PAGE) ||
                            route.arguments.containsKey(PAGE_NAME) ||
                            route.paramsHash.containsKey(RouterParams.PAGE_ID))
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, pageName: String?): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { if (pageName != null) putString(PAGE_NAME, pageName) }))
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, pageName: String?, pageUrl: String?): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                if (pageName != null)
                    putString(PAGE_NAME, pageName)
                if (pageUrl != null)
                    putString(PAGE_URL, pageUrl)
            }))
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, page: Page): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { putParcelable(PAGE, page) }))
        }
    }
}
