/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.student.features.grades

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(private val gradesRepository: GradesRepository): ViewModel()  {

    private val _gradesState = MutableStateFlow<GradesUiState>(GradesUiState.Loading)
    val gradesState = _gradesState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val courses = gradesRepository.loadCourses()
            if (courses.isEmpty()) {
                _gradesState.value = GradesUiState.Empty
                return@launch
            }
            _gradesState.value = GradesUiState.Success(courses.map(::createGradeItem))
        }
    }

    private fun createGradeItem(course: Course): GradeItem {
        return GradeItem(
            name = course.name,
            grade = course.enrollments?.firstOrNull()?.computedCurrentGrade ?: ""
        )
    }
}