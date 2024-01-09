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

import java.time.LocalDate

data class CalendarData(val previous: MonthData, val current: MonthData, val next: MonthData)

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