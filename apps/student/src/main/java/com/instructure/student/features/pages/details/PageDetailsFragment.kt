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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAGE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.features.pages.details.PageDetailsNavigation
import com.instructure.pandautils.features.pages.details.PageDetailsViewModel
import com.instructure.pandautils.features.pages.details.composables.PageDetailsScreen
import com.instructure.pandautils.features.studytools.StudyToolsViewModel
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getModuleItemId
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.pandautils.utils.orDefault
import com.instructure.student.R
import com.instructure.student.events.PageUpdatedEvent
import com.instructure.student.fragment.EditPageDetailsFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_PAGE_DETAILS)
@PageView
@AndroidEntryPoint
class PageDetailsFragment : BaseCanvasFragment(), FragmentInteractions, Bookmarkable {

    @Inject
    lateinit var webViewRouter: WebViewRouter

    @Inject
    lateinit var fileDownloader: FileDownloader

    override val navigation: Navigation? = null

    var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.emptyUserContext(), key = Const.CANVAS_CONTEXT)

    private val viewModel: PageDetailsViewModel by viewModels()
    private val studyToolsViewModel: StudyToolsViewModel by viewModels()

    override val bookmark: Bookmarker by lazy { viewModel.bookmarker }

    override fun applyTheme() = Unit

    override fun getFragment(): Fragment = this

    override fun title(): String = viewModel.currentPageName ?: viewModel.currentPage.title ?: getString(R.string.pages)

    @PageViewUrl
    @Suppress("unused")
    fun makePageViewUrl(): String {
        val url = StringBuilder(ApiPrefs.fullDomain)
        val page = viewModel.currentPage
        url.append(canvasContext.toAPIString())
        if (!page.frontPage) url.append("/pages/${viewModel.pageUrl ?: page.url ?: viewModel.currentPageName}")
        getModuleItemId()?.let { url.append("?module_item_id=$it") }
        return url.toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val studyToolsUiState by studyToolsViewModel.uiState.collectAsState()

                LaunchedEffect(uiState.pageSlug) {
                    uiState.pageSlug?.let(studyToolsViewModel::onPageResolved)
                }

                LaunchedEffect(uiState.navigation) {
                    uiState.navigation?.let { navigation ->
                        handleNavigation(navigation)
                        uiState.onNavigationHandled()
                    }
                }

                LaunchedEffect(uiState.showNoInternetDialog) {
                    if (uiState.showNoInternetDialog) {
                        NoInternetConnectionDialog.show(parentFragmentManager)
                        uiState.onNoInternetDialogShown()
                    }
                }

                LaunchedEffect(uiState.pageSet) {
                    if (uiState.pageSet) {
                        completePageViewPrerequisite("pageSet")
                    }
                }

                val navigationHost = activity as? Navigation
                val bookmarkable = navigationHost?.canBookmark().orDefault() && viewModel.bookmarker.canBookmark
                val onAddBookmark: (() -> Unit)? = if (bookmarkable) {
                    {
                        if (APIHelper.hasNetworkConnection()) {
                            navigationHost?.addBookmark()
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    null
                }

                // Remembered so the callbacks object (and the lambdas inside it) keep a stable identity
                // across recompositions - PageDetailsScreen relies on that stability (remember(webViewCallbacks))
                // to avoid tearing down/rebuilding the WebView wrapper on every uiState change, e.g. the one
                // triggered by tapping a Notebook highlight (which now sets notebookTrayArgs).
                val webViewCallbacks = remember { descriptionWebViewCallbacks() }
                val embeddedWebViewCallbacks = remember { descriptionEmbeddedWebViewCallbacks() }

                PageDetailsScreen(
                    uiState = uiState,
                    toolbarColor = canvasContext.color,
                    navigationActionClick = { activity?.onBackPressed() },
                    webViewCallbacks = webViewCallbacks,
                    embeddedWebViewCallbacks = embeddedWebViewCallbacks,
                    studyToolsUiState = studyToolsUiState,
                    applyOnWebView = { addVideoClient(requireActivity()) },
                    onAddBookmark = onAddBookmark
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe
    fun onUpdatePage(event: PageUpdatedEvent) {
        event.once(viewModel.currentPage.id.toString()) {
            viewModel.onPageUpdated()
        }
    }

    private fun descriptionWebViewCallbacks() = ComposeWebViewCallbacks(
        onPageFinished = { webView, _ ->
            if (viewModel.uiState.value.clearWebViewHistory) {
                webView.clearHistory()
                viewModel.uiState.value.onWebViewHistoryCleared()
            }
        },
        openMedia = { _, url, _ -> webViewRouter.openMedia(url) },
        canRouteInternally = { url -> webViewRouter.canRouteInternally(url) },
        routeInternally = { url -> webViewRouter.routeInternally(url) },
        downloadInternalMedia = { mime, url, filename -> fileDownloader.downloadFileToDevice(url, filename, mime) }
    )

    private fun descriptionEmbeddedWebViewCallbacks() = ComposeEmbeddedWebViewCallbacks(
        shouldLaunchInternalWebViewFragment = { true },
        launchInternalWebViewFragment = { url -> webViewRouter.launchInternalWebViewFragment(url, canvasContext) }
    )

    private fun handleNavigation(navigation: PageDetailsNavigation) {
        when (navigation) {
            is PageDetailsNavigation.LtiScreen -> webViewRouter.openLtiScreen(canvasContext, navigation.url)
            is PageDetailsNavigation.EditPageScreen -> {
                RouteMatcher.route(requireActivity(), EditPageDetailsFragment.makeRoute(canvasContext, navigation.page))
            }
        }
    }

    override fun beforePageViewPrerequisites(): List<String> = listOf("pageSet")

    companion object {
        // Delegate to PageDetailsViewModel's constants (the single source of truth) rather than
        // duplicating the literals here - this Fragment's constants are a public API other Student
        // classes reference directly (e.g. CourseBrowserFragment.PAGE_NAME), so they can't be removed,
        // but they must never drift out of sync with what the ViewModel reads via SavedStateHandle.
        const val PAGE_NAME = PageDetailsViewModel.PAGE_NAME
        const val PAGE = PageDetailsViewModel.PAGE
        const val PAGE_URL = PageDetailsViewModel.PAGE_URL
        const val NAVIGATED_FROM_MODULES = PageDetailsViewModel.NAVIGATED_FROM_MODULES
        const val NOTE_ID = PageDetailsViewModel.NOTE_ID
        private const val FRONT_PAGE = PageDetailsViewModel.FRONT_PAGE

        fun newInstance(route: Route): PageDetailsFragment? {
            return if (validRoute(route)) PageDetailsFragment().apply {
                arguments = route.arguments
                with(nonNullArgs) {
                    if (route.paramsHash.containsKey(RouterParams.PAGE_ID)) {
                        putString(PAGE_NAME, route.paramsHash[RouterParams.PAGE_ID])
                    }
                }
            } else null
        }

        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null &&
                    (route.arguments.containsKey(PAGE) ||
                            route.arguments.containsKey(PAGE_NAME) ||
                            route.arguments.containsKey(PAGE_URL) ||
                            route.paramsHash.containsKey(RouterParams.PAGE_ID))
        }

        fun makeFrontPageRoute(canvasContext: CanvasContext): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putBoolean(FRONT_PAGE, true)
            }))
        }

        fun makeRoute(
            canvasContext: CanvasContext,
            pageName: String?,
            pageUrl: String?,
            navigatedFromModules: Boolean,
            noteId: String? = null
        ): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putBoolean(NAVIGATED_FROM_MODULES, navigatedFromModules)
                if (pageName != null)
                    putString(PAGE_NAME, pageName)
                if (pageUrl != null)
                    putString(PAGE_URL, pageUrl)
                if (noteId != null)
                    putString(NOTE_ID, noteId)
            }))
        }

        fun makeRoute(canvasContext: CanvasContext, page: Page): Route {
            return Route(null, PageDetailsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { putParcelable(PAGE, page) }))
        }
    }
}
