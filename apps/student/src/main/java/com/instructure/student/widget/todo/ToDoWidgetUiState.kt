package com.instructure.student.widget.todo

import androidx.annotation.DrawableRes
import com.instructure.student.widget.glance.WidgetState
import org.threeten.bp.LocalDate


data class ToDoWidgetUiState(
    val state: WidgetState,
    val plannerItems: List<WidgetPlannerItem> = emptyList()
)

data class WidgetPlannerItem(
    val date: LocalDate,
    @DrawableRes val iconRes: Int,
    val canvasContextColor: Int,
    val canvasContextText: String,
    val title: String,
    val dateText: String,
    val url: String
)
