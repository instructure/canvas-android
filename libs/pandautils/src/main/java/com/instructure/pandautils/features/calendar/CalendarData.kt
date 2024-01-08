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
import java.util.Locale

data class CalendarData(val months: List<MonthData>)

data class MonthData(
    val year: String,
    val name: String,
    val calendarRows: List<CalendarRow>
)

data class CalendarRow(val days: List<Day>)

data class Day(val dayNumber: Int, val date: LocalDate = LocalDate.now(), val enabled: Boolean = true) {
    val today: Boolean
        get() {
            val today = LocalDate.now()
            return date.isEqual(today)
        }
} // TODO remove this default value once we don't need the old dummy data

data class Event(val name: String, val course: String, val dateString: String)

fun createMonthDataForLocalDate(date: LocalDate): MonthData {
    val year = date.year
    val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) // TODO maybe we need to get the locale from other source if app locale is overwritten
    val daysInMonth = date.lengthOfMonth()
    val firstDayOfMonth = date.withDayOfMonth(1)
    val firstDayOfWeekIndex = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 6 for Saturday

    val calendarRows = mutableListOf<CalendarRow>()
    val currentWeek = mutableListOf<Day>()

    // Fill the previous month's days if the first day of the month is not Sunday
    if (firstDayOfWeekIndex > 0) {
        val previousMonth = date.minusMonths(1).month
        val previousMonthYear = date.minusMonths(1).year
        val previousMonthFirstVisibleDay = firstDayOfMonth.minusDays(firstDayOfWeekIndex.toLong())
        val previousMonthDays = previousMonthFirstVisibleDay.dayOfMonth
        for (day in (previousMonthDays)until previousMonthDays + firstDayOfWeekIndex) {
            currentWeek.add(Day(day, LocalDate.of(previousMonthYear, previousMonth, day), enabled = false))
        }
    }

    // Fill the current month's days
    for (day in 1..daysInMonth) {
        val dateForDay = LocalDate.of(year, date.month, day)
        val enabled = dateForDay.dayOfWeek != DayOfWeek.SUNDAY && dateForDay.dayOfWeek != DayOfWeek.SATURDAY
        currentWeek.add(Day(day, dateForDay, enabled))
        if (currentWeek.size == 7) {
            calendarRows.add(CalendarRow(currentWeek.toList()))
            currentWeek.clear()
        }
    }

    // Fill the next month's days if the last day of the month is not Saturday
    if (currentWeek.isNotEmpty()) {
        val nextMonth = date.plusMonths(1).month
        val nextMonthYear = date.plusMonths(1).year
        val daysToAdd = 7 - currentWeek.size
        for (day in 1..daysToAdd) {
            currentWeek.add(Day(day, LocalDate.of(nextMonthYear, nextMonth, day), enabled = false))
        }
        calendarRows.add(CalendarRow(currentWeek.toList()))
    }

    return MonthData(year.toString(), month, calendarRows)
}