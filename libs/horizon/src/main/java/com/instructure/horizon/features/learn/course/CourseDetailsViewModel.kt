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
package com.instructure.horizon.features.learn.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    private val repository: CourseDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailsUiState())
    val state = _uiState.asStateFlow()

    fun loadState(courseWithProgress: CourseWithProgress) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedCourse = courseWithProgress,
                    availableTabs = it.availableTabs.minus(CourseDetailsTab.Tools)
                )
            }

            val hasTools = repository.hasExternalTools(courseWithProgress.courseId, false)

            _uiState.update {
                it.copy(
                    availableTabs = if (hasTools) CourseDetailsTab.entries else it.availableTabs
                )
            }
        }
    }
}