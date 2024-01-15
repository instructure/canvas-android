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

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

data class CalendarUiState(
    val selectedDay: LocalDate,
    val expanded: Boolean
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
                currentWeek.add(CalendarDayUiState(day, LocalDate.of(previousMonthYear, previousMonth, day), enabled = false))
            }
        }

        // Fill the current month's days
        for (day in 1..daysInMonth) {
            val dateForDay = LocalDate.of(date.year, date.month, day)
            val enabled =
                dateForDay.dayOfWeek != DayOfWeek.SUNDAY && dateForDay.dayOfWeek != DayOfWeek.SATURDAY
            currentWeek.add(CalendarDayUiState(day, dateForDay, enabled))
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
                currentWeek.add(CalendarDayUiState(day, LocalDate.of(nextMonthYear, nextMonth, day), enabled = false))
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
    val enabled: Boolean = true
) {
    val today: Boolean
        get() {
            val today = LocalDate.now()
            return date.isEqual(today)
        }
}

sealed class CalendarAction {
    data object ExpandChanged : CalendarAction()
    data object ExpandDisabled : CalendarAction()
    data class DaySelected(val selectedDay: LocalDate) : CalendarAction()
    data object TodayTapped : CalendarAction()

    data class PageChanged(val offset: Int) : CalendarAction()
}