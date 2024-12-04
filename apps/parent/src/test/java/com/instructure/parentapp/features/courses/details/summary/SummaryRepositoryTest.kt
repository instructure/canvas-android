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
package com.instructure.parentapp.features.courses.details.summary

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SummaryRepositoryTest {
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)
    val repository = SummaryRepository(courseApi, calendarEventApi)

    @Test
    fun `getCourse should return course on successful call`() = runTest {
        val course: Course = mockk(relaxed = true)
        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Success(course)

        val result = repository.getCourse(1)

        assertEquals(course, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `getCourse should throw exception on failed call`() = runTest {
        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Fail()

        repository.getCourse(1)
    }

    @Test
    fun `getCalendarEvents should return list of schedule items on successful call`() = runTest {
        val calendarItems: List<ScheduleItem> = listOf(mockk(relaxed = true))
        val assignmentItems: List<ScheduleItem> = listOf(mockk(relaxed = true))
        coEvery { calendarEventApi.getCalendarEvents(any(), CalendarEventAPI.CalendarEventType.CALENDAR.apiName, any(), any(), any(), any()) } returns DataResult.Success(calendarItems)
        coEvery { calendarEventApi.getCalendarEvents(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName, any(), any(), any(), any()) } returns DataResult.Success(assignmentItems)

        val result = repository.getCalendarEvents("1")

        assertEquals(assignmentItems + calendarItems, result)
    }

    @Test
    fun `getCalendarEvents should filter out hidden elements`() = runTest {
        val calendarItems: List<ScheduleItem> = listOf(ScheduleItem(itemId = "1", isHidden = true), ScheduleItem(itemId = "2", isHidden = false))
        val assignmentItems: List<ScheduleItem> = listOf(ScheduleItem(itemId = "3", isHidden = true), ScheduleItem(itemId = "4", isHidden = false))
        val expected = (assignmentItems + calendarItems).filterNot { it.isHidden }
        coEvery { calendarEventApi.getCalendarEvents(any(), CalendarEventAPI.CalendarEventType.CALENDAR.apiName, any(), any(), any(), any()) } returns DataResult.Success(calendarItems)
        coEvery { calendarEventApi.getCalendarEvents(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName, any(), any(), any(), any()) } returns DataResult.Success(assignmentItems)

        val result = repository.getCalendarEvents("1")

        assertEquals(expected, result)
    }

    @Test
    fun `getCalendarEvents should depaginate elements`() = runTest {
        val items: List<ScheduleItem> = listOf(
            ScheduleItem(itemId = "1"),
            ScheduleItem(itemId = "2"),
            ScheduleItem(itemId = "3"),
            ScheduleItem(itemId = "4")
        )
        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(listOf(items[0]), linkHeaders = LinkHeaders(nextUrl = "next1"))
        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(listOf(items[2]), linkHeaders = LinkHeaders(nextUrl = "next2"))

        coEvery { calendarEventApi.next("next1", any()) } returns DataResult.Success(listOf(items[1]))
        coEvery { calendarEventApi.next("next2", any()) } returns DataResult.Success(listOf(items[3]))

        val result = repository.getCalendarEvents("1")

        assertEquals(items, result)
    }

    @Test
    fun `getCalendarEvents should return empty list of schedule items on failed call`() = runTest {
        coEvery { calendarEventApi.getCalendarEvents(any(), CalendarEventAPI.CalendarEventType.CALENDAR.apiName, any(), any(), any(), any()) } returns DataResult.Fail()
        coEvery { calendarEventApi.getCalendarEvents(any(), CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName, any(), any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getCalendarEvents("1")

        assertEquals(emptyList<ScheduleItem>(), result)
    }
}