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
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.common.model.mapToNotes
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.OrderDirection
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

    private var courseId: Long? = null
    private var objectTypeAndId: Pair<String, String>? = null

    private val _uiState = MutableStateFlow(NotebookUiState(
        loadNextPage = ::getNextPage,
        onFilterSelected = ::onFilterSelected,
        onCourseSelected = ::onCourseSelected,
        updateContent = ::updateContent
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadCourses()
        loadData()
        updateScreenState()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            when (val result = repository.getCourses()) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(courses = result.data) }
                }
                is DataResult.Fail -> {
                    _uiState.update { it.copy(courses = emptyList()) }
                }
            }
        }
    }

    private fun loadData(
        after: String? = null,
        courseId: Long? = this.courseId,
        isLoadingMore: Boolean = false
    ) {
        viewModelScope.tryLaunch {
            _uiState.update {
                if (isLoadingMore) {
                    it.copy(isLoadingMore = true)
                } else {
                    it.copy(isLoading = true)
                }
            }

            val notesResponse = repository.getNotes(
                after = after,
                before = null,
                filterType = uiState.value.selectedFilter,
                courseId = courseId,
                objectTypeAndId = objectTypeAndId,
                orderDirection = OrderDirection.descending
            )
            cursorId = notesResponse.edges?.firstOrNull()?.cursor
            pageInfo = notesResponse.pageInfo

            val newNotes = notesResponse.mapToNotes()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    notes = if (isLoadingMore) it.notes + newNotes else newNotes,
                    hasNextPage = notesResponse.pageInfo.hasNextPage,
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    hasNextPage = false,
                )
            }
        }
    }

    private fun getNextPage() {
        if (!uiState.value.isLoadingMore && uiState.value.hasNextPage) {
            loadData(after = pageInfo?.endCursor, isLoadingMore = true)
        }
    }

    private fun onFilterSelected(newFilter: NotebookType?) {
        _uiState.update { currentState ->
            currentState.copy(selectedFilter = newFilter)
        }
        loadData()
    }

    private fun onCourseSelected(course: CourseWithProgress?) {
        _uiState.update { currentState ->
            currentState.copy(selectedCourse = course)
        }
        courseId = course?.courseId
        loadData(courseId = courseId)
    }

    private fun updateContent(courseId: Long?, objectTypeAndId: Pair<String, String>?) {
        if (courseId != this.courseId || objectTypeAndId != this.objectTypeAndId) {
            this.courseId = courseId
            this.objectTypeAndId = objectTypeAndId
            loadData()
        }
        updateScreenState()
    }

    fun updateCourseId(courseId: Long?) {
        if (courseId != this.courseId) {
            this.courseId = courseId
            loadData()
        }
        updateScreenState()
    }

    private fun updateScreenState() {
        if (courseId != null) {
            _uiState.update { it.copy(showTopBar = false) }
            if (objectTypeAndId != null) {
                _uiState.update { it.copy(showNoteTypeFilter = false, showCourseFilter = false) }
            } else {
                _uiState.update { it.copy(showNoteTypeFilter = true, showCourseFilter = false) }
            }
        } else {
            _uiState.update { it.copy(showTopBar = true, showNoteTypeFilter = true, showCourseFilter = true) }
        }
    }
}