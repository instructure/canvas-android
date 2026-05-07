/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.notebook

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteObjectType
import com.instructure.horizon.data.repository.NotebookPage
import com.instructure.horizon.domain.usecase.notebook.DeleteNoteUseCase
import com.instructure.horizon.domain.usecase.notebook.GetNotebookCoursesUseCase
import com.instructure.horizon.domain.usecase.notebook.GetNotesUseCase
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotebookViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val getNotesUseCase: GetNotesUseCase = mockk(relaxed = true)
    private val getNotebookCoursesUseCase: GetNotebookCoursesUseCase = mockk(relaxed = true)
    private val deleteNoteUseCase: DeleteNoteUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testNote = Note(
        id = "note1",
        highlightedText = NoteHighlightedData(
            "selected",
            NoteHighlightedDataRange(0, 0, "", ""),
            NoteHighlightedDataTextPosition(0, 0)
        ),
        type = NotebookType.Important,
        userText = "comment",
        updatedAt = Date(),
        courseId = 1L,
        objectType = NoteObjectType.PAGE,
        objectId = "page1",
    )

    private val pageWithMore = NotebookPage(notes = listOf(testNote), hasNextPage = true, endCursor = "endCursor1")
    private val pageNoMore = NotebookPage(notes = emptyList(), hasNextPage = false, endCursor = null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkStateProvider.isOnline() } returns true
        coEvery { getNotesUseCase(any()) } returns pageWithMore
        coEvery { getNotebookCoursesUseCase(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `loads data on init`() {
        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertEquals(listOf(testNote), viewModel.uiState.value.notes)
        assertTrue(viewModel.uiState.value.hasNextPage)
    }

    @Test
    fun `failure sets error state`() = runTest {
        coEvery { getNotesUseCase(any()) } throws Exception("network")

        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
    }

    @Test
    fun `filter selection updates state and reloads`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.value.onFilterSelected(NotebookType.Confusing)

        assertEquals(NotebookType.Confusing, viewModel.uiState.value.selectedFilter)
        coVerify(atLeast = 2) { getNotesUseCase(any()) }
    }

    @Test
    fun `loadNextPage uses end cursor`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.value.loadNextPage()

        coVerify {
            getNotesUseCase(match { it.after == "endCursor1" })
        }
    }

    @Test
    fun `course selected reloads with new courseId`() = runTest {
        val course = mockk<CourseWithProgress>(relaxed = true) { every { courseId } returns 42L }
        val viewModel = viewModel()

        viewModel.uiState.value.onCourseSelected(course)

        assertEquals(course, viewModel.uiState.value.selectedCourse)
        coVerify { getNotesUseCase(match { it.courseId == 42L }) }
    }

    @Test
    fun `delete note removes from list when online`() = runTest {
        val viewModel = viewModel()
        val note = viewModel.uiState.value.notes.first()

        viewModel.uiState.value.deleteNote(note)

        assertFalse(viewModel.uiState.value.notes.contains(note))
        coVerify { deleteNoteUseCase(note.id) }
    }

    @Test
    fun `delete note offline shows snackbar and skips use case`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { getNotesUseCase(any()) } returns NotebookPage(listOf(testNote), false, null)
        every { context.getString(any()) } returns "msg"

        val viewModel = viewModel()
        val note = viewModel.uiState.value.notes.first()

        viewModel.uiState.value.deleteNote(note)

        assertTrue(viewModel.uiState.value.notes.contains(note))
        assertNull(viewModel.uiState.value.showDeleteConfirmationForNote)
        coVerify(exactly = 0) { deleteNoteUseCase(any()) }
    }

    @Test
    fun `offline page sets hasNextPage false`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { getNotesUseCase(any()) } returns pageNoMore

        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.hasNextPage)
        assertFalse(viewModel.uiState.value.isOnline)
    }

    @Test
    fun `updateFilters reloads with new course`() = runTest {
        val viewModel = viewModel()

        viewModel.updateFilters(courseId = 99L)

        coVerify { getNotesUseCase(match { it.courseId == 99L }) }
    }

    private fun viewModel() = NotebookViewModel(
        context,
        getNotesUseCase,
        getNotebookCoursesUseCase,
        deleteNoteUseCase,
        networkStateProvider,
        savedStateHandle,
    )
}
