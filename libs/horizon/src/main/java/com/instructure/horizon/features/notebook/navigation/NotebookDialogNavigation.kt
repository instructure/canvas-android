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
package com.instructure.horizon.features.notebook.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.features.notebook.NotebookScreen
import com.instructure.horizon.features.notebook.NotebookViewModel
import com.instructure.horizon.features.notebook.addedit.AddEditNoteScreen
import com.instructure.horizon.features.notebook.addedit.add.AddNoteViewModel
import com.instructure.horizon.features.notebook.addedit.edit.EditNoteViewModel
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition

@Composable
fun NotebookDialogNavigation(
    courseId: Long,
    objectFilter: Pair<String, String>,
    onDismiss: () -> Unit,
    onShowSnackbar: (String?, () -> Unit) -> Unit,
) {
    val notebookDialogNavController = rememberNavController()
    NavHost(
        navController = notebookDialogNavController,
        startDestination = NotebookRoute.Notebook.route,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition },
    ) {
        composable(NotebookRoute.Notebook.route) {
            val viewModel = hiltViewModel<NotebookViewModel>()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(courseId, objectFilter) {
                state.updateContent(courseId, objectFilter)
            }
            NotebookScreen(
                mainNavController = notebookDialogNavController,
                state = state,
                onDismiss = { onDismiss() },
                onNoteSelected = { note ->
                    notebookDialogNavController.navigate(
                        NotebookRoute.EditNotebook(
                            noteId = note.id,
                            highlightedTextStartOffset = note.highlightedText.range.startOffset,
                            highlightedTextEndOffset = note.highlightedText.range.endOffset,
                            highlightedTextStartContainer = note.highlightedText.range.startContainer,
                            highlightedTextEndContainer = note.highlightedText.range.endContainer,
                            textSelectionStart = note.highlightedText.textPosition.start,
                            textSelectionEnd = note.highlightedText.textPosition.end,
                            highlightedText = note.highlightedText.selectedText,
                            noteType = note.type.name,
                            userComment = note.userText
                        )
                    )
                }
            )
        }
        composable<NotebookRoute.AddNotebook> {
            val viewModel = hiltViewModel<AddNoteViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AddEditNoteScreen(notebookDialogNavController, uiState, onShowSnackbar)
        }
        composable<NotebookRoute.EditNotebook> {
            val viewModel = hiltViewModel<EditNoteViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AddEditNoteScreen(notebookDialogNavController, uiState, onShowSnackbar)
        }
    }
}