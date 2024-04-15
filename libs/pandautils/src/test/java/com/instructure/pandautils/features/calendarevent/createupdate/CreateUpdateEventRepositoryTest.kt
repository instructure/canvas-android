/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.calendarevent.createupdate

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class CreateUpdateEventRepositoryTest {

    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)

    private val repository = TestCreateUpdateEventRepository(calendarEventApi)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when create calendar event fails`() = runTest {
        coEvery {
            calendarEventApi.createCalendarEvent(
                any(),
                any()
            )
        } returns DataResult.Fail()

        repository.createEvent(
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description"
        )
    }

    @Test
    fun `Create single calendar event successful`() = runTest {
        val event = ScheduleItem("itemId")

        coEvery {
            calendarEventApi.createCalendarEvent(
                any(),
                any()
            )
        } returns DataResult.Success(event)

        val result = repository.createEvent(
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description"
        )

        Assert.assertEquals(listOf(event), result)
    }

    @Test
    fun `Create recurring calendar event successful`() = runTest {
        val event2 = ScheduleItem("itemId2")
        val event3 = ScheduleItem("itemId3")
        val event4 = ScheduleItem("itemId4")
        val event = ScheduleItem("itemId").copy(
            duplicates = listOf(
                ScheduleItem.CalendarEventWrapper(event2),
                ScheduleItem.CalendarEventWrapper(event3),
                ScheduleItem.CalendarEventWrapper(event4)
            )
        )

        coEvery {
            calendarEventApi.createCalendarEvent(
                any(),
                any()
            )
        } returns DataResult.Success(event)

        val result = repository.createEvent(
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description"
        )

        Assert.assertEquals(listOf(event, event2, event3, event4), result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when update calendar event fails`() = runTest {
        coEvery {
            calendarEventApi.updateCalendarEvent(
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        repository.updateEvent(
            eventId = 1L,
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description",
            modifyEventScope = CalendarEventAPI.ModifyEventScope.ONE
        )
    }

    @Test
    fun `Update single calendar event successful`() = runTest {
        val event = ScheduleItem("itemId")

        coEvery {
            calendarEventApi.updateCalendarEvent(
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(event)

        val result = repository.updateEvent(
            1L,
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description",
            CalendarEventAPI.ModifyEventScope.ONE
        )

        Assert.assertEquals(listOf(event), result)
    }

    @Test
    fun `Update single calendar event calls recurring endpoint if has rrule`() = runTest {
        val events = listOf(ScheduleItem("itemId"))

        coEvery {
            calendarEventApi.updateRecurringCalendarEvent(
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(events)

        val result = repository.updateEvent(
            1L,
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "rrule",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description",
            CalendarEventAPI.ModifyEventScope.ONE
        )

        Assert.assertEquals(events, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when update recurring calendar event fails`() = runTest {
        coEvery {
            calendarEventApi.updateRecurringCalendarEvent(
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        repository.updateEvent(
            eventId = 1L,
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "rrule",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description",
            modifyEventScope = CalendarEventAPI.ModifyEventScope.ONE
        )
    }

    @Test
    fun `Update recurring calendar event successful`() = runTest {
        val event = listOf(ScheduleItem("itemId"))

        coEvery {
            calendarEventApi.updateRecurringCalendarEvent(
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(event)

        val result = repository.updateEvent(
            1L,
            title = "title",
            startDate = "startDate",
            endDate = "endDate",
            rrule = "rrule",
            contextCode = "contextCode",
            locationName = "locationName",
            locationAddress = "locationAddress",
            description = "description",
            CalendarEventAPI.ModifyEventScope.ALL
        )

        Assert.assertEquals(event, result)
    }
}
