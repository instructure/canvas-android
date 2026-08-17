/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.pages.details

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.isRtl
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.notes.GetPageStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.pages.LoadFrontPageUseCase
import com.instructure.pandautils.domain.usecase.pages.LoadPageDetailsUseCase
import com.instructure.pandautils.features.notes.notebook.NotebookTrayArgs
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.LockInfoHTMLHelper
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PageDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val loadFrontPageUseCase: LoadFrontPageUseCase,
    private val loadPageDetailsUseCase: LoadPageDetailsUseCase,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val resources: Resources,
    private val featureFlagProvider: FeatureFlagProvider,
    private val getPageStudyNotesUseCase: GetPageStudyNotesUseCase,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : ViewModel() {

    val canvasContext: CanvasContext = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) ?: CanvasContext.emptyUserContext()

    // Nav args, mirroring the fields the legacy PageDetailsFragment read out of its own arguments Bundle.
    val pageUrl: String? = savedStateHandle.get<String>(PAGE_URL)
    private val navigatedFromModules: Boolean = savedStateHandle.get<Boolean>(NAVIGATED_FROM_MODULES).orDefault()
    private val frontPage: Boolean = savedStateHandle.get<Boolean>(FRONT_PAGE).orDefault()

    var currentPage: Page = savedStateHandle.get<Page>(PAGE) ?: Page()
        private set

    var currentPageName: String? = savedStateHandle.get<String>(PAGE_NAME)
        private set

    var bookmarker: Bookmarker = computeBookmarker()
        private set

    // Seeded with the title we already know from the nav args so the toolbar shows the page name
    // immediately instead of only once the fetch resolves (the legacy fragment did this in
    // applyTheme()). Falls back to "Pages" when we were routed without a name (e.g. front page).
    private val _uiState = MutableStateFlow(PageDetailsUiState(toolbarTitle = currentTitle()).withCallbacks())
    val uiState = _uiState.asStateFlow()

    init {
        getPageDetails()
    }

    private fun getPageDetails(isPageUpdate: Boolean = false) {
        val page = currentPage
        when {
            page.id != 0L && page.body != null -> {
                if (currentPageName == null) {
                    // If we don't set the page name, the bookmark computation has nothing to key off of.
                    currentPageName = page.title
                }
                renderPage(page, isPageUpdate)
            }

            page.id != 0L && !page.title.isNullOrBlank() -> {
                currentPageName = page.title
                fetchPageDetails(isPageUpdate)
            }

            page.id != 0L -> onFetchFailed(null)

            frontPage -> fetchFrontPage(isPageUpdate)

            else -> fetchPageDetails(isPageUpdate)
        }
    }

    private fun fetchFrontPage(isPageUpdate: Boolean) {
        viewModelScope.launch {
            try {
                loadFrontPageUseCase(LoadFrontPageUseCase.Params(canvasContext, forceNetwork = true))
                    .onSuccess {
                        currentPage = it
                        renderPage(it, isPageUpdate)
                    }
                    .onFailure {
                        onFetchFailed((it as? Failure.Network)?.errorCode)
                    }
            } catch (e: Exception) {
                onFetchFailed(null)
            }
        }
    }

    private fun fetchPageDetails(isPageUpdate: Boolean) {
        viewModelScope.launch {
            try {
                val url = pageUrl ?: currentPage.url ?: currentPageName ?: throw IllegalStateException("Page url/name null!")
                loadPageDetailsUseCase(LoadPageDetailsUseCase.Params(canvasContext, url, forceNetwork = true))
                    .onSuccess {
                        currentPage = it
                        renderPage(it, isPageUpdate)
                    }
                    .onFailure {
                        onFetchFailed((it as? Failure.Network)?.errorCode)
                    }
            } catch (e: Exception) {
                onFetchFailed(null)
            }
        }
    }

    private fun renderPage(page: Page, isPageUpdate: Boolean) {
        val lockInfo = page.lockInfo
        if (lockInfo != null) {
            // Locked pages freeze title/edit-visibility at their pre-fetch defaults, matching the
            // legacy fragment's behavior exactly (existing production behavior, not a bug to fix here).
            val lockedHtml = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, R.string.lockedPageDesc, !navigatedFromModules)
            _uiState.update {
                it.copy(
                    screenState = PageDetailsScreenState.Content,
                    htmlContent = lockedHtml,
                    baseUrl = null,
                    pageSet = true,
                    clearWebViewHistory = isPageUpdate
                )
            }
            return
        }

        viewModelScope.launch {
            val body = page.body
            val htmlContent = if (!body.isNullOrEmpty() && body != "null") {
                var content = body
                if (Locale.getDefault().isRtl) {
                    content = "<body dir=\"rtl\">$content</body>"
                }
                // Some pages need to know the course ID, so we set it on window.ENV.COURSE.id (See MBL-14324)
                val withCourseIdScript = """<script>window.ENV = { COURSE: { id: "${canvasContext.id}" } };</script>""" + content
                // JSTextSelectionInterface's highlight anchoring (highlightSelection/selectionchange) resolves
                // StudyNote XPath containers (e.g. "/p[1]") as paths relative to the DIRECT children of the
                // element with id="parent-container", so that id must wrap our real content with nothing extra
                // in between, or highlighting silently no-ops. Unlike Horizon's own page screen, which nests an
                // extra inner <div> here, this wraps content directly (single level) so a container's simple
                // XPath segments (one per real DOM ancestor) resolve as authored/expected - Horizon's StudyNotes
                // are on a separate (Redwood) backend today and never round-trip through this screen, so there's
                // no cross-screen XPath compatibility to preserve by matching its nesting.
                val wrapped = """<div id="parent-container">$withCourseIdScript</div>"""
                // Must wrap BEFORE formatHtmlWithIframes, not after: that formatter re-wraps its ENTIRE input in
                // a full HTML document (discussion_topic_header_html_template, with its own <meta>/<style>/<div
                // id="header_content">/<script>) whenever the content contains an <iframe> (e.g. embedded Sway/
                // media pages). If we wrapped afterward, parent-container's real children would end up nested one
                // level deeper inside that template's own header_content div on iframe-containing pages only,
                // silently breaking every XPath-based highlight lookup on exactly those pages. Wrapping first
                // guarantees parent-container's direct children are always our real content either way: when no
                // iframe is present formatHtmlWithIframes returns its input unchanged, and when one is present our
                // wrapper just ends up nested inside header_content instead of at the document root, which doesn't
                // matter since all anchoring is computed relative to parent-container itself, not document root.
                htmlContentFormatter.formatHtmlWithIframes(wrapped, canvasContext.id)
            } else {
                resources.getString(R.string.noPageFound)
            }

            currentPageName = page.title ?: currentPageName
            bookmarker = computeBookmarker()

            _uiState.update {
                it.copy(
                    screenState = PageDetailsScreenState.Content,
                    toolbarTitle = currentTitle(),
                    htmlContent = htmlContent,
                    baseUrl = page.htmlUrl,
                    editMenuVisible = checkCanEdit(page),
                    pageSet = true,
                    clearWebViewHistory = isPageUpdate,
                    // Reset stale highlights/notebook-button visibility from a previous page while the
                    // new fetch is in flight.
                    highlights = emptyList(),
                    notebookButtonVisible = false,
                    // Not set for locked pages: Study Tools is never offered there, matching
                    // existing (unchanged) behavior.
                    pageSlug = page.url ?: pageUrl
                )
            }

            fetchPageStudyNotes(page)
        }
    }

    /**
     * Notebook highlights are a supplementary overlay on top of the page content, never a blocker -
     * any failure or skip here (missing feature flag, non-student context, etc.) is silently
     * swallowed and must never affect [PageDetailsUiState.screenState].
     */
    private fun fetchPageStudyNotes(page: Page) {
        val course = canvasContext as? Course
        if (page.id == 0L || course?.isStudent != true) return
        viewModelScope.launch {
            try {
                if (!featureFlagProvider.notebookEnabled()) return@launch
                // Set before awaiting the use case so the toolbar button appears as soon as we know the
                // feature applies, independent of whether the page has notes yet (the list screen has its
                // own empty state).
                _uiState.update { it.copy(notebookButtonVisible = true) }
                val notes = getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(course.id, page.id.toString()))
                _uiState.update { it.copy(highlights = notes) }
            } catch (e: Exception) {
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    private fun onFetchFailed(errorCode: Int?) {
        // Ported verbatim from the legacy fragment's loadFailedPageInfo(): the front-page-specific
        // message only shows when currentPageName is literally "front-page", which (today) is only
        // ever true when routed from a URL whose page_id resolved to that value. Front-page fetches
        // reached via makeFrontPageRoute() (e.g. from the Pages list) leave currentPageName null and
        // therefore fall into the generic message below on failure - existing behavior, preserved as-is.
        val message = if (errorCode != null && errorCode in 400..499 && currentPageName == Page.FRONT_PAGE_NAME) {
            if (canvasContext.type == CanvasContext.Type.COURSE) {
                resources.getString(R.string.noPagesAvailableForCourse)
            } else {
                resources.getString(R.string.noPagesAvailableForGroup)
            }
        } else {
            resources.getString(R.string.noPageFound)
        }

        _uiState.update {
            it.copy(
                screenState = PageDetailsScreenState.Content,
                toolbarTitle = currentTitle(),
                htmlContent = message,
                baseUrl = null
                // pageSet intentionally left untouched: a total fetch failure never completes the
                // "pageSet" page-view prerequisite, matching existing (unchanged) analytics behavior.
            )
        }
    }

    private fun currentTitle(): String = currentPageName ?: currentPage.title ?: resources.getString(R.string.pages)

    private fun checkCanEdit(page: Page): Boolean {
        val course = canvasContext as? Course
        val editingRoles = page.editingRoles.orEmpty()
        return when {
            course?.isStudent == true -> page.lockInfo == null && (editingRoles.contains("public") || editingRoles.contains("student"))
            course?.isTeacher == true -> editingRoles.contains("public") || editingRoles.contains("teacher")
            else -> false
        }
    }

    private fun computeBookmarker(): Bookmarker {
        val bookmarker = Bookmarker(true, canvasContext)
        // Guard against the legacy `pageName!!` NPE risk: a front page reached via makeFrontPageRoute()
        // (e.g. from the Pages list) never gets a pageName, so fall back to the front-page constant
        // instead of crashing.
        val pageIdParam = when {
            currentPageName == Page.FRONT_PAGE_NAME -> Page.FRONT_PAGE_NAME
            frontPage -> Page.FRONT_PAGE_NAME
            else -> currentPageName
        }
        pageIdParam?.let { bookmarker.withParam(RouterParams.PAGE_ID, it) }
        bookmarker.withUrl(currentPage.htmlUrl)
        return bookmarker
    }

    /** Called by the Fragment's EventBus subscriber after it has already deduped the event by page id. */
    fun onPageUpdated() {
        currentPage = Page(title = currentPage.title)
        getPageDetails(isPageUpdate = true)
    }

    private fun onEditClick() {
        if (APIHelper.hasNetworkConnection()) {
            _uiState.update { it.copy(navigation = PageDetailsNavigation.EditPageScreen(currentPage)) }
        } else {
            _uiState.update { it.copy(showNoInternetDialog = true) }
        }
    }

    private fun onLtiButtonPressed(url: String) {
        _uiState.update { it.copy(navigation = PageDetailsNavigation.LtiScreen(url)) }
    }

    private fun onNavigationHandled() {
        _uiState.update { it.copy(navigation = null) }
    }

    private fun onNoInternetDialogShown() {
        _uiState.update { it.copy(showNoInternetDialog = false) }
    }

    private fun onWebViewHistoryCleared() {
        _uiState.update { it.copy(clearWebViewHistory = false) }
    }

    private fun onHighlightClicked(highlightId: String) {
        val pageId = currentPage.id.takeIf { it != 0L }?.toString() ?: return
        _uiState.update {
            it.copy(
                notebookTrayArgs = NotebookTrayArgs(
                    canvasContext.id, pageId, currentTitle(), initialNoteId = highlightId, forceNetwork = false
                )
            )
        }
    }

    private fun onNotebookClick() {
        val pageId = currentPage.id.takeIf { it != 0L }?.toString() ?: return
        _uiState.update {
            it.copy(notebookTrayArgs = NotebookTrayArgs(canvasContext.id, pageId, currentTitle(), forceNetwork = false))
        }
    }

    private fun onNotebookTrayDismiss() {
        _uiState.update { it.copy(notebookTrayArgs = null) }
    }

    private fun PageDetailsUiState.withCallbacks(): PageDetailsUiState = copy(
        actions = PageDetailsActions(
            onEditClick = ::onEditClick,
            onLtiButtonPressed = ::onLtiButtonPressed,
            onHighlightClicked = ::onHighlightClicked,
            onNotebookClick = ::onNotebookClick,
            onNotebookTrayDismiss = ::onNotebookTrayDismiss
        ),
        onNavigationHandled = ::onNavigationHandled,
        onNoInternetDialogShown = ::onNoInternetDialogShown,
        onWebViewHistoryCleared = ::onWebViewHistoryCleared
    )

    companion object {
        const val PAGE_NAME = "pageDetailsName"
        const val PAGE = "pageDetails"
        const val PAGE_URL = "pageUrl"
        const val NAVIGATED_FROM_MODULES = "navigated_from_modules"
        const val FRONT_PAGE = "frontPage"
    }
}
