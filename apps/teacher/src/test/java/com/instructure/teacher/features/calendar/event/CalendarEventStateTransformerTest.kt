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

package com.instructure.teacher.features.calendar.event

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.teacher.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId

@RunWith(AndroidJUnit4::class)
class CalendarEventStateTransformerTest {

    private val baseDate =
        OffsetDateTime.now().withMonth(4).withDayOfMonth(2).withHour(14).withMinute(0)

    lateinit var context: Context

    private val calendarEventStateTransformer = CalendarEventStateTransformer()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `Show all day event as date title and the date as date subtitle if event is and all day event`() {
        // Given
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = null,
            assignment = null,
            isAllDay = true,
            endAt = DateTimeUtils.toDate(baseDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals(context.getString(R.string.allDayEvent), viewState.dateTitle)
        assertEquals("Thursday, Apr 2, 2020", viewState.dateSubtitle)
    }

    @Test
    fun `Show the date as date title and the start time with the end time as date subtitle, if event is not all day and start time and end time is different`() {
        // Given
        val endDate = baseDate.withHour(16)
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = null,
            assignment = null,
            isAllDay = false,
            startAt = DateTimeUtils.toDate(baseDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            endAt = DateTimeUtils.toDate(endDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals("Thursday, Apr 2, 2020", viewState.dateTitle)
        assertEquals("2:00 PM - 4:00 PM", viewState.dateSubtitle)
    }

    @Test
    fun `Show the date as date title and the time as date subtitle, if event is not all day and start time and end time is the same`() {
        // Given
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = null,
            assignment = null,
            isAllDay = false,
            startAt = DateTimeUtils.toDate(baseDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            endAt = DateTimeUtils.toDate(baseDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals("Thursday, Apr 2, 2020", viewState.dateTitle)
        assertEquals("2:00 PM", viewState.dateSubtitle)
    }

    @Test
    fun `Show no location as location title and empty location subtitle if event does not have address and name`() {
        // Given
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = null,
            assignment = null,
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals(context.getString(R.string.noLocation), viewState.locationTitle)
        assertEquals("", viewState.locationSubtitle)
    }

    @Test
    fun `Show location name as location title and address as location subtitle if event has address and name`() {
        // Given
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = null,
            assignment = null,
            locationName = "Budapest",
            locationAddress = "Deak Ferenc ter",
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals("Budapest", viewState.locationTitle)
        assertEquals("Deak Ferenc ter", viewState.locationSubtitle)
    }

    @Test
    fun `Show adress as location title and empty location subtitle if event only has address`() {
        // Given
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = null,
            assignment = null,
            locationAddress = "Deak Ferenc ter",
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals("Deak Ferenc ter", viewState.locationTitle)
        assertEquals("", viewState.locationSubtitle)
    }

    @Test
    fun `Show title from schedule item as event title`() {
        // Given
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = "Calendar Event",
            assignment = null,
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals("Calendar Event", viewState.eventTitle)
    }

    @Test
    fun `Show description from schedule item as html content`() {
        // Given
        val scheduleItem = ScheduleItem(
            itemId = "0",
            title = "Calendar Event",
            description = "<h>Header</h>",
            assignment = null,
            itemType = ScheduleItem.Type.TYPE_CALENDAR)

        // When
        val viewState = calendarEventStateTransformer.transformScheduleItem(context, scheduleItem)

        // Then
        assertEquals("<h>Header</h>", viewState.htmlContent)
    }
}