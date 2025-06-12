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

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.NoteHighlightedDataTextPosition
import com.instructure.horizon.features.notebook.addedit.AddEditNoteUiState
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.navigation.MainNavigationRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val repository: EditNoteRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val noteId: String = savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().noteId
    private val noteType: String =
        savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().noteType
    private val highlightedText: String =
        savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().highlightedText
    private val userComment: String =
        savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().userComment
    private val highlightedTextStartOffset: Int =
        savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().highlightedTextStartOffset
    private val highlightedTextEndOffset: Int =
        savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().highlightedTextEndOffset
    private val highlightedTextStartContainer: String =
        savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().highlightedTextStartContainer
    private val highlightedTextEndContainer: String =
        savedStateHandle.toRoute<MainNavigationRoute.EditNotebook>().highlightedTextEndContainer

    private val _uiState = MutableStateFlow(
        AddEditNoteUiState(
            highlightedData = NoteHighlightedData(
                selectedText = highlightedText,
                NoteHighlightedDataTextPosition(
                    startOffset = highlightedTextStartOffset,
                    endOffset = highlightedTextEndOffset,
                    startContainer = highlightedTextStartContainer,
                    endContainer = highlightedTextEndContainer
                )
            ),
            userComment = TextFieldValue(userComment),
            type = NotebookType.valueOf(noteType),
            onTypeChanged = ::onTypeChanged,
            onUserCommentChanged = ::onUserCommentChanged,
            onSaveNote = ::editNote,
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun editNote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.updateNote(
                noteId = noteId,
                userText = uiState.value.userComment.text,
                highlightedData = uiState.value.highlightedData,
                type = uiState.value.type
            )

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun onTypeChanged(newType: NotebookType?) {
        _uiState.update {
            it.copy(
                type = newType
            )
        }
    }

    private fun onUserCommentChanged(userComment: TextFieldValue) {
        _uiState.update { it.copy(userComment = userComment) }
    }
}