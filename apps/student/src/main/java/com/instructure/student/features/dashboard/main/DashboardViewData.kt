package com.instructure.student.features.dashboard.main

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.student.features.dashboard.main.itemviewmodels.DashboardCourseItemViewModel

data class DashboardViewData(
    val courses: List<DashboardCourseItemViewModel>
)

data class DashboardCourseItemViewData(
    val imageUrl: String?,
    val courseName: String,
    val courseCode: String?,
    val grade: String,
    val courseColor: ThemedColor
)

sealed class DashboardAction {
    data class OpenCourse(val course: Course): DashboardAction()
}