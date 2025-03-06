package com.instructure.pandautils.features.calendarevent.details

import android.content.Context
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.reminder.ReminderViewState
import com.instructure.pandautils.utils.ThemePrefs
import org.threeten.bp.LocalDate


data class EventUiState(
    val toolbarUiState: ToolbarUiState = ToolbarUiState(),
    val reminderUiState: ReminderViewState = ReminderViewState(),
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
    val errorSnack: String? = null,
    val isMessageFabEnabled: Boolean = false
)

data class ToolbarUiState(
    val toolbarColor: Int = ThemePrefs.primaryColor,
    val subtitle: String = "",
    val editAllowed: Boolean = false,
    val deleteAllowed: Boolean = false,
    val deleting: Boolean = false,
    val isUserContext: Boolean = false,
)

sealed class EventAction {
    data class OnLtiClicked(val url: String) : EventAction()
    data class DeleteEvent(val deleteScope: CalendarEventAPI.ModifyEventScope) : EventAction()
    data object EditEvent : EventAction()
    data object SnackbarDismissed : EventAction()
    data object OnReminderAddClicked : EventAction()
    data class OnReminderDeleteClicked(val context: Context, val reminderId: Long) : EventAction()
    data object OnMessageFabClicked : EventAction()
}

sealed class EventViewModelAction {
    data class OpenLtiScreen(val url: String) : EventViewModelAction()
    data class RefreshCalendarDays(val days: List<LocalDate>) : EventViewModelAction()
    data object RefreshCalendar : EventViewModelAction()
    data class OpenEditEvent(val scheduleItem: ScheduleItem) : EventViewModelAction()
    data object OnReminderAddClicked : EventViewModelAction()
    data class NavigateToComposeMessageScreen(val options: InboxComposeOptions) : EventViewModelAction()
}
