/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.inbox.coursepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentInboxCoursePickerViewModel @Inject constructor(
    private val repository: ParentInboxCoursePickerRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(ParentInboxCoursePickerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCoursePickerItems()
    }

    fun getContextURL(courseId: Long): String {
        return "${ApiPrefs.fullDomain}/courses/${courseId}"
    }

    private fun loadCoursePickerItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(screenState = ScreenState.Loading) }
            val courses = repository.getCourses().dataOrNull
            val enrollments = repository.getEnrollments().dataOrNull

            if (enrollments == null || courses == null) {
                _uiState.update { it.copy(screenState = ScreenState.Error) }
                return@launch
            }

            val studentContextItems = enrollments.mapNotNull { enrollment ->
                val course = courses.find { it.id == enrollment.courseId } ?: return@mapNotNull null
                val user = enrollment.observedUser ?: return@mapNotNull null
                StudentContextItem(course, user)
            }

            _uiState.update { it.copy(screenState = ScreenState.Data, studentContextItems = studentContextItems) }
        }
    }
}