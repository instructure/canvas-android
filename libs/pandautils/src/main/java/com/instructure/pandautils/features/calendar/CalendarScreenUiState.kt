/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.calendar

import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Locale

data class CalendarScreenUiState(
    val calendarUiState: CalendarUiState,
    val calendarEventsUiState: CalendarEventsUiState = CalendarEventsUiState(),
    val snackbarMessage: String? = null,
    val showAddEventButton: Boolean = true,
    val showAppointmentGroups: Boolean = false
)

data class CalendarUiState(
    val selectedDay: LocalDate,
    val expanded: Boolean,
    val headerUiState: CalendarHeaderUiState,
    val bodyUiState: CalendarBodyUiState,
    val scrollToPageOffset: Int = 0,
    val pendingSelectedDay: LocalDate? = null, // Temporary selected date when the calendar is animating to a new month
    val todayTapped: Boolean = false
)

data class CalendarHeaderUiState(val monthTitle: String, val yearTitle: String, val loadingMonths: Boolean = false)

data class CalendarBodyUiState(
    val previousPage: CalendarPageUiState,
    val currentPage: CalendarPageUiState,
    val nextPage: CalendarPageUiState
)

data class CalendarPageUiState(
    val calendarRows: List<CalendarRowUiState>,
    val buttonContentDescription: String
)

data class CalendarRowUiState(val days: List<CalendarDayUiState>)

data class CalendarDayUiState(
    val dayNumber: Int,
    val date: LocalDate = LocalDate.now(),
    val enabled: Boolean = true,
    val indicatorCount: Int = 0,
) {
    val today: Boolean
        get() {
            val today = LocalDate.now()
            return date.isEqual(today)
        }

    val contentDescription: String = date.let {
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val day = date.dayOfMonth
        "$dayOfWeek, $month $day"
    }
}

data class CalendarEventsUiState(
    val previousPage: CalendarEventsPageUiState = CalendarEventsPageUiState(),
    val currentPage: CalendarEventsPageUiState = CalendarEventsPageUiState(),
    val nextPage: CalendarEventsPageUiState = CalendarEventsPageUiState()
)

data class CalendarEventsPageUiState(
    val date: LocalDate = LocalDate.now(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val events: List<EventUiState> = emptyList()
)

data class EventUiState(
    val plannableId: Long,
    val contextName: String,
    val canvasContext: CanvasContext,
    val name: String,
    @DrawableRes val iconRes: Int,
    val date: String? = null,
    val status: String? = null,
    val tag: String? = null,
    val isReservation: Boolean = false,
    val reservationId: Long? = null,
    val appointmentGroupId: Long? = null,
    val canCancel: Boolean = false,
    val hasConflict: Boolean = false
)

sealed class CalendarAction {
    data class ExpandChanged(val isExpanded: Boolean) : CalendarAction()
    data object ExpandDisabled : CalendarAction()
    data object ExpandEnabled : CalendarAction()
    data class DaySelected(val selectedDay: LocalDate) : CalendarAction()
    data object TodayTapped : CalendarAction()
    data class PageChanged(val offset: Int) : CalendarAction()
    data class EventPageChanged(val offset: Int) : CalendarAction()
    data class EventSelected(val id: Long): CalendarAction()
    data class RefreshDay(val date: LocalDate): CalendarAction()
    data object Retry : CalendarAction()
    data object SnackbarDismissed : CalendarAction()
    data object HeightAnimationFinished : CalendarAction()
    data object AddToDoTapped : CalendarAction()
    data object FilterTapped : CalendarAction()
    data object FiltersRefreshed : CalendarAction()
    data object AddEventTapped : CalendarAction()
    data object RefreshCalendar : CalendarAction()
    data object PullToRefresh : CalendarAction()
    data object TodayTapHandled : CalendarAction()
    data class CancelReservation(val reservationId: Long, val appointmentGroupId: Long) : CalendarAction()
    data class ReserveAppointmentSlot(val slotId: Long) : CalendarAction()
    data class AppointmentGroupsToggled(val isEnabled: Boolean) : CalendarAction()
}

sealed class CalendarViewModelAction {
    data class OpenAssignment(val canvasContext: CanvasContext, val assignmentId: Long): CalendarViewModelAction()
    data class OpenDiscussion(val canvasContext: CanvasContext, val discussionId: Long, val assignmentId: Long?): CalendarViewModelAction()
    data class OpenQuiz(val canvasContext: CanvasContext, val htmlUrl: String): CalendarViewModelAction()
    data class OpenCalendarEvent(val canvasContext: CanvasContext, val eventId: Long): CalendarViewModelAction()
    data class OpenToDo(val plannerItem: PlannerItem) : CalendarViewModelAction()
    data class OpenCreateToDo(val initialDateString: String?) : CalendarViewModelAction()
    data object OpenFilters : CalendarViewModelAction()
    data class OpenCreateEvent(val initialDateString: String?) : CalendarViewModelAction()
}

sealed class SharedCalendarAction(val delay: Long = 0L) {
    data class RefreshDays(val days: List<LocalDate>) : SharedCalendarAction(delay = 50)
    data object RefreshCalendar : SharedCalendarAction(delay = 50)
    data class FiltersClosed(val changed: Boolean) : SharedCalendarAction()
    data object CloseToDoScreen : SharedCalendarAction()
    data object CloseEventScreen : SharedCalendarAction()
    data class TodayButtonVisible(val visible: Boolean) : SharedCalendarAction()
    data object TodayButtonTapped : SharedCalendarAction()
}