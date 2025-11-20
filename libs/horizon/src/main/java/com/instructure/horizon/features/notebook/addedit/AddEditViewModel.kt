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
package com.instructure.horizon.features.notebook.addedit

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.instructure.horizon.features.notebook.common.model.NotebookType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class AddEditViewModel: ViewModel() {
    protected val _uiState = MutableStateFlow(
        AddEditNoteUiState(
            onTypeChanged = ::onTypeChanged,
            onUserCommentChanged = ::onUserCommentChanged,
            onSnackbarDismiss = ::onSnackbarDismissed,
            updateDeleteConfirmationDialog = ::updateShowDeleteConfirmationDialog,
            updateExitConfirmationDialog = ::updateShowExitConfirmationDialog
        )
    )
    val uiState = _uiState.asStateFlow()

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

    private fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun updateShowExitConfirmationDialog(value: Boolean) {
        _uiState.update { it.copy(showExitConfirmationDialog = value) }
    }

    private fun updateShowDeleteConfirmationDialog(value: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmationDialog = value) }
    }
}