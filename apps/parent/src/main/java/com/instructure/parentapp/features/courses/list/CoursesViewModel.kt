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

package com.instructure.parentapp.features.courses.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val repository: CoursesRepository,
    private val selectedStudentHolder: SelectedStudentHolder,
    private val courseGradeFormatter: CourseGradeFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CoursesViewModelAction>()
    val events = _events.receiveAsFlow()

    private var selectedStudent: User? = null

    init {
        viewModelScope.launch {
            selectedStudentHolder.selectedStudentState.collect {
                studentChanged(it)
            }
        }

        viewModelScope.launch {
            selectedStudentHolder.selectedStudentColorChanged.collect {
                updateColor()
            }
        }
    }

    private fun updateColor() {
        selectedStudent?.let { student ->
            _uiState.update {
                it.copy(studentColor = student.studentColor)
            }
        }
    }

    private fun loadCourses(forceRefresh: Boolean = false) {
        selectedStudent?.let { student ->
            viewModelScope.tryLaunch {
                val color = student.studentColor

                _uiState.update {
                    it.copy(
                        isLoading = true,
                        isError = false,
                        studentColor = color
                    )
                }

                val courses = repository.getCourses(student.id, forceRefresh)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        courseListItems = courses.map { course ->
                            CourseListItemUiState(
                                course.id,
                                course.name,
                                course.courseCode,
                                courseGradeFormatter.getGradeText(course, student.id)
                            )
                        }
                    )
                }
            } catch {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
            }
        }
    }

    private fun studentChanged(student: User?) {
        if (selectedStudent != student) {
            selectedStudent = student
            loadCourses()
        }
    }

    fun handleAction(action: CoursesAction) {
        when (action) {
            is CoursesAction.CourseTapped -> {
                viewModelScope.launch {
                    _events.send(CoursesViewModelAction.NavigateToCourseDetails(action.courseId))
                }
            }

            is CoursesAction.Refresh -> {
                loadCourses(true)
            }
        }
    }
}
