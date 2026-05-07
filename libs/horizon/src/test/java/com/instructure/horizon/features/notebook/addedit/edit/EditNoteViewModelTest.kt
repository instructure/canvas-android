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
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.notebook.EditNoteUseCase
import com.instructure.horizon.domain.usecase.notebook.RemoveNoteUseCase
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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
    private val editNoteUseCase: EditNoteUseCase = mockk(relaxed = true)
    private val removeNoteUseCase: RemoveNoteUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private val testNoteId = "note123"
    private val testHighlightedText = "Highlighted"
    private val testUserComment = "Existing comment"
    private val testNoteType = "Important"
    private val testLastModifiedDate = "Updated 2h ago"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        every { networkStateProvider.isOnline() } returns true
        every { context.getString(R.string.editNoteTitle) } returns "Edit note"
        every { context.getString(R.string.noteHasBeenSavedMessage) } returns "Note has been saved"
        every { context.getString(R.string.failedToSaveNoteMessage) } returns "Failed to save note"
        every { context.getString(R.string.noteHasBeenDeletedMessage) } returns "Deleted"
        every { context.getString(R.string.failedToDeleteNoteMessage) } returns "Failed to delete"
        every { context.getString(R.string.notebookOfflineActionUnavailable) } returns "Offline"

        setupSavedStateHandle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state hydrates from route`() {
        val viewModel = getViewModel()
        val state = viewModel.uiState.value

        assertEquals("Edit note", state.title)
        assertEquals(testHighlightedText, state.highlightedData.selectedText)
        assertEquals(testUserComment, state.userComment.text)
        assertEquals(NotebookType.Important, state.type)
        assertTrue(state.isOnline)
    }

    @Test
    fun `edit note online calls use case`() = runTest {
        coEvery { editNoteUseCase(any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("Updated"))
        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        coVerify {
            editNoteUseCase(match {
                it.noteId == testNoteId &&
                    it.userText == "Updated" &&
                    it.type == NotebookType.Important
            })
        }
    }

    @Test
    fun `edit note offline shows snackbar without invoking use case`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Offline", viewModel.uiState.value.snackbarMessage)
        coVerify(exactly = 0) { editNoteUseCase(any()) }
    }

    @Test
    fun `delete note online calls remove use case`() = runTest {
        coEvery { removeNoteUseCase(any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onDeleteNote?.invoke {}
        advanceUntilIdle()

        coVerify { removeNoteUseCase(testNoteId) }
        assertEquals("Deleted", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `delete note offline shows snackbar without invoking use case`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        val viewModel = getViewModel()

        viewModel.uiState.value.onDeleteNote?.invoke {}
        advanceUntilIdle()

        assertEquals("Offline", viewModel.uiState.value.snackbarMessage)
        coVerify(exactly = 0) { removeNoteUseCase(any()) }
    }

    @Test
    fun `failed edit shows error message`() = runTest {
        coEvery { editNoteUseCase(any()) } throws Exception("net")
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("X"))
        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Failed to save note", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun getViewModel(): EditNoteViewModel {
        return EditNoteViewModel(context, editNoteUseCase, removeNoteUseCase, networkStateProvider, savedStateHandle)
    }

    private fun setupSavedStateHandle() {
        val route = NotebookRoute.EditNotebook(
            noteId = testNoteId,
            highlightedTextStartOffset = 0,
            highlightedTextEndOffset = 10,
            highlightedTextStartContainer = "container1",
            highlightedTextEndContainer = "container2",
            textSelectionStart = 0,
            textSelectionEnd = 10,
            highlightedText = testHighlightedText,
            noteType = testNoteType,
            userComment = testUserComment,
            updatedAt = testLastModifiedDate,
        )
        every { savedStateHandle.toRoute<NotebookRoute.EditNotebook>() } returns route
    }
}
