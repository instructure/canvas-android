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
 *
 */
package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.utils.DateHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class DateHelperTest {

    @Test
    fun `parses canvadocs timestamp with millis and Z`() {
        val date = DateHelper.stringToDateWithMillis("2026-07-24T10:00:00.000Z")
        assertEquals(Date(1784887200000L), date)
    }

    @Test
    fun `parses Canvas REST timestamp without millis and Z`() {
        val date = DateHelper.stringToDateWithMillis("2026-08-12T10:00:00Z")
        assertEquals(Date(1786528800000L), date)
    }

    @Test
    fun `parses timestamp with microsecond precision`() {
        val date = DateHelper.stringToDateWithMillis("2026-07-24T10:00:00.123456Z")
        assertEquals(Date(1784887200123L), date)
    }

    @Test
    fun `parses timestamp with numeric offset and no millis`() {
        val date = DateHelper.stringToDateWithMillis("2026-07-24T12:00:00+02:00")
        assertEquals(Date(1784887200000L), date)
    }

    @Test
    fun `parses timestamp with numeric offset and millis`() {
        val date = DateHelper.stringToDateWithMillis("2026-07-24T12:00:00.500-05:00")
        assertEquals(Date(1784912400500L), date)
    }

    @Test
    fun `returns null for null input`() {
        assertNull(DateHelper.stringToDateWithMillis(null))
    }

    @Test
    fun `returns null for empty string`() {
        assertNull(DateHelper.stringToDateWithMillis(""))
    }

    @Test
    fun `returns null for blank string`() {
        assertNull(DateHelper.stringToDateWithMillis("   "))
    }

    @Test
    fun `returns null for garbage input`() {
        assertNull(DateHelper.stringToDateWithMillis("not a date"))
    }

    @Test
    fun `returns null for date only without time or offset`() {
        assertNull(DateHelper.stringToDateWithMillis("2026-07-24"))
    }

    @Test
    fun `returns null for date time without offset`() {
        assertNull(DateHelper.stringToDateWithMillis("2026-07-24T10:00:00"))
    }

    @Test
    fun `distinct Z-form timestamps parse to different instants`() {
        val first = DateHelper.stringToDateWithMillis("2026-07-24T10:00:00.000Z")
        val second = DateHelper.stringToDateWithMillis("2026-07-24T10:00:01.000Z")
        assertNotEquals(first, second)
        assertEquals(1000L, second!!.time - first!!.time)
    }

    @Test
    fun `returns null instead of throwing for a year too large to fit in epoch millis`() {
        // OffsetDateTime.parse succeeds on this input, but toInstant().toEpochMilli() overflows with
        // an ArithmeticException; the helper must catch that too, not just DateTimeParseException.
        assertNull(DateHelper.stringToDateWithMillis("+999999999-12-31T23:59:59Z"))
    }

    @Test
    fun `returns null for RFC-822 style offset without colon`() {
        // Deliberate narrowing: ISO_OFFSET_DATE_TIME requires the +HH:MM form. The old
        // SimpleDateFormat implementation accepted "+0000" (with mangled millis); Canvas emits "...Z"
        // and canvadocs "....000Z", so no endpoint is known to send this form.
        assertNull(DateHelper.stringToDateWithMillis("2026-07-24T10:00:00.000+0000"))
    }

    @Test
    fun `parses timestamp with lowercase z`() {
        val date = DateHelper.stringToDateWithMillis("2026-07-24T10:00:00.000z")
        assertEquals(Date(1784887200000L), date)
    }
}
