package com.instructure.parentapp.features.courses.list

import android.graphics.Color
import androidx.annotation.ColorInt


data class CoursesUiState(
    val loading: Boolean = false,
    val loadError: Boolean = false,
    val courseListItems: List<CourseItemUiState> = emptyList(),
    @ColorInt val studentColor: Int = Color.BLACK
)

data class CourseItemUiState(
    val courseId: Long,
    val courseName: String = "",
    val courseCode: String = "",
    val grade: String = ""
)

sealed class CoursesAction {
    data class CourseTapped(val courseId: Long) : CoursesAction()
    data object Refresh : CoursesAction()
}

sealed class CoursesViewModelAction {
    data class NavigateToCourseDetails(val navigationUrl: String) : CoursesViewModelAction()
}
