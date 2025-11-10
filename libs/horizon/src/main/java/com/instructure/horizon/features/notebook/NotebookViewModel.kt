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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.common.model.mapToNotes
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.OrderDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class NotebookViewModel @Inject constructor(
    private val repository: NotebookRepository,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private var cursorId: String? = null
    private var pageInfo: QueryNotesQuery.PageInfo? = null

    private var courseId: Long? = savedStateHandle.get<String>(NotebookRoute.Notebook.COURSE_ID)?.toLongOrNull()
    private var objectTypeAndId: Pair<String, String>? = getObjectTypeAndId(savedStateHandle)
    private var showTopBar: Boolean = savedStateHandle.get<Boolean>(NotebookRoute.Notebook.SHOW_TOP_BAR) ?: false
    private var showFilters: Boolean = savedStateHandle.get<Boolean>(NotebookRoute.Notebook.SHOW_FILTERS) ?: false
    private var navigateToEdit: Boolean = savedStateHandle.get<Boolean>(NotebookRoute.Notebook.NAVIGATE_TO_EDIT) ?: false

    private val _uiState = MutableStateFlow(NotebookUiState(
        loadPreviousPage = ::getPreviousPage,
        loadNextPage = ::getNextPage,
        onFilterSelected = ::onFilterSelected,
        updateContent = ::updateContent,
        showTopBar = showTopBar,
        showFilters = showFilters,
        navigateToEdit = navigateToEdit,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData(
        after: String? = null,
        before: String? = null,
        courseId: Long? = this.courseId,
    ) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val notesResponse = repository.getNotes(
                after = after,
                before = before,
                filterType = uiState.value.selectedFilter,
                courseId = courseId,
                objectTypeAndId = objectTypeAndId,
                orderDirection = OrderDirection.descending
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
        } catch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    notes = emptyList(),
                    hasPreviousPage = false,
                    hasNextPage = false,
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

    private fun updateContent(courseId: Long?, objectTypeAndId: Pair<String, String>?) {
        if (courseId != this.courseId || objectTypeAndId != this.objectTypeAndId) {
            this.courseId = courseId
            this.objectTypeAndId = objectTypeAndId
            loadData()
        }
    }

    fun updateFilters(courseId: Long? = null, objectTypeAndId: Pair<String, String>? = null) {
        if (courseId != this.courseId) {
            this.courseId = courseId
            this.objectTypeAndId = objectTypeAndId
            loadData()
        }
    }

    fun updateScreenState(showFilters: Boolean = false, showTopBar: Boolean = false) {
        _uiState.update { it.copy(showTopBar = showTopBar, showFilters = showFilters) }
    }

    private fun getObjectTypeAndId(savedStateHandle: SavedStateHandle): Pair<String, String>? {
        val objectType = savedStateHandle.get<String>(NotebookRoute.Notebook.OBJECT_TYPE) ?: return null
        val objectId = savedStateHandle.get<String>(NotebookRoute.Notebook.OBJECT_ID) ?: return null
        return Pair(objectType, objectId)
    }
}