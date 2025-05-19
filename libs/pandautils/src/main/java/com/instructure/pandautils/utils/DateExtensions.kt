/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.DateTimeParseException
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun OffsetDateTime.getShortMonthAndDay(): String {
    // Get year if the year of the due date isn't the current year
    val pattern = if (LocalDate.now().year != this.year) DateTimeFormatter.ofPattern("MMM d, Y") else DateTimeFormatter.ofPattern("MMM d")
    return format(pattern)
}

fun OffsetDateTime.getTime(): String {
    val pattern = DateTimeFormatterBuilder().appendPattern("h:mm a").toFormatter()
    return format(pattern).lowercase(Locale.getDefault())
}

fun Date.isSameDay(date: Date?): Boolean {
    if (date == null) return false
    val calendar1: Calendar = Calendar.getInstance()
    calendar1.time = this
    val calendar2: Calendar = Calendar.getInstance()
    calendar2.time = date
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
}

fun Date.isNextDay(date: Date?): Boolean {
    if (date == null) return false
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    return calendar.time.isSameDay(this)
}

fun Date.isPreviousDay(date: Date?): Boolean {
    if (date == null) return false
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    return calendar.time.isSameDay(this)
}

fun Date.getLastSunday(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_WEEK, -(calendar.get(Calendar.DAY_OF_WEEK) - 1))
    return calendar.time
}

fun Date.getNextSaturday(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_WEEK, Calendar.SATURDAY - calendar.get(Calendar.DAY_OF_WEEK))
    return calendar.time
}

fun Date.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this.time).atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Date.toLocalTime(): LocalTime {
    return Instant.ofEpochMilli(this.time).atZone(ZoneId.systemDefault()).toLocalTime()
}

fun Date.toFormattedString(): String = DateFormat.getDateTimeInstance(
    DateFormat.MEDIUM,
    DateFormat.SHORT,
    Locale.getDefault()
).format(this)

fun String.toLocalDateOrNull(formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE): LocalDate? {
    return try {
        LocalDate.parse(this, formatter)
    } catch (e: DateTimeParseException) {
        null
    }
}
