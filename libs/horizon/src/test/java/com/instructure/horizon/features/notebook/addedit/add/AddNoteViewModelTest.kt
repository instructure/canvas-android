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
package com.instructure.horizon.features.notebook.addedit.add

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: AddNoteRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private val testCourseId = "123"
    private val testObjectType = "Assignment"
    private val testObjectId = "456"
    private val testHighlightedText = "This is highlighted text"
    private val testNoteType = "Important"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        mockkStatic("android.text.format.DateFormat")
        every { context.getString(R.string.createNoteTitle) } returns "Add note"
        every { context.getString(R.string.noteHasBeenSavedMessage) } returns "Note has been saved"
        every { context.getString(R.string.failedToSaveNoteMessage) } returns "Failed to save note"
        every { DateFormat.getBestDateTimePattern(any(), any()) } returns "yyyy-MM-dd HH:mm"

        setupSavedStateHandle(
            courseId = testCourseId,
            objectType = testObjectType,
            objectId = testObjectId,
            highlightedText = testHighlightedText,
            noteType = testNoteType
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test initial state is set correctly from saved state`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value

        assertEquals("Add note", state.title)
        assertEquals(testHighlightedText, state.highlightedData.selectedText)
        assertEquals(NotebookType.Important, state.type)
        assertTrue(state.hasContentChange)
        assertNull(state.onDeleteNote)
    }

    @Test
    fun `Test initial state with null note type`() {
        setupSavedStateHandle(noteType = null)
        val viewModel = getViewModel()

        assertNull(viewModel.uiState.value.type)
    }

    @Test
    fun `Test hasContentChange always returns true for add note`() {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.hasContentChange)
    }

    @Test
    fun `Test add note calls repository with correct parameters`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()
        var onFinishedCalled = false

        viewModel.uiState.value.onSaveNote { onFinishedCalled = true }
        advanceUntilIdle()

        coVerify {
            repository.addNote(
                courseId = testCourseId,
                objectId = testObjectId,
                objectType = testObjectType,
                highlightedData = any(),
                userComment = "",
                type = NotebookType.Important
            )
        }
        assertTrue(onFinishedCalled)
    }

    @Test
    fun `Test add note with user comment`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()
        val userComment = "This is my note"

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue(userComment))
        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        coVerify {
            repository.addNote(
                courseId = testCourseId,
                objectId = testObjectId,
                objectType = testObjectType,
                highlightedData = any(),
                userComment = userComment,
                type = any()
            )
        }
    }

    @Test
    fun `Test add note with different type`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onTypeChanged(NotebookType.Confusing)
        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        coVerify {
            repository.addNote(
                courseId = testCourseId,
                objectId = testObjectId,
                objectType = testObjectType,
                highlightedData = any(),
                userComment = any(),
                type = NotebookType.Confusing
            )
        }
    }

    @Test
    fun `Test add note with null type`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } returns Unit
        setupSavedStateHandle(noteType = null)
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        coVerify {
            repository.addNote(
                courseId = testCourseId,
                objectId = testObjectId,
                objectType = testObjectType,
                highlightedData = any(),
                userComment = any(),
                type = null
            )
        }
    }

    @Test
    fun `Test add note shows loading state during save`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(100)
        }
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.uiState.value.onSaveNote {}

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test add note success shows success message`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Note has been saved", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test add note failure shows error message`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Failed to save note", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test add note failure does not call onFinished`() = runTest {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()
        var onFinishedCalled = false

        viewModel.uiState.value.onSaveNote { onFinishedCalled = true }
        advanceUntilIdle()

        assertFalse(onFinishedCalled)
    }

    @Test
    fun `Test type change updates state`() {
        val viewModel = getViewModel()

        assertEquals(NotebookType.Important, viewModel.uiState.value.type)

        viewModel.uiState.value.onTypeChanged(NotebookType.Confusing)

        assertEquals(NotebookType.Confusing, viewModel.uiState.value.type)
    }

    @Test
    fun `Test user comment change updates state`() {
        val viewModel = getViewModel()
        val newComment = "Updated comment"

        assertEquals("", viewModel.uiState.value.userComment.text)

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue(newComment))

        assertEquals(newComment, viewModel.uiState.value.userComment.text)
    }

    @Test
    fun `Test snackbar dismiss clears message`() {
        coEvery { repository.addNote(any(), any(), any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Note has been saved", viewModel.uiState.value.snackbarMessage)

        viewModel.uiState.value.onSnackbarDismiss()

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `Test exit confirmation dialog can be opened and closed`() {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.showExitConfirmationDialog)

        viewModel.uiState.value.updateExitConfirmationDialog(true)

        assertTrue(viewModel.uiState.value.showExitConfirmationDialog)

        viewModel.uiState.value.updateExitConfirmationDialog(false)

        assertFalse(viewModel.uiState.value.showExitConfirmationDialog)
    }

    private fun getViewModel(): AddNoteViewModel {
        return AddNoteViewModel(context, repository, savedStateHandle)
    }

    private fun setupSavedStateHandle(
        courseId: String = testCourseId,
        objectType: String = testObjectType,
        objectId: String = testObjectId,
        highlightedText: String = testHighlightedText,
        noteType: String? = testNoteType
    ) {
        val route = NotebookRoute.AddNotebook(
            courseId = courseId,
            objectType = objectType,
            objectId = objectId,
            highlightedTextStartOffset = 0,
            highlightedTextEndOffset = 10,
            highlightedTextStartContainer = "container1",
            highlightedTextEndContainer = "container2",
            textSelectionStart = 0,
            textSelectionEnd = 10,
            highlightedText = highlightedText,
            noteType = noteType
        )
        every { savedStateHandle.toRoute<NotebookRoute.AddNotebook>() } returns route
    }
}
