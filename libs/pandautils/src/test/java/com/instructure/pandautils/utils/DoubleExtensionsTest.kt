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
package com.instructure.pandautils.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DoubleExtensionsTest {

    @Test
    fun `toFormattedString with no max decimals preserves full precision for fractional values`() {
        assertEquals("1.25", 1.25.toFormattedString(maxDecimals = null))
        assertEquals("3.75", 3.75.toFormattedString(maxDecimals = null))
        assertEquals("1.2", 1.2.toFormattedString(maxDecimals = null))
    }

    @Test
    fun `toFormattedString with no max decimals renders integer values without decimals`() {
        assertEquals("20", 20.0.toFormattedString(maxDecimals = null))
    }

    @Test
    fun `toFormattedString with no max decimals round-trips back to the original Double`() {
        val values = listOf(1.25, 3.75, 20.0)

        values.forEach { value ->
            val formatted = value.toFormattedString(maxDecimals = null)
            assertEquals(value, formatted.toDouble(), 0.0)
        }
    }

    @Test
    fun `String toDouble parses a decimal string back to its exact Double value`() {
        assertEquals(1.25, "1.25".toDouble(), 0.0)
    }

    @Test
    fun `stringValueWithoutTrailingZeros truncates fractional scores via banker's rounding (regression guard)`() {
        // This documents why the rubric score field must NOT be seeded from this extension:
        // it silently rounds 1.25 -> 1.2 and 3.75 -> 3.8 (HALF_EVEN, 1 fraction digit),
        // which previously caused a phantom auto-save of a truncated rubric score.
        assertEquals("1.2", 1.25.stringValueWithoutTrailingZeros)
        assertEquals("3.8", 3.75.stringValueWithoutTrailingZeros)
    }

    @Test
    fun `toFormattedStringWithoutTrailingZeros preserves up to 2 decimals without trailing zeros`() {
        assertEquals("1.25", 1.25.toFormattedStringWithoutTrailingZeros())
        assertEquals("3.75", 3.75.toFormattedStringWithoutTrailingZeros())
        assertEquals("1.2", 1.2.toFormattedStringWithoutTrailingZeros())
    }

    @Test
    fun `toFormattedStringWithoutTrailingZeros renders integer values without decimals`() {
        assertEquals("20", 20.0.toFormattedStringWithoutTrailingZeros())
        assertEquals("10", 10.0.toFormattedStringWithoutTrailingZeros())
        assertEquals("0", 0.0.toFormattedStringWithoutTrailingZeros())
    }

    @Test
    fun `toFormattedStringWithoutTrailingZeros caps at 2 decimals with rounding`() {
        assertEquals("1.33", 1.333.toFormattedStringWithoutTrailingZeros())
    }
}
