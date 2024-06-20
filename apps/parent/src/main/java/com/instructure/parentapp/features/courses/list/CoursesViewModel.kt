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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.R
import com.instructure.parentapp.features.main.SelectedStudentHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import javax.inject.Inject


@HiltViewModel
class CoursesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CoursesRepository,
    private val colorKeeper: ColorKeeper,
    private val apiPrefs: ApiPrefs,
    private val selectedStudentHolder: SelectedStudentHolder
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CoursesViewModelAction>()
    val events = _events.receiveAsFlow()

    private var selectedStudent: User? = null

    init {
        viewModelScope.launch {
            selectedStudentHolder.selectedStudentFlow.collect {
                studentChanged(it)
            }
        }
    }

    private fun loadCourses(forceRefresh: Boolean = false) {
        viewModelScope.tryLaunch {
            val color = colorKeeper.getOrGenerateUserColor(selectedStudent).backgroundColor()

            _uiState.update {
                it.copy(
                    isLoading = true,
                    isError = false,
                    studentColor = color
                )
            }

            selectedStudent?.id?.let {
                val courses = repository.getCourses(selectedStudent!!.id, forceRefresh)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        courseListItems = courses.map {
                            CourseListItemUiState(it.id, it.name, it.courseCode, getGradeText(it))
                        }
                    )
                }
            } ?: run {
                setErrorState()
            }
        } catch {
            setErrorState()
        }
    }

    private fun setErrorState() {
        _uiState.update {
            it.copy(
                isLoading = false,
                isError = true
            )
        }
    }

    private fun getGradeText(course: Course): String? {
        val percentageFormat = NumberFormat.getPercentInstance().apply {
            maximumFractionDigits = 2
        }

        val enrollment = course.enrollments?.find { it.userId == selectedStudent?.id } ?: return null
        val grade = course.getCourseGradeForGradingPeriodSpecificEnrollment(enrollment)
        val restrictQuantitativeData = course.settings?.restrictQuantitativeData.orDefault()

        if (grade.isLocked || (restrictQuantitativeData && !grade.hasCurrentGradeString())) return null

        val formattedScore = grade.currentScore?.takeIf {
            !restrictQuantitativeData
        }?.let {
            percentageFormat.format(it / 100)
        }.orEmpty()

        return when {
            !grade.hasCurrentGradeString() -> context.getString(R.string.noGrade)
            !grade.currentGrade.isNullOrEmpty() -> "${grade.currentGrade} $formattedScore"
            else -> formattedScore
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
                    _events.send(
                        CoursesViewModelAction.NavigateToCourseDetails(
                            "${apiPrefs.fullDomain}/courses/${action.courseId}"
                        )
                    )
                }
            }

            is CoursesAction.Refresh -> {
                loadCourses(true)
            }
        }
    }
}
