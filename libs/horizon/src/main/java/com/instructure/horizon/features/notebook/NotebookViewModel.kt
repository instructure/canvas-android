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
package com.instructure.horizon.features.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.NoteItem
import com.instructure.canvasapi2.managers.NoteObjectType
import com.instructure.canvasapi2.managers.NoteReaction
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotebookViewModel @Inject constructor(
    private val repository: NotebookRepository,
): ViewModel() {
    private var cursorId: String? = null

    private val _uiState = MutableStateFlow(NotebookUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val notesResponse = repository.getNotes()
            cursorId = notesResponse.pageInfo.startCursor

            val notes = notesResponse.edges?.map { edge ->
                val note = edge.node
                NoteItem(
                    id = note.id,
                    rootAccountUuid = note.rootAccountUuid,
                    userId = note.userId,
                    courseId = note.courseId,
                    objectId = note.objectId,
                    objectType = NoteObjectType.fromValue(note.objectType),
                    userText = note.userText.orEmpty(),
                    reactions = note.reaction?.mapNotNull { reaction ->
                        NoteReaction.fromValue(
                            reaction
                        )
                    } ?: emptyList(),
                    highlightedData = repository.parseHighlightedData(note.highlightData),
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt
                )
            }.orEmpty()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    notes = notes.map { note ->
                        Note(
                            id = note.id,
                            highlightedText = note.highlightedData?.selectedText.orEmpty(),
                            type = when (note.reactions.firstOrNull()) {
                                NoteReaction.Important -> NotebookType.Important
                                NoteReaction.Confusing -> NotebookType.Confusing
                                else -> NotebookType.Important
                            },
                            userText = note.userText,
                            updatedAt = note.updatedAt ?: Date(),
                            courseId = note.courseId.toLongOrNull() ?: 0L,
                            objectType = note.objectType ?: NoteObjectType.PAGE,
                            objectId = note.objectId
                        )
                    },
                    hasPreviousPage = notesResponse.pageInfo.hasPreviousPage,
                    hasNextPage = notesResponse.pageInfo.hasNextPage,
                )
            }
        }
    }
}