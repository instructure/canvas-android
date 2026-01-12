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
package com.instructure.horizon.features.notebook.addedit.edit

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
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
import junit.framework.TestCase.assertNotNull
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
class EditNoteViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: EditNoteRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private val testNoteId = "note123"
    private val testHighlightedText = "This is highlighted text"
    private val testUserComment = "Existing comment"
    private val testNoteType = "Important"
    private val testLastModifiedDate = "Updated 2 hours ago"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        every { context.getString(R.string.editNoteTitle) } returns "Edit note"
        every { context.getString(R.string.noteHasBeenSavedMessage) } returns "Note has been saved"
        every { context.getString(R.string.failedToSaveNoteMessage) } returns "Failed to save note"
        every { context.getString(R.string.noteHasBeenDeletedMessage) } returns "Note has been deleted"
        every { context.getString(R.string.failedToDeleteNoteMessage) } returns "Failed to delete note"

        setupSavedStateHandle(
            noteId = testNoteId,
            highlightedText = testHighlightedText,
            userComment = testUserComment,
            noteType = testNoteType,
            updatedAt = testLastModifiedDate
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

        assertEquals("Edit note", state.title)
        assertEquals(testHighlightedText, state.highlightedData.selectedText)
        assertEquals(testUserComment, state.userComment.text)
        assertEquals(NotebookType.Important, state.type)
        assertEquals(testLastModifiedDate, state.lastModifiedDate)
        assertFalse(state.hasContentChange)
        assertNotNull(state.onDeleteNote)
    }

    @Test
    fun `Test initial state with null last modified date`() {
        setupSavedStateHandle(updatedAt = null)
        val viewModel = getViewModel()

        assertNull(viewModel.uiState.value.lastModifiedDate)
    }

    @Test
    fun `Test hasContentChange returns false initially`() {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.hasContentChange)
    }

    @Test
    fun `Test hasContentChange returns true when comment changes`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("New comment"))

        assertTrue(viewModel.uiState.value.hasContentChange)
    }

    @Test
    fun `Test hasContentChange returns true when type changes`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTypeChanged(NotebookType.Confusing)

        assertTrue(viewModel.uiState.value.hasContentChange)
    }

    @Test
    fun `Test hasContentChange returns false when values revert to original`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("New comment"))
        assertTrue(viewModel.uiState.value.hasContentChange)

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue(testUserComment))
        assertFalse(viewModel.uiState.value.hasContentChange)
    }

    @Test
    fun `Test edit note calls repository with correct parameters`() = runTest {
        coEvery { repository.updateNote(any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()
        var onFinishedCalled = false

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("Updated comment"))
        viewModel.uiState.value.onSaveNote { onFinishedCalled = true }
        advanceUntilIdle()

        coVerify {
            repository.updateNote(
                noteId = testNoteId,
                userText = "Updated comment",
                highlightedData = any(),
                type = NotebookType.Important
            )
        }
        assertTrue(onFinishedCalled)
    }

    @Test
    fun `Test edit note with different type`() = runTest {
        coEvery { repository.updateNote(any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onTypeChanged(NotebookType.Confusing)
        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        coVerify {
            repository.updateNote(
                noteId = testNoteId,
                userText = testUserComment,
                highlightedData = any(),
                type = NotebookType.Confusing
            )
        }
    }

    @Test
    fun `Test edit note shows loading state during save`() = runTest {
        coEvery { repository.updateNote(any(), any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(100)
        }
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("Updated"))
        viewModel.uiState.value.onSaveNote {}

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test edit note success shows success message`() = runTest {
        coEvery { repository.updateNote(any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("Updated"))
        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Note has been saved", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test edit note failure shows error message`() = runTest {
        coEvery { repository.updateNote(any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("Updated"))
        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Failed to save note", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test delete note calls repository with correct parameters`() = runTest {
        coEvery { repository.deleteNote(any()) } returns Unit
        val viewModel = getViewModel()
        var onFinishedCalled = false

        viewModel.uiState.value.onDeleteNote?.invoke { onFinishedCalled = true }
        advanceUntilIdle()

        coVerify {
            repository.deleteNote(noteId = testNoteId)
        }
        assertTrue(onFinishedCalled)
    }

    @Test
    fun `Test delete note shows loading state during delete`() = runTest {
        coEvery { repository.deleteNote(any()) } coAnswers {
            kotlinx.coroutines.delay(100)
        }
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.uiState.value.onDeleteNote?.invoke {}

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test delete note success shows success message`() = runTest {
        coEvery { repository.deleteNote(any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onDeleteNote?.invoke {}
        advanceUntilIdle()

        assertEquals("Note has been deleted", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test delete note failure shows error message`() = runTest {
        coEvery { repository.deleteNote(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.uiState.value.onDeleteNote?.invoke {}
        advanceUntilIdle()

        assertEquals("Failed to delete note", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test delete note failure does not call onFinished`() = runTest {
        coEvery { repository.deleteNote(any()) } throws Exception("Network error")
        val viewModel = getViewModel()
        var onFinishedCalled = false

        viewModel.uiState.value.onDeleteNote?.invoke { onFinishedCalled = true }
        advanceUntilIdle()

        assertFalse(onFinishedCalled)
    }

    @Test
    fun `Test type change updates state and content change flag`() {
        val viewModel = getViewModel()

        assertEquals(NotebookType.Important, viewModel.uiState.value.type)
        assertFalse(viewModel.uiState.value.hasContentChange)

        viewModel.uiState.value.onTypeChanged(NotebookType.Confusing)

        assertEquals(NotebookType.Confusing, viewModel.uiState.value.type)
        assertTrue(viewModel.uiState.value.hasContentChange)
    }

    @Test
    fun `Test user comment change updates state and content change flag`() {
        val viewModel = getViewModel()
        val newComment = "Updated comment"

        assertEquals(testUserComment, viewModel.uiState.value.userComment.text)
        assertFalse(viewModel.uiState.value.hasContentChange)

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue(newComment))

        assertEquals(newComment, viewModel.uiState.value.userComment.text)
        assertTrue(viewModel.uiState.value.hasContentChange)
    }

    @Test
    fun `Test snackbar dismiss clears message`() {
        coEvery { repository.updateNote(any(), any(), any(), any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("Updated"))
        viewModel.uiState.value.onSaveNote {}
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Note has been saved", viewModel.uiState.value.snackbarMessage)

        viewModel.uiState.value.onSnackbarDismiss()

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `Test delete confirmation dialog can be opened and closed`() {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.showDeleteConfirmationDialog)

        viewModel.uiState.value.updateDeleteConfirmationDialog(true)

        assertTrue(viewModel.uiState.value.showDeleteConfirmationDialog)

        viewModel.uiState.value.updateDeleteConfirmationDialog(false)

        assertFalse(viewModel.uiState.value.showDeleteConfirmationDialog)
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

    private fun getViewModel(): EditNoteViewModel {
        return EditNoteViewModel(context, repository, savedStateHandle)
    }

    private fun setupSavedStateHandle(
        noteId: String = testNoteId,
        highlightedText: String = testHighlightedText,
        userComment: String = testUserComment,
        noteType: String = testNoteType,
        updatedAt: String? = testLastModifiedDate
    ) {
        val route = NotebookRoute.EditNotebook(
            noteId = noteId,
            highlightedTextStartOffset = 0,
            highlightedTextEndOffset = 10,
            highlightedTextStartContainer = "container1",
            highlightedTextEndContainer = "container2",
            textSelectionStart = 0,
            textSelectionEnd = 10,
            highlightedText = highlightedText,
            noteType = noteType,
            userComment = userComment,
            updatedAt = updatedAt
        )
        every { savedStateHandle.toRoute<NotebookRoute.EditNotebook>() } returns route
    }
}
