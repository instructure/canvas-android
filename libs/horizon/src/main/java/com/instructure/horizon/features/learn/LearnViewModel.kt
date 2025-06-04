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
package com.instructure.horizon.features.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val learnRepository: LearnRepository,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val _state = MutableStateFlow(
        LearnUiState(
            onSelectedCourseChanged = ::onSelectedCourseChanged,
        )
    )
    val state = _state.asStateFlow()

    private val courseId: Long = savedStateHandle["courseId"] ?: -1L

    init {
        getCourses()
    }

    private fun getCourses(forceRefresh: Boolean = false) = viewModelScope.tryLaunch {
        _state.value = state.value.copy(screenState = state.value.screenState.copy(isLoading = true))
        val courses = learnRepository.getCoursesWithProgress(forceNetwork = forceRefresh)
        val selectedCourse = courses.find { it.course.id == courseId } ?: courses.firstOrNull()
        _state.value = state.value.copy(
            screenState = state.value.screenState.copy(isLoading = false),
            courses = courses,
            selectedCourse = selectedCourse,
        )
    } catch {
        _state.value = state.value.copy(screenState = state.value.screenState.copy(isLoading = false, errorMessage = it?.message))
    }

    private fun onSelectedCourseChanged(course: CourseWithProgress) {
        _state.value = state.value.copy(selectedCourse = course)
    }
}