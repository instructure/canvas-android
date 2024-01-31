/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.calendar

import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.CanvasContext
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale

data class CalendarUiState(
    val selectedDay: LocalDate,
    val expanded: Boolean,
    val calendarEventsUiState: CalendarEventsUiState = CalendarEventsUiState(),
    val eventIndicators: Map<LocalDate, Int> = emptyMap(),
    val snackbarMessage: String? = null
) {
    val headerUiState: CalendarHeaderUiState
        get() {
            val month = selectedDay.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val year = selectedDay.year.toString()
            return CalendarHeaderUiState(month, year)
        }

    val bodyUiState: CalendarBodyUiState
        get() {
            val dateFieldToAdd = if (expanded) ChronoUnit.MONTHS else ChronoUnit.WEEKS
            val previousPage =
                createCalendarPageUiState(selectedDay.minus(1, dateFieldToAdd), expanded)
            val currentPage = createCalendarPageUiState(selectedDay, expanded)
            val nextPage = createCalendarPageUiState(selectedDay.plus(1, dateFieldToAdd), expanded)
            return CalendarBodyUiState(previousPage, currentPage, nextPage)
        }

    private fun createCalendarPageUiState(
        date: LocalDate,
        fullMonth: Boolean
    ): CalendarPageUiState {
        val daysInMonth = date.lengthOfMonth()
        val firstDayOfMonth = date.withDayOfMonth(1)
        val firstDayOfWeekIndex =
            firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 6 for Saturday

        val calendarRows = mutableListOf<CalendarRowUiState>()
        val currentWeek = mutableListOf<CalendarDayUiState>()

        // Fill the previous month's days if the first day of the month is not Sunday
        if (firstDayOfWeekIndex > 0) {
            val previousMonth = date.minusMonths(1).month
            val previousMonthYear = date.minusMonths(1).year
            val previousMonthFirstVisibleDay =
                firstDayOfMonth.minusDays(firstDayOfWeekIndex.toLong())
            val previousMonthDays = previousMonthFirstVisibleDay.dayOfMonth
            for (day in (previousMonthDays) until previousMonthDays + firstDayOfWeekIndex) {
                val dateForDay = LocalDate.of(previousMonthYear, previousMonth, day)
                currentWeek.add(CalendarDayUiState(day, dateForDay, enabled = false, eventIndicators[dateForDay] ?: 0))
            }
        }

        // Fill the current month's days
        for (day in 1..daysInMonth) {
            val dateForDay = LocalDate.of(date.year, date.month, day)
            val enabled =
                dateForDay.dayOfWeek != DayOfWeek.SUNDAY && dateForDay.dayOfWeek != DayOfWeek.SATURDAY
            currentWeek.add(CalendarDayUiState(day, dateForDay, enabled, eventIndicators[dateForDay] ?: 0))
            if (currentWeek.size == 7) {
                calendarRows.add(CalendarRowUiState(currentWeek.toList()))
                currentWeek.clear()
            }
        }

        // Fill the next month's days if the last day of the month is not Saturday
        if (currentWeek.isNotEmpty()) {
            val nextMonth = date.plusMonths(1).month
            val nextMonthYear = date.plusMonths(1).year
            val daysToAdd = 7 - currentWeek.size
            for (day in 1..daysToAdd) {
                val dateForDay = LocalDate.of(nextMonthYear, nextMonth, day)
                currentWeek.add(CalendarDayUiState(day, dateForDay, enabled = false, eventIndicators[dateForDay] ?: 0))
            }
            calendarRows.add(CalendarRowUiState(currentWeek.toList()))
        }

        val finalCalendarRows =
            if (fullMonth) calendarRows else calendarRows.filter { it.days.any { day -> day.date == date } }

        return CalendarPageUiState(finalCalendarRows)
    }
}

data class CalendarHeaderUiState(val monthTitle: String, val yearTitle: String)

data class CalendarBodyUiState(
    val previousPage: CalendarPageUiState,
    val currentPage: CalendarPageUiState,
    val nextPage: CalendarPageUiState
)

data class CalendarPageUiState(val calendarRows: List<CalendarRowUiState>)

data class CalendarRowUiState(val days: List<CalendarDayUiState>)

data class CalendarDayUiState(
    val dayNumber: Int,
    val date: LocalDate = LocalDate.now(),
    val enabled: Boolean = true,
    val indicatorCount: Int = 0
) {
    val today: Boolean
        get() {
            val today = LocalDate.now()
            return date.isEqual(today)
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
    val status: String? = null
)

sealed class CalendarAction {
    data object ExpandChanged : CalendarAction()
    data object ExpandDisabled : CalendarAction()
    data class DaySelected(val selectedDay: LocalDate) : CalendarAction()
    data object TodayTapped : CalendarAction()
    data class PageChanged(val offset: Int) : CalendarAction()
    data class EventPageChanged(val offset: Int) : CalendarAction()
    data class EventSelected(val id: Long): CalendarAction()
    data class RefreshDay(val date: LocalDate): CalendarAction()
    data object Retry : CalendarAction()
    data object SnackbarDismissed : CalendarAction()
}

sealed class CalendarViewModelAction {
    data class OpenAssignment(val canvasContext: CanvasContext, val assignmentId: Long): CalendarViewModelAction()
    data class OpenDiscussion(val canvasContext: CanvasContext, val discussionId: Long): CalendarViewModelAction()
    data class OpenQuiz(val canvasContext: CanvasContext, val htmlUrl: String): CalendarViewModelAction()
    data class OpenCalendarEvent(val canvasContext: CanvasContext, val eventId: Long): CalendarViewModelAction()
}