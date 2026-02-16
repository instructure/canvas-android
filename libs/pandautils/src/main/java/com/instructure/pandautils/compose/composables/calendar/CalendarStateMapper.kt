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
package com.instructure.pandautils.compose.composables.calendar

import org.threeten.bp.Clock
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.WeekFields
import java.util.Locale

class CalendarStateMapper(private val clock: Clock) {

    fun createHeaderUiState(selectedDay: LocalDate, pendingSelectedDay: LocalDate?, loading: Boolean = false): CalendarHeaderUiState {
        val dayToShow = pendingSelectedDay ?: selectedDay
        val month = dayToShow.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = dayToShow.year.toString()
        return CalendarHeaderUiState(month, year, loading)
    }

    fun createBodyUiState(
        expanded: Boolean,
        selectedDay: LocalDate,
        jumpToToday: Boolean = false,
        scrollToPageOffset: Int = 0,
        eventIndicators: Map<LocalDate, Int> = emptyMap()
    ): CalendarBodyUiState {
        val dateFieldToAdd = if (expanded) ChronoUnit.MONTHS else ChronoUnit.WEEKS

        val previousPageDate = if (jumpToToday && scrollToPageOffset < 0) LocalDate.now(clock) else selectedDay.minus(1, dateFieldToAdd)
        val nextPageDate = if (jumpToToday && scrollToPageOffset > 0) LocalDate.now(clock) else selectedDay.plus(1, dateFieldToAdd)

        val previousPage = createCalendarPageUiState(previousPageDate, expanded, eventIndicators)
        val currentPage = createCalendarPageUiState(selectedDay, expanded, eventIndicators)
        val nextPage = createCalendarPageUiState(nextPageDate, expanded, eventIndicators)
        return CalendarBodyUiState(previousPage, currentPage, nextPage)
    }

    private fun createCalendarPageUiState(
        date: LocalDate,
        fullMonth: Boolean,
        eventIndicators: Map<LocalDate, Int>
    ): CalendarPageUiState {
        val daysInMonth = date.lengthOfMonth()
        val firstDayOfMonth = date.withDayOfMonth(1)
        val localeFirstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value
        val firstDayOfWeekIndex =
            (7 + (firstDayOfMonth.dayOfWeek.value - localeFirstDayOfWeek)) % 7 // We need to add 7 to avoid negative values

        val calendarRows = mutableListOf<CalendarRowUiState>()
        val currentWeek = mutableListOf<CalendarDayUiState>()

        // Fill the previous month's days if the first day of the month is not the first day of the week
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

        val contentDescription = if (fullMonth) {
            val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val year = date.year
            "$month $year"
        } else {
            val firstDay = finalCalendarRows.first().days.first().date
            getContentDescriptionForDate(firstDay)
        }

        return CalendarPageUiState(finalCalendarRows, contentDescription)
    }

    private fun getContentDescriptionForDate(localDate: LocalDate): String {
        val dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val month = localDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val day = localDate.dayOfMonth
        return "$dayOfWeek, $month $day"
    }
}