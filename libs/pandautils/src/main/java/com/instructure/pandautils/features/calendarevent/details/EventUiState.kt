package com.instructure.pandautils.features.calendarevent.details

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.utils.ThemePrefs
import org.threeten.bp.LocalDate


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
    val isSeriesEvent: Boolean = false,
    val isSeriesHead: Boolean = false,
    val errorSnack: String? = null
)

data class ToolbarUiState(
    val toolbarColor: Int = ThemePrefs.primaryColor,
    val subtitle: String = "",
    val modifyAllowed: Boolean = false,
    val deleting: Boolean = false
)

sealed class EventAction {
    data class OnLtiClicked(val url: String) : EventAction()
    data class DeleteEvent(val deleteScope: CalendarEventAPI.ModifyEventScope) : EventAction()
    data object EditEvent : EventAction()

    data object SnackbarDismissed : EventAction()
}

sealed class EventViewModelAction {
    data class OpenLtiScreen(val url: String) : EventViewModelAction()
    data class RefreshCalendarDays(val days: List<LocalDate>) : EventViewModelAction()
    data class OpenEditEvent(val scheduleItem: ScheduleItem) : EventViewModelAction()
}
