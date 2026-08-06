/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.features.submission.domain

import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Date

/**
 * Parses a canvadocs ISO-8601 `createdAt` (e.g. `2026-07-24T10:00:00.000Z`) into a [Date].
 *
 * Deliberately NOT [com.instructure.canvasapi2.utils.DateHelper.stringToDateWithMillis], which cannot
 * parse this format: it strips index 22 of the string — the offset's `:` only in the *no-millis* form —
 * while its `SimpleDateFormat` pattern requires `.SSS`, so with millis present it mangles the
 * milliseconds instead and the `ParseException` is swallowed into a `null`. Every caller of it therefore
 * renders a blank timestamp (classic Student/Teacher annotation comments included). Fixing that shared
 * helper is out of scope here — it is also used by Inbox — so this parses locally.
 *
 * [OffsetDateTime] handles both `Z` and numeric offsets, and any number of fractional-second digits.
 * minSdk is 28, so `java.time` needs no desugaring.
 *
 * @return the parsed date, or `null` if [raw] is null/blank or not a valid offset date-time.
 */
fun parseCanvaDocsTimestamp(raw: String?): Date? {
    if (raw.isNullOrBlank()) return null
    return try {
        Date(OffsetDateTime.parse(raw).toInstant().toEpochMilli())
    } catch (e: DateTimeParseException) {
        null
    }
}
