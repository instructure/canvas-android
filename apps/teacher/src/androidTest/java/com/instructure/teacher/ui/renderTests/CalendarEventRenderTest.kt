/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.renderTests

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.teacher.features.calendar.event.CalendarEventFragment
import com.instructure.teacher.ui.renderTests.pages.CalendarEventRenderPage
import com.instructure.teacher.ui.utils.TeacherRenderTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId

@HiltAndroidTest
class CalendarEventRenderTest : TeacherRenderTest() {

    private val calendarEventRenderPage = CalendarEventRenderPage()

    @Test
    fun displayToolbarTitle() {
        val date = OffsetDateTime.now().withMonth(4).withDayOfMonth(2).withHour(14).withMinute(0)

        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = "Calendar event",
            assignment = null,
            endAt = DateTimeUtils.toDate(date.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            startAt = DateTimeUtils.toDate(date.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        openCalendarEventPage(scheduleItem)
        calendarEventRenderPage.assertDisplaysToolbarText("Calendar event")
    }

    @Test
    fun displayCalendarEvent() {
        val date = OffsetDateTime.now().withMonth(4).withDayOfMonth(2).withHour(14).withMinute(0)

        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = "Calendar event",
            assignment = null,
            endAt = DateTimeUtils.toDate(date.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            startAt = DateTimeUtils.toDate(date.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        openCalendarEventPage(scheduleItem)
        calendarEventRenderPage.assertPageObjects()
    }

    private fun openCalendarEventPage(scheduleItem: ScheduleItem) {
        val course = Course()
//        val fragmentArgs = CalendarEventFragment.createArgs(course, scheduleItem)
//        val fragment = CalendarEventFragment.newInstance(fragmentArgs)
//
//        activityRule.activity.loadFragment(fragment)
    }
}