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

import com.instructure.canvasapi2.utils.DateHelper

import org.junit.Assert

import org.junit.Test

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class DateHelperTest {
    @Test
    fun stringToDate() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.US)
        val calendar = Calendar.getInstance()
        // Clear out milliseconds because we're not displaying that in the simple date format
        calendar.set(Calendar.MILLISECOND, 0)

        val date = calendar.time
        val nowAsString = df.format(date)

        // Add a 'Z' at the end. Discussion dates (where stringToDate is used) has a Z at the end of the string ("2037-07-28T19:38:31Z")
        // so we parse that out in the function
        Assert.assertEquals(date, DateHelper.stringToDate(nowAsString + "Z"))
    }

    @Test
    fun isSameDay_date() {
        val date = Date()
        val otherDate = Date()
        otherDate.time = date.time

        assertEquals(true, DateHelper.isSameDay(date, otherDate))
    }

    @Test
    fun isSameDay_dateDifferent() {
        val date = Date()
        val otherDate = Date()
        val oneDay = (24 * 60 * 60 * 1000).toLong()
        otherDate.time = date.time + oneDay

        assertEquals(false, DateHelper.isSameDay(date, otherDate))
    }

    @Test
    fun isSameDay_calendar() {
        val calendar = Calendar.getInstance()
        val otherCalendar = Calendar.getInstance()
        otherCalendar.timeInMillis = calendar.timeInMillis

        assertEquals(true, DateHelper.isSameDay(calendar, otherCalendar))
    }

    @Test
    fun isSameDay_calendarDifferentTimeOfDay() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 4)
        val otherCalendar = Calendar.getInstance()
        otherCalendar.timeInMillis = calendar.timeInMillis
        otherCalendar.set(Calendar.HOUR_OF_DAY, 5)

        assertEquals(true, DateHelper.isSameDay(calendar, otherCalendar))
    }

    @Test
    fun isSameDay_calendarDifferentDay() {
        val calendar = Calendar.getInstance()
        val otherCalendar = Calendar.getInstance()
        otherCalendar.timeInMillis = calendar.timeInMillis
        otherCalendar.add(Calendar.DAY_OF_MONTH, 1)

        assertEquals(false, DateHelper.isSameDay(calendar, otherCalendar))
    }

    @Test
    fun compareDays_equal() {
        val calendar = Calendar.getInstance()
        val otherCalendar = Calendar.getInstance()
        otherCalendar.timeInMillis = calendar.timeInMillis

        assertEquals(0, DateHelper.compareDays(calendar, otherCalendar))
    }

    @Test
    fun compareDays_firstCalendarOlder() {
        val calendar = Calendar.getInstance()
        val otherCalendar = Calendar.getInstance()
        otherCalendar.timeInMillis = calendar.timeInMillis
        otherCalendar.add(Calendar.DAY_OF_MONTH, 1)

        assertTrue(DateHelper.compareDays(calendar, otherCalendar) < 0)
    }

    @Test
    fun compareDays_secondCalendarOlder() {
        val calendar = Calendar.getInstance()
        val otherCalendar = Calendar.getInstance()
        otherCalendar.timeInMillis = calendar.timeInMillis
        otherCalendar.add(Calendar.DAY_OF_MONTH, -1)

        assertTrue(DateHelper.compareDays(calendar, otherCalendar) > 0)
    }
}