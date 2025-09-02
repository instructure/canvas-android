/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.learn

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
class LearnViewModel @Inject constructor(
    private val repository: LearnRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LearnUiState(
            screenState = LoadingState(onRefresh = ::onRefresh),
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
        val courses = repository.getCoursesWithProgress(forceNetwork = forceRefresh)
        val selectedCourse = courses.find { it.courseId == courseId } ?: courses.firstOrNull()
        _state.update {
            state.value.copy(courses = courses, selectedCourse = selectedCourse)
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