package com.instructure.student.widget.grades

import androidx.annotation.ColorInt


data class WidgetCourseItem(
    val name: String,
    val courseCode: String,
    val isLocked: Boolean,
    val gradeText: String?,
    @ColorInt val courseColorLight: Int,
    @ColorInt val courseColorDark: Int,
    val url: String
)
