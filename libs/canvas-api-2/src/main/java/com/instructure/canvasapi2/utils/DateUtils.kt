/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.canvasapi2.utils

import android.content.Context
import com.instructure.pandares.R
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Creates an [Instant] from this [Date] */
val Date.instant: Instant get() = DateTimeUtils.toInstant(this)

/** Creates a [ZonedDateTime] from this [Date] using the default [ZoneId] */
val Date.zonedDateTime: ZonedDateTime get() = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

/**
 * Converts a [LocalDateTime] to an API-compatible ISO 8601 string in UTC.
 */
fun LocalDateTime?.toApiString(): String? {
    this ?: return null
    val zonedDateTime = this.atZone(java.time.ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
    return DateTimeFormatter.ISO_INSTANT.format(zonedDateTime.truncatedTo(ChronoUnit.SECONDS))
}

/**
 * Formats a date relative to today, showing "Today" or "Tomorrow" when applicable,
 * or a full date otherwise.
 */
fun Date.formatRelativeWithTime(context: Context): String {
    val now = Date()
    val calendar = Calendar.getInstance()

    calendar.time = now
    val todayStart = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    calendar.time = now
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowStart = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    calendar.time = now
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowEnd = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val dateMillis = this.time
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())

    return when {
        dateMillis >= todayStart && dateMillis < tomorrowStart -> "${context.getString(R.string.today)}, ${timeFormat.format(this)}"
        dateMillis >= tomorrowStart && dateMillis <= tomorrowEnd -> "${context.getString(R.string.tomorrow)}, ${timeFormat.format(this)}"
        else -> dateFormat.format(this)
    }
}
