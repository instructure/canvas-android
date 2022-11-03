package com.instructure.student.features.dashboard.main

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.features.dashboard.main.itemviewmodels.DashboardAssignmentItemViewModel
import com.instructure.student.features.dashboard.main.itemviewmodels.DashboardCourseItemViewModel
import com.instructure.student.features.dashboard.main.itemviewmodels.DashboardGradeItemViewModel
import com.instructure.student.features.dashboard.main.itemviewmodels.DashboardWidgetItemViewModel
import com.instructure.student.util.getDisplayGrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courseManager: CourseManager,
    private val assignmentManager: AssignmentManager,
    private val userManager: UserManager,
    private val apiPrefs: ApiPrefs,
    private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<DashboardViewData>
        get() = _data
    private val _data = MutableLiveData<DashboardViewData>()

    val events: LiveData<Event<DashboardAction>>
        get() = _events
    private val _events = MutableLiveData<Event<DashboardAction>>()

    private var courseMap: Map<Long, Course> = emptyMap()
    private var assignmentMap: Map<Long, Assignment> = emptyMap()

    fun refresh() {
        loadData(true)
        _state.postValue(ViewState.Refresh)
    }

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            val courses = courseManager.getCoursesAsync(forceNetwork).await().dataOrThrow
            courseMap = courses.associateBy { it.id }

            val dashboardCards = courseManager.getDashboardCoursesAsync(forceNetwork).await().dataOrThrow

            val assignments = courses.map { assignmentManager.getAllAssignmentsAsync(it.id, forceNetwork) }
                .awaitAll()
                .mapNotNull { it.dataOrNull }
                .flatten()
            assignmentMap = assignments.associateBy { it.id }

            val courseItems = dashboardCards
                .mapNotNull { courseMap[it.id] }
                .filter { it.isCurrentEnrolment() || it.isFutureEnrolment() }
                .map {
                    DashboardCourseItemViewModel(
                        DashboardCourseItemViewData(
                            it.imageUrl,
                            it.name,
                            it.courseCode,
                            getCourseGrade(it.getCourseGrade(false)),
                            ColorKeeper.getOrGenerateColor(it)
                        )
                    ) {
                        _events.postValue(Event(DashboardAction.OpenCourse(it)))
                    }
                }

            val grades = apiPrefs.user?.id?.let {
                userManager.getGradedSubmissionsAsync(it, forceNetwork).await().dataOrNull
                    ?.map {
                        val assignment = assignmentMap[it.assignmentId]
                        DashboardGradeItemViewModel(
                            DashboardGradeItemViewData(
                                assignment?.name ?: "",
                                assignment?.getDisplayGrade(it, resources)?.text ?: "N/A",
                                courseMap[assignment?.courseId]?.name ?: "Course",
                                ColorKeeper.getOrGenerateColor(courseMap[assignment?.courseId])
                            )
                        ) {
                            it.previewUrl?.let {
                                _events.postValue(Event(DashboardAction.OpenSubmission(it)))
                            }
                                ?: _events.postValue(Event(DashboardAction.ShowToast(resources.getString(R.string.errorOccurred))))

                        }
                    }
            } ?: emptyList()

            val upcomingAssignments = assignments
                .filter { !it.isSubmitted && it.dueDate?.after(Date()) ?: false }
                .subList(0, 10)
                .map {
                    val course = courseMap[it.courseId]
                    DashboardAssignmentItemViewModel(
                        DashboardAssignmentItemViewData(
                            it.name ?: "Assignment",
                            course?.name ?: "Course",
                            ColorKeeper.getOrGenerateColor(course),
                            it.dueDate?.let { SimpleDateFormat("MMM d", Locale.getDefault()).format(it) } ?: ""
                        )
                    )
                }


            _data.postValue(
                DashboardViewData(
                    courseItems,
                    listOf(
                        DashboardWidgetItemViewModel(
                            data = DashboardWidgetItemData(resources.getString(R.string.grades)),
                            collapsable = grades.size > 3,
                            items = grades
                        ),
                        DashboardWidgetItemViewModel(
                            data = DashboardWidgetItemData(resources.getString(R.string.upcomingAssignments)),
                            collapsable = upcomingAssignments.size > 3,
                            items = upcomingAssignments
                        )
                    )
                )
            )

            _state.postValue(ViewState.Success)
        }
    }

    private fun getCourseGrade(courseGrade: CourseGrade?): String {
        return if (courseGrade == null || courseGrade.noCurrentGrade) {
            resources.getString(R.string.noGradeText)
        } else {
            val scoreString = NumberHelper.doubleToPercentage(courseGrade.currentScore, 2)
            "${if (courseGrade.hasCurrentGradeString()) courseGrade.currentGrade else ""} $scoreString"
        }
    }

    fun expandCourses() {
        _events.postValue(Event(DashboardAction.ExpandCourses))
    }
}