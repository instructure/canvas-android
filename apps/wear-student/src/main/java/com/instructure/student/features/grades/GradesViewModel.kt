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

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.candroid.R
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.student.ColorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class GradesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gradesRepository: GradesRepository,
) : ViewModel() {

    private val _gradesState = MutableStateFlow<GradesUiState>(GradesUiState.Loading)
    val gradesState = _gradesState.asStateFlow()

    private val colors = mutableMapOf<String, String>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            colors.putAll(gradesRepository.loadColors())
            val courses = gradesRepository.loadCourses()
            if (courses.isEmpty()) {
                _gradesState.value = GradesUiState.Empty
                return@launch
            }
            _gradesState.value = GradesUiState.Success(courses.map(::createGradeItem))
        }
    }

    private fun createGradeItem(course: Course): GradeItem {
        val courseGrade = course.getCourseGrade(false)
        val grade =
            if (courseGrade == null || courseGrade.isLocked) {
                context.getString(R.string.locked)
            } else {
                if (courseGrade.noCurrentGrade) {
                    context.getString(R.string.noGradeText)
                } else {
                    NumberHelper.doubleToPercentage(courseGrade.currentScore, 2)
                }
            }


        val color = colors[course.contextId]?.let {
            val color = ColorUtils.parseColor(it)
            ColorUtils.createThemedColor(color, true)
        } ?: ColorUtils.generateColor(course, true)

        return GradeItem(
            name = course.name,
            grade = grade,
            color = color.textAndIconColor(),
        )
    }
}