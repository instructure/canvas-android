/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class SyllabusRepositoryTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)

    private lateinit var repository: SyllabusRepository

    @Before
    fun setup() {
        repository = SyllabusRepository(plannerApi, calendarEventApi)
    }

    @Test
    fun `getPlannerItems returns success when API call succeeds`() = runTest {
        val contextCodes = listOf("course_1")
        val expectedItems = listOf(
            PlannerItem(
                courseId = 1L,
                groupId = null,
                userId = null,
                contextType = "Course",
                contextName = "Test Course",
                plannableType = PlannableType.QUIZ,
                plannable = Plannable(
                    id = 123,
                    title = "Test Quiz",
                    courseId = 1L,
                    groupId = null,
                    userId = null,
                    pointsPossible = 10.0,
                    dueAt = null,
                    assignmentId = null,
                    todoDate = Date().toApiString(),
                    startAt = null,
                    endAt = null,
                    details = null,
                    allDay = null
                ),
                plannableDate = Date(),
                htmlUrl = null,
                submissionState = null,
                newActivity = false
            )
        )

        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns DataResult.Success(expectedItems)

        val result = repository.getPlannerItems(
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            filter = "all_ungraded_todo_items",
            forceNetwork = false
        )

        assertTrue(result is DataResult.Success)
        assertEquals(expectedItems, result.dataOrNull)
        coVerify(exactly = 1) {
            plannerApi.getPlannerItems(
                null,
                null,
                contextCodes,
                "all_ungraded_todo_items",
                any()
            )
        }
    }

    @Test
    fun `getPlannerItems returns failure when API call fails`() = runTest {
        val contextCodes = listOf("course_1")

        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns DataResult.Fail()

        val result = repository.getPlannerItems(
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            filter = "all_ungraded_todo_items",
            forceNetwork = true
        )

        assertTrue(result is DataResult.Fail)
        coVerify(exactly = 1) {
            plannerApi.getPlannerItems(
                null,
                null,
                contextCodes,
                "all_ungraded_todo_items",
                any()
            )
        }
    }

    @Test
    fun `getPlannerItems passes forceNetwork parameter correctly`() = runTest {
        val contextCodes = listOf("course_1")

        coEvery {
            plannerApi.getPlannerItems(any(), any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        repository.getPlannerItems(
            startDate = "2025-01-01",
            endDate = "2025-01-31",
            contextCodes = contextCodes,
            filter = null,
            forceNetwork = true
        )

        coVerify(exactly = 1) {
            plannerApi.getPlannerItems(
                "2025-01-01",
                "2025-01-31",
                contextCodes,
                null,
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getCalendarEvents returns success for ASSIGNMENT type when API call succeeds`() = runTest {
        val contextCodes = listOf("course_1")
        val expectedItems = listOf(
            ScheduleItem(
                itemId = "123",
                title = "Test Assignment",
                itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
                contextCode = "course_1"
            )
        )

        coEvery {
            calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(expectedItems)

        val result = repository.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.ASSIGNMENT,
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            forceNetwork = false
        )

        assertTrue(result is DataResult.Success)
        assertEquals(expectedItems, result.dataOrNull)
        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                true,
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                null,
                null,
                contextCodes,
                any()
            )
        }
    }

    @Test
    fun `getCalendarEvents returns success for CALENDAR type when API call succeeds`() = runTest {
        val contextCodes = listOf("course_1")
        val expectedItems = listOf(
            ScheduleItem(
                itemId = "456",
                title = "Test Calendar Event",
                itemType = ScheduleItem.Type.TYPE_CALENDAR,
                contextCode = "course_1"
            )
        )

        coEvery {
            calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(expectedItems)

        val result = repository.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.CALENDAR,
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            forceNetwork = false
        )

        assertTrue(result is DataResult.Success)
        assertEquals(expectedItems, result.dataOrNull)
        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                true,
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                null,
                null,
                contextCodes,
                any()
            )
        }
    }

    @Test
    fun `getCalendarEvents returns failure when API call fails`() = runTest {
        val contextCodes = listOf("course_1")

        coEvery {
            calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any())
        } returns DataResult.Fail()

        val result = repository.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.ASSIGNMENT,
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            forceNetwork = true
        )

        assertTrue(result is DataResult.Fail)
        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                true,
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                null,
                null,
                contextCodes,
                any()
            )
        }
    }

    @Test
    fun `getCalendarEvents passes date range parameters correctly`() = runTest {
        val contextCodes = listOf("course_1", "course_2")
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"

        coEvery {
            calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        repository.getCalendarEvents(
            allEvents = false,
            type = CalendarEventAPI.CalendarEventType.CALENDAR,
            startDate = startDate,
            endDate = endDate,
            contextCodes = contextCodes,
            forceNetwork = false
        )

        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                false,
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                startDate,
                endDate,
                contextCodes,
                match { !it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getCalendarEvents passes forceNetwork parameter correctly`() = runTest {
        val contextCodes = listOf("course_1")

        coEvery {
            calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        repository.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.ASSIGNMENT,
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            forceNetwork = true
        )

        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                true,
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                null,
                null,
                contextCodes,
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getCalendarEvents converts enum to apiName correctly`() = runTest {
        val contextCodes = listOf("course_1")

        coEvery {
            calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        // Test ASSIGNMENT type
        repository.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.ASSIGNMENT,
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            forceNetwork = false
        )

        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                any(),
                "assignment",
                any(),
                any(),
                any(),
                any()
            )
        }

        // Test CALENDAR type
        repository.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.CALENDAR,
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            forceNetwork = false
        )

        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                any(),
                "event",
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `getCalendarEvents handles multiple context codes correctly`() = runTest {
        val contextCodes = listOf("course_1", "course_2", "course_3")

        coEvery {
            calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any())
        } returns DataResult.Success(emptyList())

        repository.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.ASSIGNMENT,
            startDate = null,
            endDate = null,
            contextCodes = contextCodes,
            forceNetwork = false
        )

        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                any(),
                any(),
                any(),
                any(),
                contextCodes,
                any()
            )
        }
    }
}
