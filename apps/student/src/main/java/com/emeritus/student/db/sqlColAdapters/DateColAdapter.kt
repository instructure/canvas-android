/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */
package com.emeritus.student.db.sqlColAdapters

import com.squareup.sqldelight.ColumnAdapter
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter


typealias Date = OffsetDateTime

/**
 * Expectation is that the database will hold whatever datetime the server gives back to us.
 * When we pull it out, it will be converted to a local [OffsetDateTime].
 * When we put something in, it will be converted to a UTC-based ISO-8601 datetime (what the server currently uses).
 */
class DateAdapter : ColumnAdapter<Date, String> {
    override fun decode(databaseValue: String) = Date.parse(databaseValue).withOffsetSameInstant(OffsetDateTime.now().offset) as Date
    override fun encode(value: Date): String = value.toApiString()
}

/**
 * Turns a local OffsetDateTime to a UTC-based ISO-8601 Datetime with timezone string
 */
fun OffsetDateTime?.toApiString(): String {
    this ?: return ""
    val utcTime = this.withOffsetSameInstant(ZoneOffset.UTC)
    return utcTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}