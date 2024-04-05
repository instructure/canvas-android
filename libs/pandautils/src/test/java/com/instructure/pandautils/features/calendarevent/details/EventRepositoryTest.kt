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

package com.instructure.pandautils.features.calendarevent.details

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class EventRepositoryTest {

    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)

    private val eventRepository = EventRepository(calendarEventApi)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when get calendar event fails`() = runTest {
        coEvery { calendarEventApi.getCalendarEvent(any(), any()) } returns DataResult.Fail()

        eventRepository.getCalendarEvent(1)
    }

    @Test
    fun `Get calendar event successful`() = runTest {
        val expected = ScheduleItem("itemId")

        coEvery { calendarEventApi.getCalendarEvent(any(), any()) } returns DataResult.Success(expected)

        val result = eventRepository.getCalendarEvent(1)

        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when delete calendar event fails`() = runTest {
        coEvery { calendarEventApi.deleteCalendarEvent(any(), any<RestParams>()) } returns DataResult.Fail()

        eventRepository.deleteCalendarEvent(1)
    }

    @Test
    fun `Delete calendar event successful`() = runTest {
        val expected = ScheduleItem("itemId")

        coEvery { calendarEventApi.deleteCalendarEvent(any(), any<RestParams>()) } returns DataResult.Success(expected)

        val result = eventRepository.deleteCalendarEvent(1)

        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when delete recurring calendar event fails`() = runTest {
        coEvery { calendarEventApi.deleteRecurringCalendarEvent(any(), any(), any()) } returns DataResult.Fail()

        eventRepository.deleteRecurringCalendarEvent(1, CalendarEventAPI.ModifyEventScope.ALL)
    }

    @Test
    fun `Delete recurring calendar event successful`() = runTest {
        val expected = listOf(ScheduleItem("itemId"), ScheduleItem("itemId2"))

        coEvery { calendarEventApi.deleteRecurringCalendarEvent(any(), any(), any()) } returns DataResult.Success(expected)

        val result = eventRepository.deleteRecurringCalendarEvent(1, CalendarEventAPI.ModifyEventScope.ALL)

        Assert.assertEquals(expected, result)
    }
}
