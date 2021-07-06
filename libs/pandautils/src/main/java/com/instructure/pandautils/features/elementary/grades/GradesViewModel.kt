/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.grades

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradeRowItemViewModel
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradingPeriodSelectorItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val courseManager: CourseManager
) : ViewModel() {


    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<GradesViewData>
        get() = _data
    private val _data = MutableLiveData(GradesViewData(emptyList()))

    val events: LiveData<Event<GradesAction>>
        get() = _events
    private val _events = MutableLiveData<Event<GradesAction>>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _state.postValue(ViewState.Loading)
        loadData(false)
    }

    // Probably should add grading period parameter here
    private fun loadData(forceNetwork: Boolean) {
        viewModelScope.launch {
            try {
                val coursesWithGrades = courseManager.getCoursesWithGradesAsync(forceNetwork).await()
                val viewData = createViewData(coursesWithGrades.dataOrThrow)
                _data.postValue(viewData)
                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error("Failed to load grades"))
            }
        }
    }

    private fun createViewData(courses: List<Course>): GradesViewData {
        val gradingPeriod = GradingPeriod(1, "Current Grading Period")

        val gradeRowItems = courses
            .filter { !it.homeroomCourse }
            .map {
                GradeRowItemViewModel(GradeRowViewData(
                    it.id,
                    it.name,
                    getCourseColor(it),
                    it.enrollments?.first()?.computedCurrentScore,
                    createGradeText(it)))
            }

        val items = listOf<ItemViewModel>(GradingPeriodSelectorItemViewModel(listOf(gradingPeriod), gradingPeriod))
            .plus(gradeRowItems)

        return GradesViewData(items)
    }

    private fun createGradeText(it: Course): String {
        val currentGrade = it.enrollments?.first()?.computedCurrentGrade
        return if (currentGrade != null) {
            currentGrade
        } else {
            val currentScore = it.enrollments?.first()?.computedCurrentScore
            val currentScoreRounded = currentScore?.roundToInt()
            if (currentScoreRounded != null) {
                "$currentScoreRounded%"
            } else {
                "--"
            }
        }
    }

    private fun getCourseColor(course: Course): String {
        return if (course.courseColor.isNullOrEmpty()) {
            ColorApiHelper.K5_DEFAULT_COLOR
        } else {
            course.courseColor!!
        }
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }
}