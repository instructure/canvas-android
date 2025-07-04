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

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradeRowItemViewModel
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradingPeriodSelectorItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

private const val CURRENT_GRADING_PERIOD_ID = -1L

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val courseManager: CourseManager,
    private val resources: Resources,
    private val enrollmentManager: EnrollmentManager,
    private val colorKeeper: ColorKeeper
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

    private var gradingPeriodsViewModel: GradingPeriodSelectorItemViewModel? = null
    private var courses = emptyList<Course>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _state.postValue(ViewState.Loading)
        loadData(false)
    }

    private fun loadData(forceNetwork: Boolean, previouslySelectedGradingPeriod: GradingPeriod? = null) {
        viewModelScope.launch {
            try {
                val coursesWithGrades = courseManager.getCoursesWithGradesAsync(forceNetwork).await().dataOrThrow
                courses = coursesWithGrades
                    .filter { !it.homeroomCourse }

                courses.forEach {
                    colorKeeper.addToCache(it.contextId, getCourseColor(it))
                }

                gradingPeriodsViewModel = createGradingPeriodsViewModel(courses)
                val gradeRowViewModels = createGradeRowViewModels(courses)
                val viewData = createViewData(gradeRowViewModels)

                _data.postValue(viewData)
                if (viewData.items.isEmpty()) {
                    _state.postValue(ViewState.Empty(emptyTitle = R.string.noGradesToDisplay))
                } else {
                    _state.postValue(ViewState.Success)
                }
            } catch (e: Exception) {
                if (_data.value == null || _data.value?.items?.isNullOrEmpty() == true) {
                    _state.postValue(ViewState.Error(resources.getString(R.string.failedToLoadGrades)))
                } else {
                    _state.postValue(ViewState.Error())
                    _events.postValue(Event(GradesAction.ShowRefreshError))
                }

                if (previouslySelectedGradingPeriod != null) {
                    gradingPeriodsViewModel?.selectedGradingPeriod = previouslySelectedGradingPeriod
                    gradingPeriodsViewModel?.notifyChange()
                }
            }
        }
    }

    private fun createGradingPeriodsViewModel(courses: List<Course>): GradingPeriodSelectorItemViewModel? {
        val gradingPeriods = courses
            .flatMap { it.gradingPeriods ?: emptyList() }
            .distinctBy { gradingPeriod -> gradingPeriod.id }
            .map { gradingPeriod -> GradingPeriod(gradingPeriod.id, gradingPeriod.title ?: "") }

        return if (gradingPeriods.isNotEmpty()) {
            val currentGradingPeriod = GradingPeriod(CURRENT_GRADING_PERIOD_ID, resources.getString(R.string.currentGradingPeriod))
            val allGradingPeriods = listOf(currentGradingPeriod).plus(gradingPeriods)
            GradingPeriodSelectorItemViewModel(allGradingPeriods, currentGradingPeriod, resources)
            { index -> _events.postValue(Event(GradesAction.OpenGradingPeriodsDialog(allGradingPeriods, index))) }
        } else {
            null
        }
    }

    private fun createGradeRowViewModels(courses: List<Course>): List<GradeRowItemViewModel> {
        return courses
            .map {
                val enrollment = it.enrollments?.first()
                val grades = it.getCourseGrade(false)
                val restrictQuantitativeData = it.settings?.restrictQuantitativeData ?: false
                val notGraded = (enrollment?.currentGradingPeriodId ?: 0L) != 0L
                val hideGrades = grades?.isLocked.orDefault()
                GradeRowItemViewModel(resources,
                    GradeRowViewData(
                        it.id,
                        it.name,
                        colorKeeper.getOrGenerateColor(it),
                        it.imageUrl ?: "",
                        if (hideGrades) 0.0 else grades?.currentScore,
                        createGradeText(
                            grades?.currentScore,
                            grades?.currentGrade,
                            hideGrades,
                            notGraded = notGraded,
                            restrictQuantitativeData = restrictQuantitativeData
                        ),
                        hideProgress = restrictQuantitativeData || notGraded || hideGrades)
                ) { gradeRowClicked(it) }
            }
    }

    private fun createViewData(gradeRowItems: List<GradeRowItemViewModel>): GradesViewData {
        val items = mutableListOf<ItemViewModel>()

        if (gradingPeriodsViewModel != null && gradingPeriodsViewModel!!.isNotEmpty() && gradeRowItems.isNotEmpty()) {
            items.add(gradingPeriodsViewModel!!)
        }
        items.addAll(gradeRowItems)

        return GradesViewData(items)
    }

    private fun createGradeText(score: Double?, grade: String?, hideFinalGrades: Boolean, notGraded: Boolean = true, restrictQuantitativeData: Boolean = true): String {
        return when {
            hideFinalGrades -> "--"
            !grade.isNullOrEmpty() -> grade
            else -> {
                val currentScoreRounded = score?.roundToInt()
                when {
                    currentScoreRounded != null && !restrictQuantitativeData -> "$currentScoreRounded%"
                    notGraded -> resources.getString(R.string.notGraded)
                    else -> "--"
                }
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

    private fun gradeRowClicked(course: Course) {
        _events.postValue(Event(GradesAction.OpenCourseGrades(course)))
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        val gradingPeriodId = gradingPeriodsViewModel?.selectedGradingPeriod?.id ?: CURRENT_GRADING_PERIOD_ID
        if (gradingPeriodId == CURRENT_GRADING_PERIOD_ID) {
            loadData(true)
        } else {
            loadDataForGradingPeriod(gradingPeriodId, null, true)
        }
    }

    fun gradingPeriodSelected(gradingPeriod: GradingPeriod) {
        if (gradingPeriodsViewModel?.selectedGradingPeriod != gradingPeriod) {
            val previouslySelectedGradingPeriod = gradingPeriodsViewModel?.selectedGradingPeriod
            gradingPeriodsViewModel?.selectedGradingPeriod = gradingPeriod
            gradingPeriodsViewModel?.notifyChange()

            _state.postValue(ViewState.Refresh)
            if (gradingPeriod.id == CURRENT_GRADING_PERIOD_ID) {
                loadData(false, previouslySelectedGradingPeriod)
            } else {
                loadDataForGradingPeriod(gradingPeriod.id, previouslySelectedGradingPeriod, false)
            }
        }
    }

    private fun loadDataForGradingPeriod(id: Long, previouslySelectedGradingPeriod: GradingPeriod?, forceNetwork: Boolean) {
        viewModelScope.launch {
            try {
                val enrollments = enrollmentManager.getEnrollmentsForGradingPeriodAsync(id, forceNetwork).await().dataOrThrow

                val gradeRowItems = createGradeRowsForGradingPeriod(enrollments)
                val viewData = createViewData(gradeRowItems)

                _state.postValue(ViewState.Success)
                _data.postValue(viewData)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error())
                _events.postValue(Event(GradesAction.ShowGradingPeriodError))

                if (previouslySelectedGradingPeriod != null) {
                    gradingPeriodsViewModel?.selectedGradingPeriod = previouslySelectedGradingPeriod
                    gradingPeriodsViewModel?.notifyChange()
                }
            }
        }
    }

    private fun createGradeRowsForGradingPeriod(enrollments: List<Enrollment>): List<GradeRowItemViewModel> {
        val enrollmentsMap = enrollments.associateBy { it.courseId }
        return courses
            .map { createGradeRowFromEnrollment(it, enrollmentsMap[it.id]) }
    }

    private fun createGradeRowFromEnrollment(course: Course, enrollment: Enrollment?): GradeRowItemViewModel {
        val restrictQuantitativeData = course.settings?.restrictQuantitativeData ?: false
        val gradeRowViewData = GradeRowViewData(
            course.id,
            course.name,
            colorKeeper.getOrGenerateColor(course),
            course.imageUrl ?: "",
            enrollment?.grades?.currentScore,
            createGradeText(enrollment?.grades?.currentScore, enrollment?.grades?.currentGrade, course.hideFinalGrades, restrictQuantitativeData),
            restrictQuantitativeData || course.hideFinalGrades)

        return GradeRowItemViewModel(resources, gradeRowViewData) { gradeRowClicked(course) }
    }
}