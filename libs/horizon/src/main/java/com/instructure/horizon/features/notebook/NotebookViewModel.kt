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
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.common.model.mapToNotes
import com.instructure.redwood.QueryNotesQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotebookViewModel @Inject constructor(
    private val repository: NotebookRepository,
): ViewModel() {
    private var cursorId: String? = null
    private var pageInfo: QueryNotesQuery.PageInfo? = null

    private val _uiState = MutableStateFlow(NotebookUiState(
        loadPreviousPage = ::getPreviousPage,
        loadNextPage = ::getNextPage,
        onFilterSelected = ::onFilterSelected,
        updateContent = { courseId, objectTypeAndId ->
            loadData(courseId = courseId, objectTypeAndId = objectTypeAndId)
        }
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData(
        after: String? = null,
        before: String? = null,
        courseId: Long? = null,
        objectTypeAndId: Pair<String, String>? = null
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val notesResponse = repository.getNotes(
                after = after,
                before = before,
                filterType = uiState.value.selectedFilter,
                courseId = courseId,
                objectTypeAndId = objectTypeAndId
            )
            cursorId = notesResponse.edges?.firstOrNull()?.cursor
            pageInfo = notesResponse.pageInfo

            val notes = notesResponse.mapToNotes()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    notes = notes,
                    hasPreviousPage = notesResponse.pageInfo.hasPreviousPage,
                    hasNextPage = notesResponse.pageInfo.hasNextPage,
                )
            }
        }
    }

    private fun getNextPage() {
        loadData(after = pageInfo?.endCursor)
    }

    private fun getPreviousPage() {
        loadData(before = pageInfo?.startCursor)
    }

    private fun onFilterSelected(newFilter: NotebookType?) {
        _uiState.update { currentState ->
            currentState.copy(selectedFilter = newFilter)
        }
        loadData()
    }
}