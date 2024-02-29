package com.instructure.pandautils.features.calendarevent.details

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.utils.ThemePrefs


data class EventUiState(
    val toolbarColor: Int = ThemePrefs.primaryColor,
    val calendar: String = "",
    val modifyAllowed: Boolean = false,
    val loading: Boolean = false,
    val loadError: String? = null,
    val title: String = "",
    val date: String = "",
    val recurrence: String = "",
    val location: String = "",
    val address: String = "",
    val formattedDescription: String = "",
)

sealed class EventAction {
    data class OnLtiClicked(val url: String) : EventAction()
    data object DeleteEvent : EventAction()
    data object EditEvent : EventAction()
}

sealed class EventViewModelAction {
    data class OpenLtiScreen(val url: String) : EventViewModelAction()
    data class RefreshCalendarDay(val date: String) : EventViewModelAction()
    data class OpenEditEvent(val scheduleItem: ScheduleItem) : EventViewModelAction()
}
