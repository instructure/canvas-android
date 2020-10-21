/*
 * Copyright (C) 2019 - present Instructure, Inc.
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

import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder

fun OffsetDateTime.getShortMonthAndDay(): String {
    // Get year if the year of the due date isn't the current year
    val pattern = if (LocalDate.now().year != this.year) DateTimeFormatter.ofPattern("MMM d, Y") else DateTimeFormatter.ofPattern("MMM d")
    return format(pattern)
}

fun OffsetDateTime.getTime(): String {
    val pattern = DateTimeFormatterBuilder().appendPattern("h:mm a").toFormatter()
    return format(pattern).toLowerCase()
}