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
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.addedit.AddEditViewModel
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: EditNoteRepository,
    savedStateHandle: SavedStateHandle
): AddEditViewModel() {
    private val noteId: String = savedStateHandle.toRoute<NotebookRoute.EditNotebook>().noteId
    private val noteType: String =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().noteType
    private val highlightedText: String =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().highlightedText
    private val userComment: String =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().userComment
    private val highlightedTextStartOffset: Int =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().highlightedTextStartOffset
    private val highlightedTextEndOffset: Int =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().highlightedTextEndOffset
    private val highlightedTextStartContainer: String =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().highlightedTextStartContainer
    private val highlightedTextEndContainer: String =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().highlightedTextEndContainer
    private val highlightedTextSelectionStart: Int =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().textSelectionStart
    private val highlightedTextSelectionEnd: Int =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().textSelectionEnd
    private val lastModifiedDate: String? =
        savedStateHandle.toRoute<NotebookRoute.EditNotebook>().updatedAt


    init {
        _uiState.update {
            it.copy(
                title = "Edit note",
                highlightedData = NoteHighlightedData(
                    selectedText = highlightedText,
                    range = NoteHighlightedDataRange(
                        startOffset = highlightedTextStartOffset,
                        endOffset = highlightedTextEndOffset,
                        startContainer = highlightedTextStartContainer,
                        endContainer = highlightedTextEndContainer
                    ),
                    textPosition = NoteHighlightedDataTextPosition(
                        start = highlightedTextSelectionStart,
                        end = highlightedTextSelectionEnd
                    )
                ),
                userComment = TextFieldValue(userComment),
                type = NotebookType.valueOf(noteType),
                lastModifiedDate = lastModifiedDate,
                onSaveNote = ::editNote,
                onDeleteNote = ::deleteNote,
            )
        }
    }

    private fun editNote(onFinished: () -> Unit) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoading = true) }

            repository.updateNote(
                noteId = noteId,
                userText = uiState.value.userComment.text,
                highlightedData = uiState.value.highlightedData,
                type = uiState.value.type
            )

            _uiState.update { it.copy(isLoading = false, snackbarMessage = context.getString(R.string.noteHasBeenSavedMessage)) }
            onFinished()

        } catch {
            _uiState.update { it.copy(isLoading = false, snackbarMessage = context.getString(R.string.failedToSaveNoteMessage)) }
        }
    }

    private fun deleteNote(onFinished: () -> Unit) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoading = true) }

            repository.deleteNote(noteId)

            _uiState.update { it.copy(isLoading = false, snackbarMessage = context.getString(R.string.noteHasBeenDeletedMessage)) }
            onFinished()
        } catch {
            _uiState.update { it.copy(isLoading = false, snackbarMessage = context.getString(R.string.failedToDeleteNoteMessage)) }
        }
    }
}