package com.instructure.parentapp.features.courses.list

import android.graphics.Color
import androidx.annotation.ColorInt


data class CoursesUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val courseListItems: List<CourseListItemUiState> = emptyList(),
    @ColorInt val studentColor: Int = Color.BLACK
) {
    val isEmpty = courseListItems.isEmpty() && !isLoading
}

data class CourseListItemUiState(
    val courseId: Long,
    val courseName: String = "",
    val courseCode: String? = null,
    val grade: String? = null
)

sealed class CoursesAction {
    data class CourseTapped(val courseId: Long) : CoursesAction()
    data object Refresh : CoursesAction()
}

sealed class CoursesViewModelAction {
    data class NavigateToCourseDetails(val navigationUrl: String) : CoursesViewModelAction()
}
