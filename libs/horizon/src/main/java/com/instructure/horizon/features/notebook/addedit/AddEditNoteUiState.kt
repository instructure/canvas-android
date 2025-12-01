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
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.horizon.features.notebook.common.model.NotebookType

data class AddEditNoteUiState(
    val title: String = "",
    val highlightedData: NoteHighlightedData = NoteHighlightedData(
        "",
        NoteHighlightedDataRange(0, 0, "", ""),
        NoteHighlightedDataTextPosition(0, 0)
    ),
    val userComment: TextFieldValue = TextFieldValue(""),
    val onUserCommentChanged: (TextFieldValue) -> Unit = {},
    val type: NotebookType? = null,
    val onTypeChanged: (NotebookType?) -> Unit,
    val onSaveNote: (() -> Unit) -> Unit = {},
    val isLoading: Boolean = false,
    val lastModifiedDate: String? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val updateDeleteConfirmationDialog: (Boolean) -> Unit = {},
    val showExitConfirmationDialog: Boolean = false,
    val updateExitConfirmationDialog: (Boolean) -> Unit = {},
    val onDeleteNote: ((() -> Unit) -> Unit)? = null,
    val snackbarMessage: String? = null,
    val onSnackbarDismiss: () -> Unit,
    val hasContentChange: Boolean = false
)