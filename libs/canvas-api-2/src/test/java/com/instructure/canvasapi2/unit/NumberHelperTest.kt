/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import com.instructure.canvasapi2.utils.NumberHelper

import org.junit.Assert

import org.junit.Test

import org.junit.Assert.assertEquals

class NumberHelperTest {

    private val fileUnits = arrayOf("B", "KB", "MB", "GB", "TB")

    @Test
    fun doubleToPercentage_TestHundred() {
        val hundredPercent = "100%"
        val hundredDouble = 100.0

        Assert.assertEquals(hundredPercent, NumberHelper.doubleToPercentage(hundredDouble))
    }

    @Test
    fun doubleToPercentage_TestPercent() {
        val percent = "%"
        val percentDouble = 76.43

        assertEquals(true, NumberHelper.doubleToPercentage(percentDouble).contains(percent))
    }

    @Test
    fun doubleToPercentage_TestTwoNumbersAfterDecimal() {
        val percentString = "48.48%"
        val percentDouble = 48.48443

        assertEquals(percentString, NumberHelper.doubleToPercentage(percentDouble))
    }

    @Test
    fun doubleToPercentage_TestTwoNumbersAfterDecimalRoundUp() {
        val percentString = "86.59%"
        val percentDouble = 86.58954

        assertEquals(percentString, NumberHelper.doubleToPercentage(percentDouble))
    }

    @Test
    fun doubleToPercentage_TestLargePercentComma() {
        val percentString = "1,056.34%"
        val percentDouble = 1056.34

        assertEquals(percentString, NumberHelper.doubleToPercentage(percentDouble))
    }

    @Test
    fun formatDecimal_TwoDecimalPlaces() {
        val expected = "12,345.67"
        val input = 12345.66789
        val output = NumberHelper.formatDecimal(input, 2, false)
        assertEquals(expected, output)
    }

    @Test
    fun formatDecimal_TwoDecimalPlacesNoTrim() {
        val expected = "12,345.00"
        val input = 12345.000000
        val output = NumberHelper.formatDecimal(input, 2, false)
        assertEquals(expected, output)
    }

    @Test
    fun formatDecimal_NoDecimalPlaces() {
        val expected = "12,345"
        val input = 12345.6789
        val output = NumberHelper.formatDecimal(input, 0, false)
        assertEquals(expected, output)
    }

    @Test
    fun formatDecimal_TrimZeroWithZeroDigits() {
        val expected = "12,345"
        val input = 12345.000001
        val output = NumberHelper.formatDecimal(input, 3, true)
        assertEquals(expected, output)
    }

    @Test
    fun formatDecimal_TrimZeroWithNonZeroDigits() {
        val expected = "12,345.678"
        val input = 12345.67789
        val output = NumberHelper.formatDecimal(input, 3, true)
        assertEquals(expected, output)
    }

    @Test
    fun `readableFileSize formats negative size as zero bytes`() {
        assertEquals("0 B", NumberHelper.readableFileSize(fileUnits, -1L))
        assertEquals("0 B", NumberHelper.readableFileSize(fileUnits, -1099511627776L))
    }

    @Test
    fun `readableFileSize correctly formats bytes`() {
        assertEquals("0 B", NumberHelper.readableFileSize(fileUnits, 0L))
        assertEquals("1,023 B", NumberHelper.readableFileSize(fileUnits, 1023L))
    }

    @Test
    fun `readableFileSize correctly formats kilobytes`() {
        assertEquals("1 KB", NumberHelper.readableFileSize(fileUnits, 1024L))
        assertEquals("1,023.9 KB", NumberHelper.readableFileSize(fileUnits, 1048575L))
    }

    @Test
    fun `readableFileSize correctly formats megabytes`() {
        assertEquals("1 MB", NumberHelper.readableFileSize(fileUnits, 1048576L))
        assertEquals("1,023.9 MB", NumberHelper.readableFileSize(fileUnits, 1073741823L))
    }

    @Test
    fun `readableFileSize correctly formats gigabytes`() {
        assertEquals("1 GB", NumberHelper.readableFileSize(fileUnits, 1073741824L))
        assertEquals("1,023.9 GB", NumberHelper.readableFileSize(fileUnits, 1099511627775L))
    }

    @Test
    fun `readableFileSize correctly formats terabytes`() {
        // Terabytes
        assertEquals("1 TB", NumberHelper.readableFileSize(fileUnits, 1099511627776L))
        assertEquals("1,023.9 TB", NumberHelper.readableFileSize(fileUnits, 1125899906842623L))
    }

    @Test
    fun `readableFileSize formats petabytes and above as terabytes`() {
        assertEquals("1,024 TB", NumberHelper.readableFileSize(fileUnits, 1125899906842624L))
        assertEquals("2,000,000 TB", NumberHelper.readableFileSize(fileUnits, 2199023255552000000L))
    }

}
