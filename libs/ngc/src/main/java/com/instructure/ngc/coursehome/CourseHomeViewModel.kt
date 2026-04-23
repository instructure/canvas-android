/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.ngc.coursehome

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseHomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadCourseUseCase: LoadCourseUseCase
) : ViewModel() {

    private val courseId: Long = savedStateHandle.get<Long>(ARG_COURSE_ID) ?: 0L

    private val _uiState = MutableStateFlow(CourseHomeUiState())
    val uiState: StateFlow<CourseHomeUiState> = _uiState.asStateFlow()

    init {
        loadCourse()
    }

    fun onTabSelected(tab: CourseHomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun loadCourse() {
        viewModelScope.launch {
            try {
                val course = loadCourseUseCase(
                    LoadCourseUseCaseParams(courseId, false)
                )
                _uiState.update {
                    it.copy(
                        courseName = course.name,
                        courseImageUrl = course.imageUrl,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    companion object {
        const val ARG_COURSE_ID = "courseId"
    }
}