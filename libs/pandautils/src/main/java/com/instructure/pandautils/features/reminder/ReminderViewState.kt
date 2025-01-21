package com.instructure.pandautils.features.reminder

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.instructure.pandautils.R
import java.util.Date

data class ReminderViewState(
    val reminders: List<ReminderItem> = emptyList(),
    val dueDate: Date? = null,
    val themeColor: Color? = null,
) {
    fun getThemeColor(context: Context): Color {
        return themeColor ?: Color(context.getColor(R.color.textDarkest))
    }
}

data class ReminderItem(
    val id: Long,
    val title: String,
    val date: Date
)

