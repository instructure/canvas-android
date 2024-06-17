package com.instructure.parentapp.features.courses.list

import android.graphics.Color
import androidx.annotation.ColorInt


data class CourseListUiState(
    val loading: Boolean = false,
    val loadError: Boolean = false,
    val courseListItems: List<CourseListItemUiState> = emptyList(),
    @ColorInt val studentColor: Int = Color.BLACK
)

data class CourseListItemUiState(
    val courseId: Long,
    val courseName: String = "",
    val courseCode: String = "",
    val grade: String = ""
)

sealed class CourseListAction {
    data class CourseTapped(val courseId: Long) : CourseListAction()
    data object Refresh : CourseListAction()
}

sealed class CourseListViewModelAction {
    data class NavigateToCourseDetails(val navigationUrl: String) : CourseListViewModelAction()
}
