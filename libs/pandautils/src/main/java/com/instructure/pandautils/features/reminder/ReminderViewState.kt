package com.instructure.pandautils.features.reminder

import androidx.compose.ui.graphics.Color
import java.util.Date

data class ReminderViewState(
    val reminders: List<ReminderItem> = emptyList(),
    val dueDate: Date? = null,
    val themeColor: Color = Color.Black,
)

data class ReminderItem(
    val id: Long,
    val title: String,
    val date: Date
)

