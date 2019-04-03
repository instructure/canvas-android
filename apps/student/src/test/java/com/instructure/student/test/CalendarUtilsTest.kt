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

package com.instructure.student.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.student.util.CanvasCalendarUtils
import hirondelle.date4j.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
class CalendarUtilsTest : Assert() {

    @Test
    fun testWeekWindow() {
        var startDayMonday = false
        // Test cases for start day Sunday

        // Sunday
        val dateTime1 = DateTime("2014-12-28")
        val date1 = Date(dateTime1.getMilliseconds(TimeZone.getDefault()))
        // Sunday
        val startTime = DateTime("2014-12-28")
        // Saturday
        val endTime = DateTime("2015-01-03")

        val dateWindow1 = CanvasCalendarUtils.setSelectedWeekWindow(date1, startDayMonday)
        val endTimeResult1 = DateTime.forInstant(dateWindow1.end.time, TimeZone.getDefault())
        val startTimeResult1 = DateTime.forInstant(dateWindow1.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult1.endOfDay.compareTo(startTime.endOfDay) == 0)
        Assert.assertTrue(endTimeResult1.endOfDay.compareTo(endTime.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date1, dateWindow1.start, dateWindow1.start))


        // Monday
        val dateTime2 = DateTime("2014-12-29")
        val date2 = Date(dateTime2.getMilliseconds(TimeZone.getDefault()))

        val dateWindow2 = CanvasCalendarUtils.setSelectedWeekWindow(date2, startDayMonday)
        val endTimeResult2 = DateTime.forInstant(dateWindow2.end.time, TimeZone.getDefault())
        val startTimeResult2 = DateTime.forInstant(dateWindow2.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult2.endOfDay.compareTo(startTime.endOfDay) == 0)
        Assert.assertTrue(endTimeResult2.endOfDay.compareTo(endTime.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date2, dateWindow2.start, dateWindow2.end))

        // Tuesday
        val dateTime3 = DateTime("2014-12-30")
        val date3 = Date(dateTime3.getMilliseconds(TimeZone.getDefault()))

        val dateWindow3 = CanvasCalendarUtils.setSelectedWeekWindow(date3, startDayMonday)
        val endTimeResult3 = DateTime.forInstant(dateWindow3.end.time, TimeZone.getDefault())
        val startTimeResult3 = DateTime.forInstant(dateWindow3.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult3.endOfDay.compareTo(startTime.endOfDay) == 0)
        Assert.assertTrue(endTimeResult3.endOfDay.compareTo(endTime.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date3, dateWindow3.start, dateWindow3.end))

        // Wednesday
        val dateTime4 = DateTime("2014-12-31")
        val date4 = Date(dateTime4.getMilliseconds(TimeZone.getDefault()))

        val dateWindow4 = CanvasCalendarUtils.setSelectedWeekWindow(date4, startDayMonday)
        val endTimeResult4 = DateTime.forInstant(dateWindow4.end.time, TimeZone.getDefault())
        val startTimeResult4 = DateTime.forInstant(dateWindow4.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult4.endOfDay.compareTo(startTime.endOfDay) == 0)
        Assert.assertTrue(endTimeResult4.endOfDay.compareTo(endTime.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date4, dateWindow4.start, dateWindow4.end))

        // Thursday
        val dateTime5 = DateTime("2015-01-01")
        val date5 = Date(dateTime5.getMilliseconds(TimeZone.getDefault()))

        val dateWindow5 = CanvasCalendarUtils.setSelectedWeekWindow(date5, startDayMonday)
        val endTimeResult5 = DateTime.forInstant(dateWindow5.end.time, TimeZone.getDefault())
        val startTimeResult5 = DateTime.forInstant(dateWindow5.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult5.endOfDay.compareTo(startTime.endOfDay) == 0)
        Assert.assertTrue(endTimeResult5.endOfDay.compareTo(endTime.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date5, dateWindow5.start, dateWindow5.end))

        // Friday
        val dateTime6 = DateTime("2015-01-02")
        val date6 = Date(dateTime6.getMilliseconds(TimeZone.getDefault()))

        val dateWindow6 = CanvasCalendarUtils.setSelectedWeekWindow(date6, startDayMonday)
        val endTimeResult6 = DateTime.forInstant(dateWindow6.end.time, TimeZone.getDefault())
        val startTimeResult6 = DateTime.forInstant(dateWindow6.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult6.endOfDay.compareTo(startTime.endOfDay) == 0)
        Assert.assertTrue(endTimeResult6.endOfDay.compareTo(endTime.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date6, dateWindow6.start, dateWindow6.end))

        // Saturday
        val dateTime7 = DateTime("2015-01-02")
        val date7 = Date(dateTime7.getMilliseconds(TimeZone.getDefault()))

        val dateWindow7 = CanvasCalendarUtils.setSelectedWeekWindow(date4, startDayMonday)
        val endTimeResult7 = DateTime.forInstant(dateWindow7.end.time, TimeZone.getDefault())
        val startTimeResult7 = DateTime.forInstant(dateWindow7.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult7.endOfDay.compareTo(startTime.endOfDay) == 0)
        Assert.assertTrue(endTimeResult7.endOfDay.compareTo(endTime.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date7, dateWindow7.start, dateWindow7.end))

        /////////////////////////////////////
        // Test cases for start day Monday //
        /////////////////////////////////////
        startDayMonday = true

        // Monday
        val dateTime1m = DateTime("2014-12-29")
        val date1m = Date(dateTime1m.getMilliseconds(TimeZone.getDefault()))
        // Monday
        val startTimeM = DateTime("2014-12-29")
        // Sunday
        val endTimeM = DateTime("2015-01-04")

        val dateWindow1m = CanvasCalendarUtils.setSelectedWeekWindow(date1m, startDayMonday)
        val endTimeResult1m = DateTime.forInstant(dateWindow1m.end.time, TimeZone.getDefault())
        val startTimeResult1m = DateTime.forInstant(dateWindow1m.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult1m.endOfDay.compareTo(startTimeM.endOfDay) == 0)
        Assert.assertTrue(endTimeResult1m.endOfDay.compareTo(endTimeM.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date1m, dateWindow1.start, dateWindow1m.end))


        // Tuesday
        val dateTime2m = DateTime("2014-12-30")
        val date2m = Date(dateTime2.getMilliseconds(TimeZone.getDefault()))

        val dateWindow2m = CanvasCalendarUtils.setSelectedWeekWindow(date2m, startDayMonday)
        val endTimeResult2m = DateTime.forInstant(dateWindow2m.end.time, TimeZone.getDefault())
        val startTimeResult2m = DateTime.forInstant(dateWindow2m.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult2m.endOfDay.compareTo(startTimeM.endOfDay) == 0)
        Assert.assertTrue(endTimeResult2m.endOfDay.compareTo(endTimeM.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date2m, dateWindow2m.start, dateWindow2m.end))

        // Wednesday
        val dateTime3m = DateTime("2014-12-31")
        val date3m = Date(dateTime3m.getMilliseconds(TimeZone.getDefault()))

        val dateWindow3m = CanvasCalendarUtils.setSelectedWeekWindow(date3m, startDayMonday)
        val endTimeResult3m = DateTime.forInstant(dateWindow3m.end.time, TimeZone.getDefault())
        val startTimeResult3m = DateTime.forInstant(dateWindow3m.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult3m.endOfDay.compareTo(startTimeM.endOfDay) == 0)
        Assert.assertTrue(endTimeResult3m.endOfDay.compareTo(endTimeM.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date3m, dateWindow3m.start, dateWindow3m.end))

        // Thursday
        val dateTime4m = DateTime("2015-01-01")
        val date4m = Date(dateTime4m.getMilliseconds(TimeZone.getDefault()))

        val dateWindow4m = CanvasCalendarUtils.setSelectedWeekWindow(date4m, startDayMonday)
        val endTimeResult4m = DateTime.forInstant(dateWindow4m.end.time, TimeZone.getDefault())
        val startTimeResult4m = DateTime.forInstant(dateWindow4m.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult4m.endOfDay.compareTo(startTimeM.endOfDay) == 0)
        Assert.assertTrue(endTimeResult4m.endOfDay.compareTo(endTimeM.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date4m, dateWindow4m.start, dateWindow4m.end))

        // Friday
        val dateTime5m = DateTime("2015-01-02")
        val date5m = Date(dateTime5m.getMilliseconds(TimeZone.getDefault()))

        val dateWindow5m = CanvasCalendarUtils.setSelectedWeekWindow(date5, startDayMonday)
        val endTimeResult5m = DateTime.forInstant(dateWindow5m.end.time, TimeZone.getDefault())
        val startTimeResult5m = DateTime.forInstant(dateWindow5m.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult5m.endOfDay.compareTo(startTimeM.endOfDay) == 0)
        Assert.assertTrue(endTimeResult5m.endOfDay.compareTo(endTimeM.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date5m, dateWindow5m.start, dateWindow5m.end))

        // Saturday
        val dateTime6m = DateTime("2015-01-03")
        val date6m = Date(dateTime6m.getMilliseconds(TimeZone.getDefault()))

        val dateWindow6m = CanvasCalendarUtils.setSelectedWeekWindow(date6m, startDayMonday)
        val endTimeResult6m = DateTime.forInstant(dateWindow6m.end.time, TimeZone.getDefault())
        val startTimeResult6m = DateTime.forInstant(dateWindow6m.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult6m.endOfDay.compareTo(startTimeM.endOfDay) == 0)
        Assert.assertTrue(endTimeResult6m.endOfDay.compareTo(endTimeM.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date6m, dateWindow6m.start, dateWindow6m.end))

        // Sunday
        val dateTime7m = DateTime("2015-01-04")
        val date7m = Date(dateTime7m.getMilliseconds(TimeZone.getDefault()))

        val dateWindow7m = CanvasCalendarUtils.setSelectedWeekWindow(date7m, startDayMonday)
        val endTimeResult7m = DateTime.forInstant(dateWindow7m.end.time, TimeZone.getDefault())
        val startTimeResult7m = DateTime.forInstant(dateWindow7m.start.time, TimeZone.getDefault())

        Assert.assertTrue(startTimeResult7m.endOfDay.compareTo(startTimeM.endOfDay) == 0)
        Assert.assertTrue(endTimeResult7m.endOfDay.compareTo(endTimeM.endOfDay) == 0)
        Assert.assertTrue(CanvasCalendarUtils.isWithinWeekWindow(date7m, dateWindow7m.start, dateWindow7m.end))

    }

}
