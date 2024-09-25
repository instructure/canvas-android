package com.instructure.parentapp.features.courses.details

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.instructure.parentapp.R


data class CourseDetailsUiState(
    val courseName: String = "",
    @ColorInt val studentColor: Int = Color.BLACK,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val tabs: List<TabType> = emptyList()
)

enum class TabType(@StringRes val labelRes: Int) {
    GRADES(R.string.courseGradesLabel),
    FRONT_PAGE(R.string.courseFrontPageLabel),
    SYLLABUS(R.string.courseSyllabusLabel),
    SUMMARY(R.string.courseSummaryLabel)
}

sealed class CourseDetailsAction {
    data object Refresh : CourseDetailsAction()
}
