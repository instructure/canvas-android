package com.instructure.pandautils.features.calendarevent.details

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.utils.ThemePrefs


data class EventUiState(
    val toolbarUiState: ToolbarUiState = ToolbarUiState(),
    val loading: Boolean = false,
    val loadError: String? = null,
    val title: String = "",
    val date: String = "",
    val recurrence: String = "",
    val location: String = "",
    val address: String = "",
    val formattedDescription: String = "",
)

data class ToolbarUiState(
    val toolbarColor: Int = ThemePrefs.primaryColor,
    val subtitle: String = "",
    val modifyAllowed: Boolean = false
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
