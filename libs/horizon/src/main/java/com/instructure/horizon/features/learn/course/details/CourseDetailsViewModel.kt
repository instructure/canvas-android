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
package com.instructure.horizon.features.learn.course.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.learn.navigation.LearnRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CourseDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailsUiState())
    val state = _uiState.asStateFlow()

    private val courseId: Long = savedStateHandle[LearnRoute.LearnCourseDetailsScreen.courseIdAttr] ?: -1L

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            fetchData()
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isError = true, isLoading = false)) }
        }
    }

    suspend fun fetchData(forceRefresh: Boolean = false) {
        val course = repository.getCourse(courseId, forceRefresh)
        val hasTools = repository.hasExternalTools(courseId, forceRefresh)
        val availableTabs = if (hasTools)
            CourseDetailsTab.entries
        else
            CourseDetailsTab.entries.minus(CourseDetailsTab.Tools)

        _uiState.update {
            it.copy(
                courseName = course.courseName,
                courseProgress = course.progress,
                courseId = course.courseId,
                courseSyllabus = course.courseSyllabus.orEmpty(),
                availableTabs = availableTabs
            )
        }
    }
}