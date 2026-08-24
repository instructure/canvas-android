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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.notes.GetPageStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.pages.LoadFrontPageUseCase
import com.instructure.pandautils.domain.usecase.pages.LoadPageDetailsUseCase
import com.instructure.pandautils.features.notes.notebook.NotebookTrayArgs
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.HtmlContentFormatter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PageDetailsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val loadFrontPageUseCase: LoadFrontPageUseCase = mockk(relaxed = true)
    private val loadPageDetailsUseCase: LoadPageDetailsUseCase = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val getPageStudyNotesUseCase: GetPageStudyNotesUseCase = mockk(relaxed = true)
    private val firebaseCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    private val studentCourse = Course(id = 1L, enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
    private val teacherCourse = Course(id = 1L, enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Teacher)))

    private fun createStudyNote(id: String = "note-1") = StudyNote(
        id = id,
        userText = "Great point",
        reactions = emptyList(),
        highlightData = null,
        learningObjectId = "1",
        learningObjectType = LearningObjectType.WikiPage,
        createdAt = null,
        updatedAt = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(APIHelper)
        every { APIHelper.hasNetworkConnection() } returns true

        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns studentCourse
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_URL) } returns null
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.NAVIGATED_FROM_MODULES) } returns false
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.FRONT_PAGE) } returns false
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns null
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_NAME) } returns null
        every { savedStateHandle.get<String>(PageDetailsViewModel.NOTE_ID) } returns null

        every { resources.getString(R.string.pages) } returns "Pages"
        every { resources.getString(R.string.noPageFound) } returns "No page found"
        every { resources.getString(R.string.noPagesAvailableForCourse) } returns "No pages available for this course."
        every { resources.getString(R.string.noPagesAvailableForGroup) } returns "No pages available for this group."

        coEvery { htmlContentFormatter.formatHtmlWithIframes(any(), any()) } answers { firstArg() }
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun getViewModel() = PageDetailsViewModel(
        context,
        savedStateHandle,
        loadFrontPageUseCase,
        loadPageDetailsUseCase,
        htmlContentFormatter,
        resources,
        featureFlagProvider,
        getPageStudyNotesUseCase,
        firebaseCrashlytics
    )

    @Test
    fun `Renders directly from nav-arg page when body already present, skipping network`() {
        val page = Page(id = 1L, title = "My Page", body = "<p>Hello</p>", editingRoles = "public")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals("My Page", viewModel.uiState.value.toolbarTitle)
        assertTrue(viewModel.uiState.value.htmlContent.contains("Hello"))
        assertTrue(viewModel.uiState.value.pageSet)
    }

    @Test
    fun `Page content is wrapped in a single-level parent-container div for Notebook highlight anchoring`() {
        val page = Page(id = 1L, title = "My Page", body = "<p>Hello</p>", editingRoles = "public")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val htmlContent = getViewModel().uiState.value.htmlContent

        assertTrue(htmlContent.contains("id=\"parent-container\""))
        // Must stay a single-level wrapper (no extra inner <div>, unlike Horizon's own page screen):
        // JSTextSelectionInterface's XPath anchoring is authored relative to parent-container's
        // direct children, so an extra nesting level would shift every offset and break resolution.
        assertFalse(htmlContent.contains("<div id=\"parent-container\"><div>"))
    }

    @Test
    fun `Page content is wrapped in parent-container before formatHtmlWithIframes runs, not after`() {
        // Regression test: formatHtmlWithIframes re-wraps its ENTIRE input in a full HTML document
        // whenever the content contains an iframe. If parent-container wrapped that formatter's
        // OUTPUT instead of its INPUT, real content would end up nested one level deeper inside that
        // document's own container on iframe-containing pages only, silently breaking every Notebook
        // highlight XPath on exactly those pages. Wrapping the raw body first guarantees
        // formatHtmlWithIframes always receives (and its output always contains) our wrapper intact.
        val page = Page(id = 1L, title = "My Page", body = "<p>Hello</p>", editingRoles = "public")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        getViewModel()

        coVerify { htmlContentFormatter.formatHtmlWithIframes(match { it.contains("id=\"parent-container\"") }, any()) }
    }

    @Test
    fun `Toolbar title shows the nav-arg page name immediately, before the fetch resolves`() {
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_NAME) } returns "syllabus"
        val neverCompletes = CompletableDeferred<DataResult<Page>>()
        coEvery { loadPageDetailsUseCase(any()) } coAnswers { neverCompletes.await() }

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Loading, viewModel.uiState.value.screenState)
        assertEquals("syllabus", viewModel.uiState.value.toolbarTitle)
    }

    @Test
    fun `Toolbar title shows the nav-arg page title immediately when no page name is given`() {
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns Page(id = 1L, title = "My Page")
        val neverCompletes = CompletableDeferred<DataResult<Page>>()
        coEvery { loadPageDetailsUseCase(any()) } coAnswers { neverCompletes.await() }

        val viewModel = getViewModel()

        assertEquals("My Page", viewModel.uiState.value.toolbarTitle)
    }

    @Test
    fun `Toolbar title falls back to Pages while loading when nav args carry no name`() {
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.FRONT_PAGE) } returns true
        val neverCompletes = CompletableDeferred<DataResult<Page>>()
        coEvery { loadFrontPageUseCase(any()) } coAnswers { neverCompletes.await() }

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Loading, viewModel.uiState.value.screenState)
        assertEquals("Pages", viewModel.uiState.value.toolbarTitle)
    }

    @Test
    fun `Fetches by title when only a title is present on the nav-arg page`() {
        val page = Page(id = 1L, title = "front-page")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        val fetched = Page(id = 1L, title = "front-page", body = "<p>Body</p>")
        coEvery { loadPageDetailsUseCase(any()) } returns DataResult.Success(fetched)

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Content, viewModel.uiState.value.screenState)
        assertTrue(viewModel.uiState.value.htmlContent.contains("Body"))
    }

    @Test
    fun `Shows generic no-page-found message when nav-arg page has no id, body, or title`() {
        val page = Page(id = 0L, title = null, body = null)
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.FRONT_PAGE) } returns false
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_URL) } returns null

        val viewModel = getViewModel()

        // page.id == 0 falls through to the front-page/regular-fetch branches; force a fetch failure
        // to reach the generic message via a thrown exception (no url/name available).
        assertEquals(PageDetailsScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals("No page found", viewModel.uiState.value.htmlContent)
        assertFalse(viewModel.uiState.value.pageSet)
    }

    @Test
    fun `Front page fetch success renders content`() {
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.FRONT_PAGE) } returns true
        val page = Page(id = 5L, title = "front-page", body = "<p>Front</p>", htmlUrl = "https://x/front-page")
        coEvery { loadFrontPageUseCase(any()) } returns DataResult.Success(page)

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Content, viewModel.uiState.value.screenState)
        assertTrue(viewModel.uiState.value.htmlContent.contains("Front"))
        assertEquals("https://x/front-page", viewModel.uiState.value.baseUrl)
    }

    @Test
    fun `Front page fetch failure with 400-499 and front-page name shows context-specific message`() {
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.FRONT_PAGE) } returns true
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_NAME) } returns Page.FRONT_PAGE_NAME
        coEvery { loadFrontPageUseCase(any()) } returns DataResult.Fail(Failure.Network(errorCode = 404))

        val viewModel = getViewModel()

        assertEquals("No pages available for this course.", viewModel.uiState.value.htmlContent)
        assertFalse(viewModel.uiState.value.pageSet)
    }

    @Test
    fun `Front page fetch failure without a front-page name shows generic message`() {
        // Reached via e.g. PageListFragment's makeFrontPageRoute(), which never sets PAGE_NAME.
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.FRONT_PAGE) } returns true
        coEvery { loadFrontPageUseCase(any()) } returns DataResult.Fail(Failure.Network(errorCode = 404))

        val viewModel = getViewModel()

        assertEquals("No page found", viewModel.uiState.value.htmlContent)
    }

    @Test
    fun `Regular page fetch failure with non-4xx error shows generic message regardless of page name`() {
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_NAME) } returns Page.FRONT_PAGE_NAME
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_URL) } returns "some-page"
        coEvery { loadPageDetailsUseCase(any()) } returns DataResult.Fail(Failure.Network(errorCode = 500))

        val viewModel = getViewModel()

        assertEquals("No page found", viewModel.uiState.value.htmlContent)
    }

    @Test
    fun `Locked page renders lock HTML and freezes title and edit visibility`() {
        val lockInfo = LockInfo(contextModule = null)
        val page = Page(id = 1L, title = "Locked Page", body = "<p>Secret</p>", lockInfo = lockInfo, editingRoles = "public")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Content, viewModel.uiState.value.screenState)
        // Title/edit-visibility are not recomputed for locked pages - the title stays at the value
        // seeded from the nav args, and the edit menu stays hidden.
        assertEquals("Locked Page", viewModel.uiState.value.toolbarTitle)
        assertFalse(viewModel.uiState.value.editMenuVisible)
        assertTrue(viewModel.uiState.value.pageSet)
    }

    @Test
    fun `Student can edit when editingRoles allows students and page is not locked`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>", editingRoles = "students")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.editMenuVisible)
    }

    @Test
    fun `Student cannot edit when editingRoles does not include student or public`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>", editingRoles = "teachers")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.editMenuVisible)
    }

    @Test
    fun `Teacher can edit regardless of lock state when editingRoles allows teachers`() {
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns teacherCourse
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>", editingRoles = "teachers")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.editMenuVisible)
    }

    @Test
    fun `onEditClick navigates to edit screen when online`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>", editingRoles = "students")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        every { APIHelper.hasNetworkConnection() } returns true

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onEditClick()

        val navigation = viewModel.uiState.value.navigation
        assertTrue(navigation is PageDetailsNavigation.EditPageScreen)
    }

    @Test
    fun `onEditClick shows no-internet dialog when offline`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>", editingRoles = "students")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        every { APIHelper.hasNetworkConnection() } returns false

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onEditClick()

        assertTrue(viewModel.uiState.value.showNoInternetDialog)
        assertNull(viewModel.uiState.value.navigation)
    }

    @Test
    fun `onLtiButtonPressed navigates to Lti screen`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onLtiButtonPressed("https://lti.example.com")

        assertEquals(PageDetailsNavigation.LtiScreen("https://lti.example.com"), viewModel.uiState.value.navigation)
    }

    @Test
    fun `Bookmarker falls back to front-page constant instead of crashing when pageName is unavailable`() {
        // frontPage = true, PAGE_NAME never set - this used to NPE via pageName!! in the legacy code.
        every { savedStateHandle.get<Boolean>(PageDetailsViewModel.FRONT_PAGE) } returns true
        coEvery { loadFrontPageUseCase(any()) } returns DataResult.Fail(Failure.Network(errorCode = 404))

        val viewModel = getViewModel()

        assertEquals(Page.FRONT_PAGE_NAME, viewModel.bookmarker.getParamForBookmark[RouterParams.PAGE_ID])
    }

    @Test
    fun `onPageUpdated resets to a title-only page, re-fetches, and flags webview history for clearing`() {
        val page = Page(id = 9L, title = "Page", body = "<p>Body</p>", editingRoles = "public")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_URL) } returns null

        val viewModel = getViewModel()
        assertFalse(viewModel.uiState.value.clearWebViewHistory)

        val refetched = Page(id = 9L, title = "Page", body = "<p>Updated</p>")
        coEvery { loadPageDetailsUseCase(any()) } returns DataResult.Success(refetched)

        viewModel.onPageUpdated()

        assertTrue(viewModel.uiState.value.htmlContent.contains("Updated"))
        assertTrue(viewModel.uiState.value.clearWebViewHistory)

        viewModel.uiState.value.onWebViewHistoryCleared()
        assertFalse(viewModel.uiState.value.clearWebViewHistory)
    }

    @Test
    fun `Study notes fetch is skipped when the rendered page has no id`() {
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_URL) } returns "some-page"
        val fetchedPage = Page(id = 0L, title = "Page", body = "<p>Body</p>")
        coEvery { loadPageDetailsUseCase(any()) } returns DataResult.Success(fetchedPage)

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Content, viewModel.uiState.value.screenState)
        assertTrue(viewModel.uiState.value.highlights.isEmpty())
        coVerify(exactly = 0) { getPageStudyNotesUseCase(any()) }
    }

    @Test
    fun `Study notes fetch is skipped when canvasContext is not a Course`() {
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns CanvasContext.emptyUserContext()
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.highlights.isEmpty())
        coVerify(exactly = 0) { getPageStudyNotesUseCase(any()) }
    }

    @Test
    fun `Study notes fetch is skipped when the course context is not a student enrollment`() {
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns teacherCourse
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.highlights.isEmpty())
        coVerify(exactly = 0) { getPageStudyNotesUseCase(any()) }
    }

    @Test
    fun `Study notes fetch is skipped when the notebook feature flag is disabled`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        coEvery { featureFlagProvider.notebookEnabled() } returns false

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.highlights.isEmpty())
        coVerify(exactly = 0) { getPageStudyNotesUseCase(any()) }
    }

    @Test
    fun `Study notes are fetched and populate highlights when the feature flag is enabled`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        coEvery { featureFlagProvider.notebookEnabled() } returns true
        val note = createStudyNote()
        coEvery { getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(studentCourse.id, "1")) } returns listOf(note)

        val viewModel = getViewModel()

        assertEquals(listOf(note), viewModel.uiState.value.highlights)
        coVerify { getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(studentCourse.id, "1")) }
    }

    @Test
    fun `Study notes fetch failure is swallowed and does not affect screen state`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        coEvery { featureFlagProvider.notebookEnabled() } returns true
        coEvery { getPageStudyNotesUseCase(any()) } throws RuntimeException("boom")

        val viewModel = getViewModel()

        assertEquals(PageDetailsScreenState.Content, viewModel.uiState.value.screenState)
        assertTrue(viewModel.uiState.value.htmlContent.contains("Body"))
        assertTrue(viewModel.uiState.value.highlights.isEmpty())
        verify { firebaseCrashlytics.recordException(any()) }
    }

    @Test
    fun `onHighlightClicked sets notebookTrayArgs with the tapped note id and page title`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        coEvery { featureFlagProvider.notebookEnabled() } returns true
        val note = createStudyNote()
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onHighlightClicked("note-1")

        assertEquals(
            NotebookTrayArgs(
                courseId = studentCourse.id, pageId = "1", pageTitle = "Page", initialNoteId = "note-1", forceNetwork = false
            ),
            viewModel.uiState.value.notebookTrayArgs
        )
    }

    @Test
    fun `onNotebookClick sets notebookTrayArgs without an initial note`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onNotebookClick()

        assertEquals(
            NotebookTrayArgs(
                courseId = studentCourse.id, pageId = "1", pageTitle = "Page", initialNoteId = null, forceNetwork = false
            ),
            viewModel.uiState.value.notebookTrayArgs
        )
    }

    @Test
    fun `onNotebookTrayDismiss clears notebookTrayArgs`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onNotebookClick()
        assertTrue(viewModel.uiState.value.notebookTrayArgs != null)

        viewModel.uiState.value.actions.onNotebookTrayDismiss()

        assertNull(viewModel.uiState.value.notebookTrayArgs)
    }

    @Test
    fun `opening the tray does not clear a pending one-shot navigation`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>", editingRoles = "students")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onLtiButtonPressed("https://lti.example.com")
        viewModel.uiState.value.actions.onNotebookClick()

        assertEquals(PageDetailsNavigation.LtiScreen("https://lti.example.com"), viewModel.uiState.value.navigation)
        assertTrue(viewModel.uiState.value.notebookTrayArgs != null)
    }

    @Test
    fun `notebookButtonVisible is true for a student course with the flag enabled`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        coEvery { featureFlagProvider.notebookEnabled() } returns true

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.notebookButtonVisible)
    }

    @Test
    fun `notebookButtonVisible stays false for a teacher course or a disabled flag`() {
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        every { savedStateHandle.get<Page>(PageDetailsViewModel.PAGE) } returns page
        coEvery { featureFlagProvider.notebookEnabled() } returns false

        assertFalse(getViewModel().uiState.value.notebookButtonVisible)

        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns teacherCourse
        coEvery { featureFlagProvider.notebookEnabled() } returns true

        assertFalse(getViewModel().uiState.value.notebookButtonVisible)
    }

    @Test
    fun `notebookTrayArgs is not set when the page id is unresolved`() {
        every { savedStateHandle.get<String>(PageDetailsViewModel.PAGE_URL) } returns "some-page"
        val fetchedPage = Page(id = 0L, title = "Page", body = "<p>Body</p>")
        coEvery { loadPageDetailsUseCase(any()) } returns DataResult.Success(fetchedPage)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onHighlightClicked("note-1")
        assertNull(viewModel.uiState.value.notebookTrayArgs)

        viewModel.uiState.value.actions.onNotebookClick()
        assertNull(viewModel.uiState.value.notebookTrayArgs)
    }

    @Test
    fun `scrollToNoteId is null by default`() {
        val viewModel = getViewModel()

        assertNull(viewModel.uiState.value.scrollToNoteId)
    }

    @Test
    fun `scrollToNoteId is seeded from the nav arg and survives a page fetch`() {
        every { savedStateHandle.get<String>(PageDetailsViewModel.NOTE_ID) } returns "note-42"
        val page = Page(id = 1L, title = "Page", body = "<p>Body</p>")
        coEvery { loadPageDetailsUseCase(any()) } returns DataResult.Success(page)

        val viewModel = getViewModel()

        assertEquals("note-42", viewModel.uiState.value.scrollToNoteId)
    }
}
