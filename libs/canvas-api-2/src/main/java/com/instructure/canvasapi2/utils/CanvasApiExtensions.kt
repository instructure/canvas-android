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

import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.text.SimpleDateFormat
import java.util.*

@JvmOverloads
fun Date?.toApiString(timeZone: TimeZone? = null): String {
    this ?: return ""

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    timeZone?.let { format.timeZone = it }
    val formatted = format.format(this)
    return formatted.substring(0, 22) + ":" + formatted.substring(22)
}

fun OffsetDateTime?.toApiString(): String? {
    this ?: return null
    return DateTimeFormatter.ISO_INSTANT.format(this.truncatedTo(ChronoUnit.SECONDS))
}

fun LocalDate?.toApiString(): String? {
    this ?: return null
    return DateTimeFormatter.ISO_LOCAL_DATE.format(this)
}

fun String?.toDate(): Date? {
    this ?: return null

    return try {
        var s = this.replace("Z", "+00:00")
        s = s.substring(0, 22) + s.substring(23)
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(s)
    } catch (e: Exception) {
        null
    }
}

fun String?.toSimpleDate(): Date? {
    this ?: return null

    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun MutableList<*>?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()
