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
package com.instructure.horizon.features.learn.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    private val courseDetailsRepository: CourseDetailsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(
        CourseDetailsUiState(
            screenState = LoadingState(
                onRefresh = ::onRefresh
            ),
            onSelectedCourseChanged = ::onSelectedCourseChanged,
        )
    )
    val state = _state.asStateFlow()

    private val courseId: Long = savedStateHandle["courseId"] ?: -1L

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _state.update { it.copy(screenState = it.screenState.copy(isLoading = true)) }
            getCourses()
            _state.update { it.copy(screenState = it.screenState.copy(isLoading = false)) }
        } catch {
            _state.update { it.copy(screenState = it.screenState.copy(isLoading = false, errorMessage = "Failed to load Courses")) }
        }
    }

    private suspend fun getCourses(forceRefresh: Boolean = false) {
        val courses = courseDetailsRepository.getCoursesWithProgress(forceNetwork = forceRefresh)
        val selectedCourse = courses.find { it.courseId == courseId } ?: courses.firstOrNull()
        _state.update {
            state.value.copy(courses = courses, selectedCourse = selectedCourse,)
        }
    }

    private fun onSelectedCourseChanged(course: CourseWithProgress) {
        _state.value = state.value.copy(selectedCourse = course)
    }

    private fun onRefresh() {
        viewModelScope.tryLaunch {
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = true)) }
            getCourses(forceRefresh = true)
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        } catch {
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        }
    }
}