/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.student.test.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.student.mobius.syllabus.datasource.SyllabusNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyllabusNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)
    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)

    private lateinit var syllabusNetworkDataSource: SyllabusNetworkDataSource

    @Before
    fun setup() {
        syllabusNetworkDataSource = SyllabusNetworkDataSource(courseApi, calendarEventApi, plannerApi)
    }

    @Test
    fun `Return course settings`() = runTest {
        val expected = CourseSettings(courseSummary = false)
        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Success(expected)

        val result = syllabusNetworkDataSource.getCourseSettings(1L, false)

        assertEquals(expected, result)
        coVerify(exactly = 1) { courseApi.getCourseSettings(1L, RestParams(isForceReadFromNetwork = false)) }
    }

    @Test
    fun `Return null if course settings fails`() = runTest {
        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Fail()

        val result = syllabusNetworkDataSource.getCourseSettings(1L, false)

        assertNull(result)
    }

    @Test
    fun `Return course with syllabus`() = runTest {
        val expected = Course(id = 1L, syllabusBody = "Syllabus")
        coEvery { courseApi.getCourseWithSyllabus(any(), any()) } returns DataResult.Success(expected)

        val result = syllabusNetworkDataSource.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) { courseApi.getCourseWithSyllabus(1L, RestParams(isForceReadFromNetwork = false)) }
    }

    @Test
    fun `Return failed data result if course call fails`() = runTest {
        coEvery { courseApi.getCourseWithSyllabus(any(), any()) } returns DataResult.Fail()

        val result = syllabusNetworkDataSource.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return calendar events`() = runTest {
        val expected = listOf(ScheduleItem(itemId = "event_1"), ScheduleItem(itemId = "event_2"))

        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(expected)

        val result = syllabusNetworkDataSource.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.CALENDAR,
            null,
            null,
            listOf("course_1"),
            false
        )

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                true,
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                null,
                null,
                listOf("course_1"),
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)
            )
        }
    }

    @Test
    fun `Depaginate calendar events`() = runTest {
        val page1 = listOf(ScheduleItem(itemId = "event_1"), ScheduleItem(itemId = "event_2"))
        val page2 = listOf(ScheduleItem(itemId = "event_3"), ScheduleItem(itemId = "event_4"))

        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(page1, linkHeaders = LinkHeaders(nextUrl = "next_url"))

        coEvery { calendarEventApi.next(any(), any()) } returns DataResult.Success(page2)

        val result = syllabusNetworkDataSource.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.CALENDAR,
            null,
            null,
            listOf("course_1"),
            false
        )

        assertEquals(DataResult.Success(page1 + page2), result)
        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                true,
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                null,
                null,
                listOf("course_1"),
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)
            )
            calendarEventApi.next("next_url", RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false))
        }
    }

    @Test
    fun `Return failed data result if calender events fail`() = runTest {
        coEvery { calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any()) } returns DataResult.Fail()

        val result = syllabusNetworkDataSource.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.CALENDAR,
            null,
            null,
            listOf("course_1"),
            false
        )

        assertEquals(DataResult.Fail(), result)
    }
}