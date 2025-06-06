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
package com.instructure.horizon.features.notebook.add

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.NoteHighlightedDataTextPosition
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.navigation.MainNavigationRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val repository: AddNoteRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val courseId: String = savedStateHandle.toRoute<MainNavigationRoute.AddNotebook>().courseId
    private val objectType: String = savedStateHandle.toRoute<MainNavigationRoute.AddNotebook>().objectType
    private val objectId: String = savedStateHandle.toRoute<MainNavigationRoute.AddNotebook>().objectId
    private val highlightedTextStartPosition: Int = savedStateHandle.toRoute<MainNavigationRoute.AddNotebook>().highlightedTextStart
    private val highlightedTextEndPosition: Int = savedStateHandle.toRoute<MainNavigationRoute.AddNotebook>().highlightedTextEnd
    private val highlightedText: String = savedStateHandle.toRoute<MainNavigationRoute.AddNotebook>().highlightedText

    private val _uiState = MutableStateFlow(AddNoteUiState(
        highlightedData = NoteHighlightedData(
            selectedText = highlightedText,
            textPosition = NoteHighlightedDataTextPosition(
                start = highlightedTextStartPosition,
                end = highlightedTextEndPosition
            )
        ),
        onTypeChanged = ::onTypeChanged,
        onUserCommentChanged = ::onUserCommentChanged,
        onSaveNote = ::addNote,
    ))
    val uiState = _uiState.asStateFlow()

    fun addNote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.addNote(
                courseId = courseId,
                objectId = objectId,
                objectType = objectType,
                highlightedData = uiState.value.highlightedData,
                userComment = uiState.value.userComment.text,
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