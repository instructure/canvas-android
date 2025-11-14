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
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.common.model.mapToNotes
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.OrderDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotebookViewModel @Inject constructor(
    private val repository: NotebookRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var cursorId: String? = null
    private var pageInfo: QueryNotesQuery.PageInfo? = null

    private var courseId: Long? =
        savedStateHandle.get<String>(NotebookRoute.Notebook.COURSE_ID)?.toLongOrNull()
    private var objectTypeAndId: Pair<String, String>? = getObjectTypeAndId(savedStateHandle)
    private var showTopBar: Boolean =
        savedStateHandle.get<Boolean>(NotebookRoute.Notebook.SHOW_TOP_BAR) ?: false
    private var showFilters: Boolean =
        savedStateHandle.get<Boolean>(NotebookRoute.Notebook.SHOW_FILTERS) ?: false
    private var navigateToEdit: Boolean =
        savedStateHandle.get<Boolean>(NotebookRoute.Notebook.NAVIGATE_TO_EDIT) ?: false

    private val _uiState = MutableStateFlow(
        NotebookUiState(
            loadNextPage = ::getNextPage,
            onFilterSelected = ::onFilterSelected,
            onCourseSelected = ::onCourseSelected,
            updateContent = ::updateContent,
            showTopBar = showTopBar,
            showFilters = showFilters,
            navigateToEdit = navigateToEdit,
        )
    )
    val uiState = _uiState.asStateFlow()
    var updateJob: Job? = null

    init {
        removeCourseFilterIfNeeded()
        loadCourses()
        loadData()
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
        updateJob = viewModelScope.tryLaunch {
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

            val oldNotes = if (courseId != null) {
                uiState.value.notes.filter { it.courseId == this@NotebookViewModel.courseId }
            } else {
                uiState.value.notes
            }
            val newNotes = notesResponse.mapToNotes().also { notes ->
                // Filter notes by courseId if applicable
                if (courseId != null) {
                    notes.filter { it.courseId == courseId }
                } else {
                    notes
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    notes = if (isLoadingMore) oldNotes + newNotes else newNotes,
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
        updateJob?.cancel()
        loadData()
    }

    private fun onCourseSelected(course: CourseWithProgress?) {
        _uiState.update { currentState ->
            currentState.copy(selectedCourse = course)
        }
        updateJob?.cancel()
        courseId = course?.courseId
        loadData(courseId = courseId)
    }

    private fun updateContent(courseId: Long?, objectTypeAndId: Pair<String, String>?) {
        if (courseId != this.courseId || objectTypeAndId != this.objectTypeAndId) {
            this.courseId = courseId
            this.objectTypeAndId = objectTypeAndId
            updateJob?.cancel()
            loadData()
        }
    }

    fun updateFilters(courseId: Long? = null, objectTypeAndId: Pair<String, String>? = null) {
        if (courseId != this.courseId) {
            this.courseId = courseId
            this.objectTypeAndId = objectTypeAndId
            updateJob?.cancel()
            loadData()
        }
    }

    private fun removeCourseFilterIfNeeded() {
        if (courseId != null) {
            _uiState.update { it.copy(showCourseFilter = false) }
        }
    }

    fun updateScreenState(
        showNoteTypeFilter: Boolean = true,
        showCourseFilter: Boolean = true,
        showTopBar: Boolean = false
    ) {
        _uiState.update { it.copy(showTopBar = showTopBar, showCourseFilter = showCourseFilter, showNoteTypeFilter = showNoteTypeFilter) }
    }

    private fun getObjectTypeAndId(savedStateHandle: SavedStateHandle): Pair<String, String>? {
        val objectType =
            savedStateHandle.get<String>(NotebookRoute.Notebook.OBJECT_TYPE) ?: return null
        val objectId = savedStateHandle.get<String>(NotebookRoute.Notebook.OBJECT_ID) ?: return null
        return Pair(objectType, objectId)
    }
}