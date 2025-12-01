/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesParams
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsParams
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursesWidgetViewModel @Inject constructor(
    private val loadFavoriteCoursesUseCase: LoadFavoriteCoursesUseCase,
    private val loadGroupsUseCase: LoadGroupsUseCase,
    private val sectionExpandedStateDataStore: SectionExpandedStateDataStore,
    private val coursesWidgetBehavior: CoursesWidgetBehavior
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CoursesWidgetUiState(
            onCourseClick = ::onCourseClick,
            onGroupClick = ::onGroupClick,
            onToggleCoursesExpanded = ::toggleCoursesExpanded,
            onToggleGroupsExpanded = ::toggleGroupsExpanded,
            onManageOfflineContent = ::onManageOfflineContent,
            onCustomizeCourse = ::onCustomizeCourse
        )
    )
    val uiState: StateFlow<CoursesWidgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeExpandedStates()
        observeGradeVisibility()
        observeColorOverlay()
    }

    private fun onCourseClick(courseId: Long) {
        coursesWidgetBehavior.onCourseClick(courseId)
    }

    private fun onGroupClick(groupId: Long) {
        coursesWidgetBehavior.onGroupClick(groupId)
    }

    private fun onManageOfflineContent(courseId: Long) {
        coursesWidgetBehavior.onManageOfflineContent(courseId)
    }

    private fun onCustomizeCourse(courseId: Long) {
        coursesWidgetBehavior.onCustomizeCourse(courseId)
    }

    fun refresh() {
        loadData(forceRefresh = true)
    }

    fun toggleCoursesExpanded() {
        viewModelScope.launch {
            val newState = !_uiState.value.isCoursesExpanded
            sectionExpandedStateDataStore.setCoursesExpanded(newState)
        }
    }

    fun toggleGroupsExpanded() {
        viewModelScope.launch {
            val newState = !_uiState.value.isGroupsExpanded
            sectionExpandedStateDataStore.setGroupsExpanded(newState)
        }
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }

            try {
                val courses = loadFavoriteCoursesUseCase(LoadFavoriteCoursesParams(forceRefresh))
                val groups = loadGroupsUseCase(LoadGroupsParams(forceRefresh))

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        courses = courses,
                        groups = groups
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
            }
        }
    }

    private fun observeExpandedStates() {
        viewModelScope.launch {
            combine(
                sectionExpandedStateDataStore.observeCoursesExpanded(),
                sectionExpandedStateDataStore.observeGroupsExpanded()
            ) { coursesExpanded, groupsExpanded ->
                Pair(coursesExpanded, groupsExpanded)
            }.collect { (coursesExpanded, groupsExpanded) ->
                _uiState.update {
                    it.copy(
                        isCoursesExpanded = coursesExpanded,
                        isGroupsExpanded = groupsExpanded
                    )
                }
            }
        }
    }

    private fun observeGradeVisibility() {
        viewModelScope.launch {
            coursesWidgetBehavior.observeGradeVisibility()
                .catch { }
                .collect { showGrades ->
                    _uiState.update { it.copy(showGrades = showGrades) }
                }
        }
    }

    private fun observeColorOverlay() {
        viewModelScope.launch {
            coursesWidgetBehavior.observeColorOverlay()
                .catch { }
                .collect { showColorOverlay ->
                    _uiState.update { it.copy(showColorOverlay = showColorOverlay) }
                }
        }
    }
}