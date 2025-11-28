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
package com.instructure.horizon.features.dashboard.widget.course.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardCourseListViewModel @Inject constructor(
    private val repository: DashboardCourseListRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(
        DashboardCourseListUiState(
            loadingState = LoadingState(
                onRefresh = ::onRefresh,
                onSnackbarDismiss = ::onDismissSnackbar
            ),
            onFilterOptionSelected = ::onFilterOptionSelected,
            onShowMoreCourses = ::onShowMoreCourses
        )
    )
    val uiState = _uiState.asStateFlow()
    private var allCourses: List<DashboardCourseListCourseState> = emptyList()
    private val pageSize: Int = 10

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            fetchData()
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean = false) {
        val courses = repository.getCourses(forceRefresh)
        val programs = repository.getPrograms(forceRefresh)
        val courseStates = courses.map { course ->
            DashboardCourseListCourseState(
                parentPrograms = programs.filter { program ->
                    program.sortedRequirements.any { requirement ->
                        requirement.courseId == course.courseId
                    }
                }.map { program ->
                    DashboardCourseListParentProgramState(
                        programName = program.name,
                        programId = program.id
                    )
                },
                name = course.courseName,
                courseId = course.courseId,
                progress = course.progress
            )
        }.sortedByDescending { course ->
            course.progress.run { if (this == 100.0) -1.0 else this } // Active courses first, then completed courses
        }

        allCourses = courseStates
        _uiState.update {
            it.copy(
                courses = filterCourses(it.selectedFilterOption, courseStates),
                visibleCourseCount = pageSize
            )
        }
    }

    private fun filterCourses(
        filterOption: DashboardCourseListFilterOption,
        allCourses: List<DashboardCourseListCourseState>
    ): List<DashboardCourseListCourseState> {
        return when (filterOption) {
            DashboardCourseListFilterOption.All -> {
                allCourses
            }
            DashboardCourseListFilterOption.NotStarted -> {
                allCourses.filter { it.progress == 0.0 }
            }
            DashboardCourseListFilterOption.InProgress -> {
                allCourses.filter { it.progress > 0.0 && it.progress < 100.0 }
            }
            DashboardCourseListFilterOption.Completed -> {
                allCourses.filter { it.progress == 100.0 }
            }
        }
    }

    private fun onRefresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            fetchData(true)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false)) }
        }
    }

    private fun onFilterOptionSelected(filterOption: DashboardCourseListFilterOption) {
        _uiState.update {
            it.copy(
                courses = filterCourses(filterOption, allCourses),
                selectedFilterOption = filterOption,
                visibleCourseCount = pageSize
            )
        }
    }

    private fun onShowMoreCourses() {
        _uiState.update {
            it.copy(visibleCourseCount = it.visibleCourseCount + pageSize)
        }
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }
}