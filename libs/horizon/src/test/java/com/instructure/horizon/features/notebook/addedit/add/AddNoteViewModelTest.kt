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
import com.instructure.horizon.domain.usecase.notebook.AddNoteUseCase
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
    private val addNoteUseCase: AddNoteUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
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
        every { networkStateProvider.isOnline() } returns true
        every { context.getString(R.string.createNoteTitle) } returns "Add note"
        every { context.getString(R.string.noteHasBeenSavedMessage) } returns "Note has been saved"
        every { context.getString(R.string.failedToSaveNoteMessage) } returns "Failed to save note"
        every { context.getString(R.string.notebookOfflineActionUnavailable) } returns "Offline"
        every { DateFormat.getBestDateTimePattern(any(), any()) } returns "yyyy-MM-dd HH:mm"

        setupSavedStateHandle()
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
        assertTrue(state.isOnline)
    }

    @Test
    fun `Test add note calls use case with correct params`() = runTest {
        coEvery { addNoteUseCase(any()) } returns Unit
        val viewModel = getViewModel()
        var finishedCalled = false

        viewModel.uiState.value.onSaveNote { finishedCalled = true }
        advanceUntilIdle()

        coVerify {
            addNoteUseCase(match {
                it.courseId == testCourseId &&
                    it.objectId == testObjectId &&
                    it.objectType == testObjectType &&
                    it.userComment == "" &&
                    it.type == NotebookType.Important
            })
        }
        assertTrue(finishedCalled)
    }

    @Test
    fun `Test add note offline shows snackbar without invoking use case`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Offline", viewModel.uiState.value.snackbarMessage)
        coVerify(exactly = 0) { addNoteUseCase(any()) }
    }

    @Test
    fun `Test add note success shows success message`() = runTest {
        coEvery { addNoteUseCase(any()) } returns Unit
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Note has been saved", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `Test add note failure shows error message`() = runTest {
        coEvery { addNoteUseCase(any()) } throws Exception("Network")
        val viewModel = getViewModel()

        viewModel.uiState.value.onSaveNote {}
        advanceUntilIdle()

        assertEquals("Failed to save note", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Test user comment change updates state`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.onUserCommentChanged(TextFieldValue("hi"))

        assertEquals("hi", viewModel.uiState.value.userComment.text)
    }

    private fun getViewModel(): AddNoteViewModel {
        return AddNoteViewModel(context, addNoteUseCase, networkStateProvider, savedStateHandle)
    }

    private fun setupSavedStateHandle() {
        val route = NotebookRoute.AddNotebook(
            courseId = testCourseId,
            objectType = testObjectType,
            objectId = testObjectId,
            highlightedTextStartOffset = 0,
            highlightedTextEndOffset = 10,
            highlightedTextStartContainer = "container1",
            highlightedTextEndContainer = "container2",
            textSelectionStart = 0,
            textSelectionEnd = 10,
            highlightedText = testHighlightedText,
            noteType = testNoteType,
        )
        every { savedStateHandle.toRoute<NotebookRoute.AddNotebook>() } returns route
    }
}
