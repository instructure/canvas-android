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
package com.instructure.pandautils.features.notes.notebook

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import android.content.res.Resources
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseTaggedStudyNote
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.canvasapi2.models.NoteType
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.models.StudyNoteHighlightData
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCaseParams
import com.instructure.pandautils.domain.usecase.notes.GetCourseStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetEligibleCoursesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetGlobalStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetPageStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetPageTitlesUseCase
import com.instructure.pandautils.features.notes.composable.toNoteLocalisedDateFormat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
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
import java.util.Date

@ExperimentalCoroutinesApi
class NotebookViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val getPageStudyNotesUseCase: GetPageStudyNotesUseCase = mockk(relaxed = true)
    private val getCourseStudyNotesUseCase: GetCourseStudyNotesUseCase = mockk(relaxed = true)
    private val getGlobalStudyNotesUseCase: GetGlobalStudyNotesUseCase = mockk(relaxed = true)
    private val getEligibleCoursesUseCase: GetEligibleCoursesUseCase = mockk(relaxed = true)
    private val getPageTitlesUseCase: GetPageTitlesUseCase = mockk(relaxed = true)
    private val notebookRouter: NotebookRouter = mockk(relaxed = true)
    private val notebookPrefs: NotebookPrefs = mockk(relaxed = true)
    private val loadCourseUseCase: LoadCourseUseCase = mockk(relaxed = true)

    private fun createStudyNote(
        id: String = "note-1",
        reactions: List<String> = emptyList(),
        highlightData: StudyNoteHighlightData? = StudyNoteHighlightData(
            selectedText = "Selected text",
            range = null,
            textPosition = null,
            pageLastModifiedAt = null
        ),
        userText: String = "User note",
        learningObjectId: String = "1",
        learningObjectType: LearningObjectType = LearningObjectType.WikiPage,
        createdAt: Date? = null,
        updatedAt: Date? = null
    ) = StudyNote(
        id = id,
        userText = userText,
        reactions = reactions,
        highlightData = highlightData,
        learningObjectId = learningObjectId,
        learningObjectType = learningObjectType,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Long>(NotebookRoute.ARG_COURSE_ID) } returns 1L
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns "1"
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_TITLE) } returns "Home Page"
        every { savedStateHandle.get<String>(NotebookRoute.ARG_COURSE_TITLE) } returns "Course 1"
        every { savedStateHandle.get<String>(NotebookRoute.ARG_INITIAL_NOTE_ID) } returns null
        every { savedStateHandle.get<Boolean>(NotebookRoute.ARG_FORCE_NETWORK) } returns true
        every { resources.getString(R.string.notebookLoadError) } returns "There was a problem loading your notes."
        every { resources.getString(R.string.notebookPageNoteLabel, any()) } answers {
            "Page note ${(invocation.args[1] as Array<*>).joinToString()}"
        }
        every { resources.getString(R.string.notebookAssignmentNoteLabel, any()) } answers {
            "Assignment note ${(invocation.args[1] as Array<*>).joinToString()}"
        }
        every { resources.getString(R.string.notebookQuizNoteLabel, any()) } answers {
            "Quiz note ${(invocation.args[1] as Array<*>).joinToString()}"
        }

        // Sane, explicit defaults for the label-lookup use cases - MockK's relaxed-mock default for a
        // Map<String, String>/List<Course> return type isn't reliably a safe non-null empty value, and
        // an unstubbed call landing on a bad default would otherwise throw deep inside loadTopList()'s
        // try block (after the notes fetch itself already succeeded) and get misreported as an Error
        // state instead of Content. Individual tests override these with a more specific stub as needed.
        coEvery { getPageTitlesUseCase(any()) } returns emptyMap()
        coEvery { getEligibleCoursesUseCase(Unit) } returns emptyList()
        coEvery { getGlobalStudyNotesUseCase(any()) } returns emptyList()
        coEvery { getCourseStudyNotesUseCase(any()) } returns emptyList()
        every { notebookPrefs.selectedFilter } returns NoteTypeFilter.All

        // Date.toNoteLocalisedDateFormat() delegates to android.text.format.DateFormat, which isn't
        // available in a plain (non-Robolectric) JVM unit test (see PageDetailsHighlightMapperTest's
        // note on the same limitation). Stub it deterministically so mapping/sort-order assertions
        // below don't depend on that Android stub jar.
        mockkStatic("com.instructure.pandautils.features.notes.composable.NoteDateFormatKt")
        every { any<Date>().toNoteLocalisedDateFormat() } answers { "formatted-${firstArg<Date>().time}" }
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun getViewModel() = NotebookViewModel(
        savedStateHandle,
        resources,
        getPageStudyNotesUseCase,
        getCourseStudyNotesUseCase,
        getGlobalStudyNotesUseCase,
        getEligibleCoursesUseCase,
        getPageTitlesUseCase,
        notebookRouter,
        notebookPrefs,
        loadCourseUseCase
    )

    // region Page level (existing behavior, unchanged)

    @Test
    fun `loads notes on init and populates content in newest-first order`() {
        val older = createStudyNote(id = "1", updatedAt = Date(1_000))
        val newer = createStudyNote(id = "2", updatedAt = Date(2_000))
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(older, newer)

        val viewModel = getViewModel()

        assertEquals(NotebookScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals(listOf("2", "1"), viewModel.uiState.value.notes.map { it.id })
        assertEquals(listOf("2", "1"), viewModel.uiState.value.filteredNotes.map { it.id })
    }

    @Test
    fun `use case throws results in Error state with the resource message and empty notes`() {
        coEvery { getPageStudyNotesUseCase(any()) } throws RuntimeException("boom")

        val viewModel = getViewModel()

        val state = viewModel.uiState.value.screenState
        assertTrue(state is NotebookScreenState.Error)
        assertEquals("There was a problem loading your notes.", (state as NotebookScreenState.Error).message)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
    }

    @Test
    fun `mapping produces empty highlightedText when highlightData is null`() {
        val note = createStudyNote(highlightData = null)
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()

        assertEquals("", viewModel.uiState.value.notes.first().highlightedText)
    }

    @Test
    fun `mapping resolves noteType from reactions`() {
        val note = createStudyNote(reactions = listOf("Important"))
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()

        assertEquals(NoteType.IMPORTANT, viewModel.uiState.value.notes.first().noteType)
    }

    @Test
    fun `mapping formats dateLabel from updatedAt or createdAt, null when both are null`() {
        val createdOnly = createStudyNote(id = "1", createdAt = Date(1_000), updatedAt = null)
        val updatedOverridesCreated = createStudyNote(id = "2", createdAt = Date(1_000), updatedAt = Date(2_000))
        val neither = createStudyNote(id = "3", createdAt = null, updatedAt = null)
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(createdOnly, updatedOverridesCreated, neither)

        val viewModel = getViewModel()
        val notesById = viewModel.uiState.value.notes.associateBy { it.id }

        assertEquals("formatted-1000", notesById["1"]?.dateLabel)
        // updatedAt (2000) wins over createdAt (1000) even though both are set.
        assertEquals("formatted-2000", notesById["2"]?.dateLabel)
        assertNull(notesById["3"]?.dateLabel)
    }

    @Test
    fun `pageTitle seeded from SavedStateHandle is exposed as levelTitle`() {
        coEvery { getPageStudyNotesUseCase(any()) } returns emptyList()

        val viewModel = getViewModel()

        assertEquals("Home Page", viewModel.uiState.value.levelTitle)
    }

    @Test
    fun `forceNetwork from SavedStateHandle defaults to true when not supplied by the host`() {
        coEvery { getPageStudyNotesUseCase(any()) } returns emptyList()

        getViewModel()

        coVerify { getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(1L, "1", forceNetwork = true)) }
    }

    @Test
    fun `forceNetwork false from SavedStateHandle is passed through to the use case`() {
        every { savedStateHandle.get<Boolean>(NotebookRoute.ARG_FORCE_NETWORK) } returns false
        coEvery { getPageStudyNotesUseCase(any()) } returns emptyList()

        getViewModel()

        coVerify { getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(1L, "1", forceNetwork = false)) }
    }

    @Test
    fun `onFilterSelected narrows filteredNotes without touching notes, All restores everything`() {
        val important = createStudyNote(id = "1", reactions = listOf("Important"))
        val confusing = createStudyNote(id = "2", reactions = listOf("Confusing"))
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(important, confusing)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onFilterSelected(NoteTypeFilter.Important)

        assertEquals(listOf("1"), viewModel.uiState.value.filteredNotes.map { it.id })
        assertEquals(2, viewModel.uiState.value.notes.size)

        viewModel.uiState.value.actions.onFilterSelected(NoteTypeFilter.All)

        assertEquals(2, viewModel.uiState.value.filteredNotes.size)
    }

    @Test
    fun `onFilterSelected with zero matches yields empty filteredNotes while notes stays populated`() {
        val important = createStudyNote(id = "1", reactions = listOf("Important"))
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(important)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onFilterSelected(NoteTypeFilter.Confusing)

        assertTrue(viewModel.uiState.value.filteredNotes.isEmpty())
        assertEquals(1, viewModel.uiState.value.notes.size)
    }

    @Test
    fun `use case is invoked exactly once across a filter change`() {
        val note = createStudyNote(reactions = listOf("Important"))
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onFilterSelected(NoteTypeFilter.Confusing)
        viewModel.uiState.value.actions.onFilterSelected(NoteTypeFilter.All)

        coVerify(exactly = 1) { getPageStudyNotesUseCase(any()) }
    }

    @Test
    fun `onFilterSelected persists the choice to NotebookPrefs`() {
        coEvery { getPageStudyNotesUseCase(any()) } returns emptyList()

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onFilterSelected(NoteTypeFilter.Confusing)

        verify { notebookPrefs.selectedFilter = NoteTypeFilter.Confusing }
    }

    @Test
    fun `a freshly opened list level seeds selectedFilter from NotebookPrefs`() {
        every { notebookPrefs.selectedFilter } returns NoteTypeFilter.Important
        coEvery { getPageStudyNotesUseCase(any()) } returns emptyList()

        val viewModel = getViewModel()

        assertEquals(NoteTypeFilter.Important, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `onNoteClick pushes a detail level and resolves selectedNote`() {
        val note = createStudyNote(id = "1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onNoteClick("1")

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.NoteDetails("1"), state.currentLevel)
        assertEquals("1", state.selectedNote?.id)
    }

    @Test
    fun `onNoteArrowClick resolves the note's course, routes to its learning object and requests the tray to close`() {
        // learningObjectId ("page-2") deliberately differs from the opened-from page id ("1", the
        // default ARG_PAGE_ID stub) - this note belongs to a different page, so routing applies.
        val note = createStudyNote(id = "1", learningObjectId = "page-2")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)
        val activity: FragmentActivity = mockk(relaxed = true)
        val course = Course(id = 1L, name = "Course 1")
        coEvery { loadCourseUseCase(LoadCourseUseCaseParams(1L)) } returns course

        val viewModel = getViewModel()
        val noteUiState = viewModel.uiState.value.notes.first()
        viewModel.uiState.value.actions.onNoteArrowClick(activity, noteUiState)

        verify { notebookRouter.routeToLearningObject(activity, course, "page-2", LearningObjectType.WikiPage, "1") }
        assertTrue(viewModel.uiState.value.closeTrayRequested)
        assertNull(viewModel.uiState.value.scrollToNoteOnDismiss)
    }

    @Test
    fun `onNoteArrowClick falls back to an id-only CanvasContext when the course fetch fails`() {
        val note = createStudyNote(id = "1", learningObjectId = "page-2")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)
        val activity: FragmentActivity = mockk(relaxed = true)
        coEvery { loadCourseUseCase(any()) } throws RuntimeException("boom")

        val viewModel = getViewModel()
        val noteUiState = viewModel.uiState.value.notes.first()
        viewModel.uiState.value.actions.onNoteArrowClick(activity, noteUiState)

        verify { notebookRouter.routeToLearningObject(activity, CanvasContext.emptyCourseContext(1L), "page-2", LearningObjectType.WikiPage, "1") }
        assertTrue(viewModel.uiState.value.closeTrayRequested)
    }

    @Test
    fun `onNoteArrowClick for a note on the page the tray was opened from just closes and requests a scroll, without routing`() {
        // Default setUp opens the tray on Page level with ARG_PAGE_ID = "1"; this note's
        // learningObjectId ("1") matches it - nothing to navigate to.
        val note = createStudyNote(id = "note-1", learningObjectId = "1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)
        val activity: FragmentActivity = mockk(relaxed = true)

        val viewModel = getViewModel()
        val noteUiState = viewModel.uiState.value.notes.first()
        viewModel.uiState.value.actions.onNoteArrowClick(activity, noteUiState)

        verify(exactly = 0) { notebookRouter.routeToLearningObject(any(), any(), any(), any(), any()) }
        assertTrue(viewModel.uiState.value.closeTrayRequested)
        assertEquals("note-1", viewModel.uiState.value.scrollToNoteOnDismiss)
    }

    @Test
    fun `onBackClick from detail pops back to the list, keeps the previously loaded notes and does not refetch`() {
        val note = createStudyNote(id = "1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onNoteClick("1")
        viewModel.uiState.value.actions.onBackClick()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is NotebookLevel.PageNotes)
        assertEquals(listOf("1"), state.notes.map { it.id })
        coVerify(exactly = 1) { getPageStudyNotesUseCase(any()) }
    }

    @Test
    fun `initialNoteId opens directly on the detail level and resolves selectedNote once loaded`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_INITIAL_NOTE_ID) } returns "note-42"
        val note = createStudyNote(id = "note-42")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.NoteDetails("note-42"), state.currentLevel)
        assertEquals("note-42", state.selectedNote?.id)
    }

    @Test
    fun `initialNoteId targets the detail level immediately, before the fetch resolves`() {
        // Regression test for the "opens immediately, no list-then-slide-to-detail flash" fix: the
        // detail level must already be the current level (with a Loading screenState) the instant the
        // ViewModel is constructed, not only after the notes fetch completes.
        every { savedStateHandle.get<String>(NotebookRoute.ARG_INITIAL_NOTE_ID) } returns "note-42"
        val gate = CompletableDeferred<List<StudyNote>>()
        coEvery { getPageStudyNotesUseCase(any()) } coAnswers { gate.await() }

        val viewModel = getViewModel()

        val intermediateState = viewModel.uiState.value
        assertEquals(NotebookLevel.NoteDetails("note-42"), intermediateState.currentLevel)
        assertEquals(NotebookScreenState.Loading, intermediateState.screenState)
        assertNull(intermediateState.selectedNote)

        gate.complete(listOf(createStudyNote(id = "note-42")))

        val loadedState = viewModel.uiState.value
        assertEquals(NotebookScreenState.Content, loadedState.screenState)
        assertEquals("note-42", loadedState.selectedNote?.id)
    }

    @Test
    fun `unknown initialNoteId opens the detail level with no selected note once loaded`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_INITIAL_NOTE_ID) } returns "does-not-exist"
        val note = createStudyNote(id = "note-1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is NotebookLevel.NoteDetails)
        assertNull(state.selectedNote)
    }

    @Test
    fun `initialNoteId is not re-pushed after onRetryClick`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_INITIAL_NOTE_ID) } returns "note-42"
        val note = createStudyNote(id = "note-42")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()
        // Back to the list, then retry - the initial note must not be pushed a second time.
        viewModel.uiState.value.actions.onBackClick()
        viewModel.uiState.value.actions.onRetryClick()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is NotebookLevel.PageNotes)
    }

    @Test
    fun `selected filter survives a detail push and pop`() {
        val important = createStudyNote(id = "1", reactions = listOf("Important"))
        val confusing = createStudyNote(id = "2", reactions = listOf("Confusing"))
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(important, confusing)

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onFilterSelected(NoteTypeFilter.Important)
        viewModel.uiState.value.actions.onNoteClick("1")
        viewModel.uiState.value.actions.onBackClick()

        assertEquals(NoteTypeFilter.Important, viewModel.uiState.value.selectedFilter)
        assertEquals(listOf("1"), viewModel.uiState.value.filteredNotes.map { it.id })
    }

    @Test
    fun `a load error still surfaces on the detail level when opened via initialNoteId`() {
        // The detail level is targeted synchronously, before the fetch even starts, so a subsequent
        // load failure must not strand the level on the list - it should be visible (and retryable)
        // from the detail screen the user was already shown.
        every { savedStateHandle.get<String>(NotebookRoute.ARG_INITIAL_NOTE_ID) } returns "note-42"
        coEvery { getPageStudyNotesUseCase(any()) } throws RuntimeException("boom")

        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.screenState is NotebookScreenState.Error)
        assertTrue(state.currentLevel is NotebookLevel.NoteDetails)
    }

    @Test
    fun `Page level items is a flat NoteRow list with no section headers`() {
        val note = createStudyNote(id = "1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()

        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertTrue(items.single() is NotebookListItem.NoteRow)
    }

    // endregion

    // region Course level (root opening, e.g. Student's CourseBrowserFragment)

    @Test
    fun `a Course-level opening (no page id) roots on CourseNotes and fetches via the course use case`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val note = createStudyNote(id = "1")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(note)

        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.CourseNotes(1L, "Course 1"), state.currentLevel)
        assertEquals("Course 1", state.levelTitle)
        assertEquals(listOf("1"), state.notes.map { it.id })
        coVerify(exactly = 1) { getCourseStudyNotesUseCase(GetCourseStudyNotesUseCase.Params(1L, forceNetwork = true)) }
    }

    @Test
    fun `CourseNotes level groups notes by page, ordered by each group's most recent note first`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val olderPageOneNote = createStudyNote(id = "1", learningObjectId = "page-1", createdAt = Date(1_000))
        val newerPageTwoNote = createStudyNote(id = "2", learningObjectId = "page-2", createdAt = Date(2_000))
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(olderPageOneNote, newerPageTwoNote)
        coEvery { getPageTitlesUseCase(any()) } returns mapOf("page-1" to "Page One", "page-2" to "Page Two")

        val viewModel = getViewModel()

        val items = viewModel.uiState.value.items
        assertEquals(4, items.size)
        val firstHeader = items[0] as NotebookListItem.SectionHeader
        assertEquals("Page Two", firstHeader.title)
        assertEquals(1, firstHeader.count)
        assertEquals(NotebookLevel.PageNotes("page-2", "Page Two"), firstHeader.target)
        assertEquals("2", (items[1] as NotebookListItem.NoteRow).note.id)
        val secondHeader = items[2] as NotebookListItem.SectionHeader
        assertEquals("Page One", secondHeader.title)
        assertEquals("1", (items[3] as NotebookListItem.NoteRow).note.id)
    }

    @Test
    fun `CourseNotes level falls back to a generic label when a page's title can't be resolved`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val note = createStudyNote(id = "1", learningObjectId = "page-404")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(note)
        coEvery { getPageTitlesUseCase(any()) } returns emptyMap()

        val viewModel = getViewModel()

        val header = viewModel.uiState.value.items.first() as NotebookListItem.SectionHeader
        assertEquals("Page note page-404", header.title)
    }

    @Test
    fun `CourseNotes level still renders Content with fallback section titles when the page-title lookup fails`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val note = createStudyNote(id = "1", learningObjectId = "page-404")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(note)
        coEvery { getPageTitlesUseCase(any()) } throws RuntimeException("boom")

        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(NotebookScreenState.Content, state.screenState)
        assertEquals(listOf("1"), state.notes.map { it.id })
        val header = state.items.first() as NotebookListItem.SectionHeader
        assertEquals("Page note page-404", header.title)
    }

    @Test
    fun `CourseNotes level falls back to a generic label for Assignment and Quiz notes`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val assignmentNote = createStudyNote(id = "1", learningObjectId = "a-1", learningObjectType = LearningObjectType.Assignment)
        val quizNote = createStudyNote(id = "2", learningObjectId = "q-1", learningObjectType = LearningObjectType.Quiz)
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(assignmentNote, quizNote)
        coEvery { getPageTitlesUseCase(any()) } returns emptyMap()

        val viewModel = getViewModel()

        val headers = viewModel.uiState.value.items.filterIsInstance<NotebookListItem.SectionHeader>()
        assertEquals(setOf("Assignment note a-1", "Quiz note q-1"), headers.map { it.title }.toSet())
    }

    @Test
    fun `onSectionHeaderClick on a Course level drills into that page without refetching`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val pageOneNote = createStudyNote(id = "1", learningObjectId = "page-1")
        val pageTwoNote = createStudyNote(id = "2", learningObjectId = "page-2")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(pageOneNote, pageTwoNote)
        coEvery { getPageTitlesUseCase(any()) } returns mapOf("page-1" to "Page One", "page-2" to "Page Two")

        val viewModel = getViewModel()
        val header = viewModel.uiState.value.items.filterIsInstance<NotebookListItem.SectionHeader>()
            .first { it.target == NotebookLevel.PageNotes("page-1", "Page One") }
        viewModel.uiState.value.actions.onSectionHeaderClick(header.target)

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.PageNotes("page-1", "Page One"), state.currentLevel)
        assertEquals(listOf("1"), state.notes.map { it.id })
        coVerify(exactly = 0) { getPageStudyNotesUseCase(any()) }
    }

    @Test
    fun `onBackClick from a page drilled into from Course pops back to Course without refetching`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val note = createStudyNote(id = "1", learningObjectId = "page-1")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(note)
        coEvery { getPageTitlesUseCase(any()) } returns mapOf("page-1" to "Page One")

        val viewModel = getViewModel()
        val header = viewModel.uiState.value.items.filterIsInstance<NotebookListItem.SectionHeader>().first()
        viewModel.uiState.value.actions.onSectionHeaderClick(header.target)
        viewModel.uiState.value.actions.onBackClick()

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.CourseNotes(1L, "Course 1"), state.currentLevel)
        coVerify(exactly = 1) { getCourseStudyNotesUseCase(any()) }
    }

    @Test
    fun `onBackClick from a root Page level fetches the parent Course only the first time`() {
        // Root opening is Page (default setUp), never visited Course before.
        val pageNote = createStudyNote(id = "1", learningObjectId = "1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(pageNote)
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(pageNote)
        coEvery { getEligibleCoursesUseCase(Unit) } returns listOf(Course(id = 1L, name = "Course 1"))

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick()

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.CourseNotes(1L, "Course 1"), state.currentLevel)
        coVerify(exactly = 1) { getCourseStudyNotesUseCase(any()) }
    }

    @Test
    fun `zooming out from a Page opened with forceNetwork false still force-refreshes its parent Course`() {
        // Regression test: PageDetailsScreen opens the tray with forceNetwork = false since it already
        // fetched this page's notes itself - but that optimization must not leak into the Course/Global
        // fetches that follow zooming out, since nothing pre-fetched those.
        every { savedStateHandle.get<Boolean>(NotebookRoute.ARG_FORCE_NETWORK) } returns false
        val pageNote = createStudyNote(id = "1", learningObjectId = "1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(pageNote)
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(pageNote)
        coEvery { getEligibleCoursesUseCase(Unit) } returns listOf(Course(id = 1L, name = "Course 1"))

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick() // Page -> Course

        coVerify { getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(1L, "1", forceNetwork = false)) }
        coVerify { getCourseStudyNotesUseCase(GetCourseStudyNotesUseCase.Params(1L, forceNetwork = true)) }
    }

    @Test
    fun `zooming out from Course to Global with forceNetwork false at the Page root still force-refreshes Global`() {
        every { savedStateHandle.get<Boolean>(NotebookRoute.ARG_FORCE_NETWORK) } returns false
        val pageNote = createStudyNote(id = "1", learningObjectId = "1")
        coEvery { getPageStudyNotesUseCase(any()) } returns listOf(pageNote)
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(pageNote)
        coEvery { getEligibleCoursesUseCase(Unit) } returns listOf(Course(id = 1L, name = "Course 1"))
        coEvery { getGlobalStudyNotesUseCase(any()) } returns listOf(CourseTaggedStudyNote(1L, pageNote))

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick() // Page -> Course
        viewModel.uiState.value.actions.onBackClick() // Course -> Global

        coVerify { getGlobalStudyNotesUseCase(GetGlobalStudyNotesUseCase.Params(listOf(1L), forceNetwork = true)) }
    }

    @Test
    fun `zooming Page to Course and back to Page again never refetches either scope twice`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val note = createStudyNote(id = "1", learningObjectId = "page-1")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(note)
        coEvery { getPageTitlesUseCase(any()) } returns mapOf("page-1" to "Page One")

        val viewModel = getViewModel()
        val header = viewModel.uiState.value.items.filterIsInstance<NotebookListItem.SectionHeader>().first()
        viewModel.uiState.value.actions.onSectionHeaderClick(header.target) // Course -> Page
        viewModel.uiState.value.actions.onBackClick() // Page -> Course (pop)
        viewModel.uiState.value.actions.onSectionHeaderClick(header.target) // Course -> Page again

        coVerify(exactly = 1) { getCourseStudyNotesUseCase(any()) }
        coVerify(exactly = 0) { getPageStudyNotesUseCase(any()) }
    }

    // endregion

    // region Global level (reached only by zooming out of Course)

    @Test
    fun `onBackClick from a root Course level fetches Global via eligible courses fan-out only the first time`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val courseOneNote = createStudyNote(id = "1")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(courseOneNote)
        val courses = listOf(Course(id = 1L, name = "Course 1"), Course(id = 2L, name = "Course 2"))
        coEvery { getEligibleCoursesUseCase(Unit) } returns courses
        coEvery { getGlobalStudyNotesUseCase(any()) } returns listOf(
            CourseTaggedStudyNote(1L, courseOneNote),
            CourseTaggedStudyNote(2L, createStudyNote(id = "2"))
        )

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick()

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.GlobalNotes, state.currentLevel)
        assertEquals(setOf("1", "2"), state.notes.map { it.id }.toSet())
        coVerify(exactly = 1) { getEligibleCoursesUseCase(Unit) }
        coVerify(exactly = 1) { getGlobalStudyNotesUseCase(GetGlobalStudyNotesUseCase.Params(listOf(1L, 2L), forceNetwork = true)) }
    }

    @Test
    fun `Global level groups notes by course, resolving course names from the eligible courses list`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val courseOneNote = createStudyNote(id = "1", createdAt = Date(1_000))
        val courseTwoNote = createStudyNote(id = "2", createdAt = Date(2_000))
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(courseOneNote)
        coEvery { getEligibleCoursesUseCase(Unit) } returns listOf(Course(id = 1L, name = "Course 1"), Course(id = 2L, name = "Course 2"))
        coEvery { getGlobalStudyNotesUseCase(any()) } returns listOf(
            CourseTaggedStudyNote(1L, courseOneNote),
            CourseTaggedStudyNote(2L, courseTwoNote)
        )

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick()

        val items = viewModel.uiState.value.items
        val firstHeader = items[0] as NotebookListItem.SectionHeader
        assertEquals("Course 2", firstHeader.title)
        assertEquals(NotebookLevel.CourseNotes(2L, "Course 2"), firstHeader.target)
    }

    @Test
    fun `drilling from Global into a course that was already visited reuses its cached notes`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val courseOneNote = createStudyNote(id = "1")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(courseOneNote)
        coEvery { getEligibleCoursesUseCase(Unit) } returns listOf(Course(id = 1L, name = "Course 1"))
        coEvery { getGlobalStudyNotesUseCase(any()) } returns listOf(CourseTaggedStudyNote(1L, courseOneNote))

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick() // Course(1) -> Global (fan-out fetch)
        val header = viewModel.uiState.value.items.filterIsInstance<NotebookListItem.SectionHeader>()
            .first { it.target == NotebookLevel.CourseNotes(1L, "Course 1") }
        viewModel.uiState.value.actions.onSectionHeaderClick(header.target) // Global -> Course(1) again

        val state = viewModel.uiState.value
        assertEquals(NotebookLevel.CourseNotes(1L, "Course 1"), state.currentLevel)
        assertEquals(listOf("1"), state.notes.map { it.id })
        // Only the original root-level fetch of course 1 - the Global fan-out and the drill-back-in
        // both reuse it rather than issuing their own network calls.
        coVerify(exactly = 1) { getCourseStudyNotesUseCase(any()) }
    }

    @Test
    fun `drilling from Global into a course never visited before is derived from the fan-out, not refetched`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        val courseOneNote = createStudyNote(id = "1")
        val courseTwoNote = createStudyNote(id = "2")
        coEvery { getCourseStudyNotesUseCase(any()) } returns listOf(courseOneNote)
        coEvery { getEligibleCoursesUseCase(Unit) } returns listOf(Course(id = 1L, name = "Course 1"), Course(id = 2L, name = "Course 2"))
        coEvery { getGlobalStudyNotesUseCase(any()) } returns listOf(
            CourseTaggedStudyNote(1L, courseOneNote),
            CourseTaggedStudyNote(2L, courseTwoNote)
        )

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick() // Course(1) -> Global
        val header = viewModel.uiState.value.items.filterIsInstance<NotebookListItem.SectionHeader>()
            .first { it.target == NotebookLevel.CourseNotes(2L, "Course 2") }
        viewModel.uiState.value.actions.onSectionHeaderClick(header.target) // Global -> Course(2), never fetched before

        val state = viewModel.uiState.value
        assertEquals(listOf("2"), state.notes.map { it.id })
        // Course 2's notes only ever came from the Global fan-out call, never their own dedicated call.
        coVerify(exactly = 1) { getCourseStudyNotesUseCase(any()) }
        coVerify(exactly = 1) { getGlobalStudyNotesUseCase(any()) }
    }

    @Test
    fun `GlobalNotes has no back control wired - onBackClick is a no-op at that level`() {
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns null
        coEvery { getCourseStudyNotesUseCase(any()) } returns emptyList()
        coEvery { getEligibleCoursesUseCase(Unit) } returns emptyList()
        coEvery { getGlobalStudyNotesUseCase(any()) } returns emptyList()

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick() // Course -> Global
        viewModel.uiState.value.actions.onBackClick() // no-op

        assertEquals(NotebookLevel.GlobalNotes, viewModel.uiState.value.currentLevel)
    }

    // endregion
}
