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
package com.instructure.student.widget.grades.courseselector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.utils.ScreenState
import com.instructure.student.widget.grades.GradesWidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseSelectorViewModel @Inject constructor(
    private val repository: GradesWidgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CourseSelectorUiState()
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val courses = repository.getCoursesWithGradingScheme(forceNetwork = true).dataOrThrow
                if (courses.isEmpty()) {
                    _uiState.update { it.copy(state = ScreenState.Empty) }
                } else {
                    _uiState.update { it.copy(state = ScreenState.Content, courses = courses) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(state = ScreenState.Error) }
                viewModelScope.ensureActive()
            }
        }
    }
}
