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
class AddNoteViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AddNoteRepository,
    savedStateHandle: SavedStateHandle
): AddEditViewModel() {
    private val courseId: String = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().courseId
    private val objectType: String = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().objectType
    private val objectId: String = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().objectId
    private val highlightedTextStartOffset: Int = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().highlightedTextStartOffset
    private val highlightedTextEndOffset: Int = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().highlightedTextEndOffset
    private val highlightedTextStartContainer: String = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().highlightedTextStartContainer
    private val highlightedTextEndContainer: String = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().highlightedTextEndContainer
    private val highlightedTextSelectionStart: Int = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().textSelectionStart
    private val highlightedTextSelectionEnd: Int = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().textSelectionEnd
    private val highlightedText: String = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().highlightedText
    private val noteType: String? = savedStateHandle.toRoute<NotebookRoute.AddNotebook>().noteType

    init {
        _uiState.update {
            it.copy(
                title = "Create note",
                hasContentChange = true,
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
                onSaveNote = ::addNote,
                onDeleteNote = null,
                type = if (noteType == null) null else NotebookType.valueOf(noteType),
            )
        }
    }

    private fun addNote(onFinished: () -> Unit) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoading = true) }

            repository.addNote(
                courseId = courseId,
                objectId = objectId,
                objectType = objectType,
                highlightedData = uiState.value.highlightedData,
                userComment = uiState.value.userComment.text,
                type = uiState.value.type
            )

            _uiState.update { it.copy(isLoading = false, snackbarMessage = context.getString(R.string.noteHasBeenSavedMessage)) }
            onFinished()
        } catch {
            _uiState.update { it.copy(isLoading = false, snackbarMessage = context.getString(R.string.failedToSaveNoteMessage)) }
        }
    }

    override fun hasContentChange(): Boolean {
        return true
    }
}