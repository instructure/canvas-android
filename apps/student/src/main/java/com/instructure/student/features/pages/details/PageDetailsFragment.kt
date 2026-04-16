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
package com.instructure.student.features.pages.details

import android.os.Bundle
import android.text.Html as AndroidHtml
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAGE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.getModuleItemId
import com.instructure.pandautils.utils.loadHtmlWithIframes
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withRequireNetwork
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.events.PageUpdatedEvent
import com.instructure.student.fragment.EditPageDetailsFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.LockInfoHTMLHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.Subscribe
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_PAGE_DETAILS)
@PageView
@AndroidEntryPoint
class PageDetailsFragment : InternalWebviewFragment(), Bookmarkable {

    @Inject
    lateinit var repository: PageDetailsRepository

    @Inject
    lateinit var webViewRouter: WebViewRouter

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    @Inject
    lateinit var fileDownloader: FileDownloader

    private var loadHtmlJob: Job? = null
    private var pageName: String? by NullableStringArg(key = PAGE_NAME)
    private var page: Page by ParcelableArg(default = Page(), key = PAGE)
    private var pageUrl: String? by NullableStringArg(key = PAGE_URL)
    private var navigatedFromModules: Boolean by BooleanArg(key = NAVIGATED_FROM_MODULES)
    private var frontPage: Boolean by BooleanArg(key = FRONT_PAGE)

    // Flag for the webview client to know whether or not we should clear the history
    private var isUpdated = false

