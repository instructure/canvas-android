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

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.canvasapi2.models.NoteType
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.models.StudyNoteHighlightData
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.notes.GetPageStudyNotesUseCase
import com.instructure.pandautils.features.notes.composable.toNoteLocalisedDateFormat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
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
        createdAt: Date? = null,
        updatedAt: Date? = null
    ) = StudyNote(
        id = id,
        userText = userText,
        reactions = reactions,
        highlightData = highlightData,
        learningObjectId = "1",
        learningObjectType = LearningObjectType.WikiPage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Long>(NotebookRoute.ARG_COURSE_ID) } returns 1L
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID) } returns "1"
        every { savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_TITLE) } returns "Home Page"
        every { savedStateHandle.get<String>(NotebookRoute.ARG_INITIAL_NOTE_ID) } returns null
        every { savedStateHandle.get<Boolean>(NotebookRoute.ARG_FORCE_NETWORK) } returns true
        every { resources.getString(R.string.notebookLoadError) } returns "There was a problem loading your notes."

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

    private fun getViewModel() = NotebookViewModel(savedStateHandle, resources, getPageStudyNotesUseCase)

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
    fun `onBackClick pops back to the list, keeps the previously loaded notes and does not refetch`() {
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
    fun `onBackClick at the root level is a no-op`() {
        coEvery { getPageStudyNotesUseCase(any()) } returns emptyList()

        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onBackClick()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is NotebookLevel.PageNotes)
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
}