    @PageViewUrl
    @Suppress("unused")
    fun makePageViewUrl(): String {
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
        retainInstance = false
        setShouldLoadUrl(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getPageDetails()
    }

    override fun onPause() {
        super.onPause()
        binding.canvasWebViewWrapper.webView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCanvasWebView()?.onExplainTextSelected = { selectedText -> explainText(selectedText) }
        getCanvasWebView()?.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            override fun launchInternalWebViewFragment(url: String) = loadInternalWebView(activity, makeRoute(canvasContext, url, isLTITool))
        }

        // Add to the webview client for clearing webview history after an update to prevent going back to old data
        val callback = getCanvasWebView()?.canvasWebViewClientCallback
        callback?.let {
            getCanvasWebView()?.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback by it {
                override fun onPageFinishedCallback(webView: WebView, url: String) {
                    it.onPageFinishedCallback(webView, url)
                    // Only clear history after an update
                    if (isUpdated) getCanvasWebView()?.clearHistory()
                }

                override fun openMediaFromWebView(mime: String, url: String, filename: String) = webViewRouter.openMedia(url)

                override fun canRouteInternallyDelegate(url: String) = webViewRouter.canRouteInternally(url)

                override fun routeInternallyCallback(url: String) = webViewRouter.routeInternally(url)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> activity?.withRequireNetwork { openEditPage(page) }
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
        } else if (frontPage) fetchFrontPage()
        else fetchPageDetails()
    }

    private fun fetchFrontPage() {
        lifecycleScope.tryLaunch {
            val result = repository.getFrontPage(canvasContext, true)
            result.onSuccess {
                nonNullArgs.putParcelable(PAGE, it)
                loadPage(it)
            }.onFailure {
                loadFailedPageInfo((it as? Failure.Network)?.errorCode)
            }
        } catch {
            Logger.e("Page Fetch Error ${it.message}")
            loadFailedPageInfo()
        }
    }

    private fun fetchPageDetails() {
        lifecycleScope.tryLaunch {
            val pageUrl = pageUrl ?: page.url ?: pageName ?: throw Exception("Page url/name null!")
            val result = repository.getPageDetails(canvasContext, pageUrl, true)
            result.onSuccess {
                nonNullArgs.putParcelable(PAGE, it)
                loadPage(it)
            }.onFailure {
                loadFailedPageInfo((it as? Failure.Network)?.errorCode)
            }
        } catch {
            Logger.e("Page Fetch Error ${it.message}")
            loadFailedPageInfo()
        }
    }

    private fun loadPage(page: Page) = with(binding) {
        setPageObject(page)

        page.lockInfo?.let {
            val lockedMessage = LockInfoHTMLHelper.getLockedInfoHTML(it, requireContext(), R.string.lockedPageDesc, !navigatedFromModules)
            populateWebView(lockedMessage, getString(R.string.pages))
            return
        }

        if (page.body != null && page.body != "null" && page.body != "") {
            // Add RTL support
            if (canvasWebViewWrapper.webView.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                page.body = "<body dir=\"rtl\">${page.body}</body>"
            }

            // Some pages need to know the course ID, so we set it on window.ENV.COURSE.id (See MBL-14324)
            val body = """<script>window.ENV = { COURSE: { id: "${canvasContext.id}" } };</script>""" + page.body.orEmpty()

            // Load the html with the helper function to handle iframe cases
            loadHtmlJob = canvasWebViewWrapper.webView.loadHtmlWithIframes(
                requireContext(),
                featureFlagProvider,
                body,
                {
                    canvasWebViewWrapper.loadHtml(it, page.title, baseUrl = page.htmlUrl)
                },
                courseId = canvasContext.id,
                onLtiButtonPressed = {
                    RouteMatcher.route(
                        requireActivity(),
                        LtiLaunchFragment.makeSessionlessLtiUrlRoute(
                            requireActivity(),
                            canvasContext,
                            it
                        )
                    )
                })
        } else if (page.body == null || page.body?.endsWith("") == true) {
            populateWebView(resources.getString(R.string.noPageFound), getString(R.string.pages))
        }

        toolbar.title = title()

        checkCanEdit()
    }

    private fun explainText(selectedText: String) {
        val findingText = getString(com.instructure.pandares.R.string.findingRelevantPages)
        val bottomSheet = ExplainBottomSheet().apply {
            this.selectedText = selectedText
            this.statusText = findingText
        }
        bottomSheet.show(childFragmentManager, ExplainBottomSheet.TAG)

        lifecycleScope.tryLaunch {
            val generativeModel = Generation.getClient()
            if (generativeModel.checkStatus() != FeatureStatus.AVAILABLE) {
                bottomSheet.isLoading = false
                bottomSheet.explanationText = getString(com.instructure.pandares.R.string.aiUnavailable)
                return@tryLaunch
            }

            // Step 1: Fetch page titles and ask which are relevant
            val params = RestParams(isForceReadFromNetwork = true)
            val pagesApi = RestBuilder().build(PageAPI.PagesInterface::class.java, params)
            val pagesResult = pagesApi.getFirstPagePages(canvasContext.id, "courses", params)
            val allPages = pagesResult.dataOrNull.orEmpty()

            if (allPages.isEmpty()) {
                bottomSheet.statusText = getString(com.instructure.pandares.R.string.generatingExplanation)
                generativeModel.generateContentStream(
                    generateContentRequest(TextPart("Explain in simple terms: \"$selectedText\"")) {
                        temperature = 0.3f
                        maxOutputTokens = 256
                    }
                ).collect { chunk ->
                    val token = chunk.candidates.firstOrNull()?.text ?: ""
                    bottomSheet.explanationText += token
                }
                bottomSheet.isLoading = false
                return@tryLaunch
            }

            val titlesPrompt = buildString {
                append("Which of these course pages are most relevant to explain: \"$selectedText\"?\n\nPages:\n")
                allPages.forEachIndexed { i, p -> append("${i + 1}. ${p.title}\n") }
                append("\nReturn only the numbers of the 1-2 most relevant pages, comma-separated.")
            }

            val pickResponse = generativeModel.generateContent(
                generateContentRequest(TextPart(titlesPrompt)) {
                    temperature = 0.1f
                    maxOutputTokens = 20
                }
            )
            val pickText = pickResponse.candidates.firstOrNull()?.text ?: ""
            Log.d("ExplainFeature", "Pick response: $pickText")

            val indices = pickText.replace(Regex("[^0-9,]"), "")
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..allPages.size }
                .map { it - 1 }
                .take(2)

            // Step 2: Fetch relevant page bodies and explain
            bottomSheet.statusText = getString(com.instructure.pandares.R.string.generatingExplanation)

            val contextParts = buildString {
                for (idx in indices) {
                    val p = allPages[idx]
                    val detailResult = pagesApi.getDetailedPage("courses", canvasContext.id, p.url ?: continue, params)
                    val body = detailResult.dataOrNull?.body ?: continue
                    val plainText = AndroidHtml.fromHtml(body, AndroidHtml.FROM_HTML_MODE_COMPACT).toString().trim()
                    append("--- ${p.title} ---\n${plainText.take(1000)}\n\n")
                }
            }

            val explainPrompt = if (contextParts.isNotBlank()) {
                "Explain in simple terms: \"$selectedText\"\n\nContext from course pages:\n$contextParts"
            } else {
                "Explain in simple terms: \"$selectedText\""
            }

            generativeModel.generateContentStream(
                generateContentRequest(TextPart(explainPrompt)) {
                    temperature = 0.3f
                    maxOutputTokens = 256
                }
            ).collect { chunk ->
                val token = chunk.candidates.firstOrNull()?.text ?: ""
                bottomSheet.explanationText += token
            }
            bottomSheet.isLoading = false
        } catch {
            Log.e("ExplainFeature", "Explain failed", it)
            bottomSheet.isLoading = false
            bottomSheet.explanationText = "Something went wrong. Please try again."
        }
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

    private fun loadFailedPageInfo(errorCode: Int? = null) {
        if (errorCode != null && errorCode >= 400 && errorCode < 500 && pageName != null && pageName == Page.FRONT_PAGE_NAME) {

            var context: String = if (canvasContext.type == CanvasContext.Type.COURSE) {
                getString(R.string.course)
            } else {
                getString(R.string.group)
            }

            // We want a complete sentence.
            context += "."

            // We want it to be lowercase.
            context = context.lowercase(Locale.getDefault())

            populateWebView(resources.getString(R.string.noPagesInContext) + " " + context, getString(R.string.pages))
        } else {
            populateWebView(resources.getString(R.string.noPageFound), getString(R.string.pages))
        }
    }

    override fun applyTheme() {
        binding.toolbar.applyTopSystemBarInsets()
        binding.canvasWebViewWrapper.applyBottomSystemBarInsets()
        binding.toolbar.let {
            setupToolbarMenu(it, R.menu.menu_page_details)
            it.title = title()
            it.setupAsBackButton(this)
            // Set the edit option false by default
            it.menu.findItem(R.id.menu_edit).isVisible = false
            checkCanEdit()

            ViewStyler.themeToolbarColored(requireActivity(), it, canvasContext)
        }
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)
            .withParam(RouterParams.PAGE_ID, if (Page.FRONT_PAGE_NAME == pageName) Page.FRONT_PAGE_NAME else pageName!!)
            .withUrl(page.htmlUrl)

    private fun openEditPage(page: Page) {
        if (APIHelper.hasNetworkConnection()) {
            val route = EditPageDetailsFragment.makeRoute(canvasContext, page)
            RouteMatcher.route(requireActivity(), route)
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    private fun checkCanEdit() = with(binding) {
        val course = canvasContext as? Course
        val editingRoles = page.editingRoles.orEmpty()
        if (course?.isStudent == true) {
            if (page.lockInfo == null && (editingRoles.contains("public") || editingRoles.contains("student"))) {
                toolbar.menu?.findItem(R.id.menu_edit)?.isVisible = true
            }
        } else if (course?.isTeacher == true) {
            if ((editingRoles.contains("public") || editingRoles.contains("teacher"))) {
                toolbar.menu?.findItem(R.id.menu_edit)?.isVisible = true
            }
        }
    }

    private fun setPageObject(page: Page) {
        this.page = page
        completePageViewPrerequisite("pageSet")
    }

    override fun beforePageViewPrerequisites(): List<String> {
        return listOf("pageSet")
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

    override fun handleBackPressed() = false

    override fun downloadInternalMedia(mime: String?, url: String?, filename: String?) {
        fileDownloader.downloadFileToDevice(url, filename, mime)
    }

    companion object {
        const val PAGE_NAME = "pageDetailsName"
        const val PAGE = "pageDetails"
        const val PAGE_URL = "pageUrl"
        const val NAVIGATED_FROM_MODULES = "navigated_from_modules"
        private const val FRONT_PAGE = "frontPage"

        fun newInstance(route: Route): PageDetailsFragment? {
            return if (validRoute(route)) PageDetailsFragment().apply {
                arguments = route.arguments
                with(nonNullArgs) {
                    if (containsKey(PAGE_NAME)) pageName = getString(PAGE_NAME)
                    if (route.paramsHash.containsKey(RouterParams.PAGE_ID)) pageName = route.paramsHash[RouterParams.PAGE_ID]
                }
            } else null
        }

        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null &&
                    (route.arguments.containsKey(PAGE) ||
                            route.arguments.containsKey(PAGE_NAME) ||
                            route.paramsHash.containsKey(RouterParams.PAGE_ID))
        }

        fun makeFrontPageRoute(canvasContext: CanvasContext): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putBoolean(FRONT_PAGE, true)
            }))
        }

        fun makeRoute(canvasContext: CanvasContext, pageName: String?, pageUrl: String?, navigatedFromModules: Boolean): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putBoolean(NAVIGATED_FROM_MODULES, navigatedFromModules)
                if (pageName != null)
                    putString(PAGE_NAME, pageName)
                if (pageUrl != null)
                    putString(PAGE_URL, pageUrl)
            }))
        }

        fun makeRoute(canvasContext: CanvasContext, page: Page): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { putParcelable(PAGE, page) }))
        }
    }
}
