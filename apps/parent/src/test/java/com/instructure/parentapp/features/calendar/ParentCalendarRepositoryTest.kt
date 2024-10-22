/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */
package com.instructure.parentapp.features.calendar

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.util.Date

class ParentCalendarRepositoryTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)
    private val calendarFilterDao: CalendarFilterDao = mockk(relaxed = true)

    private val calendarRepository: CalendarRepository =
        ParentCalendarRepository(plannerApi, coursesApi, calendarEventApi, apiPrefs, featuresApi, parentPrefs, calendarFilterDao)

    @Test(expected = Exception::class)
    fun `Throw exception when calendar event request fails`() = runTest {
        coEvery { calendarEventApi.getCalendarEvents(any(), any(), any(), any(), any(), any()) } returns DataResult.Fail()

        calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", listOf("course_1"), true)
    }

    @Test(expected = Exception::class)
    fun `Throw exception when planner items request fails`() = runTest {
        coEvery { plannerApi.getPlannerNotes(any(), any(), any(), any()) } returns DataResult.Fail()

        calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", listOf("course_1"), true)
    }

    @Test
    fun `getPlannerItems returns correct items sorted by date`() = runTest {
        val assignment = ScheduleItem(
            itemId = "123",
            title = "assignment",
            assignment = Assignment(id = 123L, dueAt = LocalDateTime.now().plusHours(1).toApiString()),
            itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
            contextCode = "course_1"
        )

        val calendarEvent = ScheduleItem(
            itemId = "0",
            title = "calendar event",
            assignment = null,
            startAt = LocalDateTime.now().plusHours(2).toApiString(),
            endAt = LocalDateTime.now().toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR,
            contextCode = "course_1"
        )

        val plannerNote = Plannable(
            id = 2,
            title = "To Do",
            null,
            null,
            null,
            null,
            Date(),
            null,
            LocalDateTime.now().toApiString(),
            null,
            null,
            null,
            null
        )

        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                any(), any(), any(), any()
            )
        } returns DataResult.Success(listOf(assignment))

        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                any(), any(), any(), any()
            )
        } returns DataResult.Success(listOf(calendarEvent))

        coEvery { plannerApi.getPlannerNotes(any(), any(), any(), any()) } returns DataResult.Success(listOf(plannerNote))

        val result = calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", listOf("course_1"), true)

        assertEquals(3, result.size)
        // Planner items should be sorted by date
        val plannerNoteResult = result[0]
        val assignmentResult = result[1]
        val calendarEventResult = result[2]

        assertEquals(assignment.assignment?.id, assignmentResult.plannable.id)
        assertEquals(assignment.title, assignmentResult.plannable.title)
        assertEquals(assignment.contextCode, assignmentResult.canvasContext.contextId)

        assertEquals(calendarEvent.itemId, calendarEventResult.plannable.id.toString())
        assertEquals(calendarEvent.title, calendarEventResult.plannable.title)
        assertEquals(calendarEvent.contextCode, calendarEventResult.canvasContext.contextId)

        assertEquals(plannerNote.id, plannerNoteResult.plannable.id)
        assertEquals(plannerNote.title, plannerNoteResult.plannable.title)

        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                any(),
                any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 1) {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                any(),
                any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 1) { plannerApi.getPlannerNotes(any(), any(), any(), any()) }
    }

    @Test
    fun `getPlannerItems filters hidden events`() = runTest {
        val assignment = ScheduleItem(
            itemId = "123",
            title = "assignment",
            assignment = Assignment(id = 123L, dueAt = LocalDateTime.now().toApiString()),
            itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
            contextCode = "course_1"
        )

        val assignmentHidden = ScheduleItem(
            itemId = "124",
            title = "assignment hidden",
            assignment = Assignment(id = 124L, dueAt = LocalDateTime.now().toApiString()),
            itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
            contextCode = "course_1",
            isHidden = true
        )

        val calendarEvent = ScheduleItem(
            itemId = "0",
            title = "calendar event",
            assignment = null,
            startAt = LocalDateTime.now().toApiString(),
            endAt = LocalDateTime.now().toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR,
            contextCode = "course_1"
        )

        val calendarEventHidden = ScheduleItem(
            itemId = "1",
            title = "calendar event hidden",
            assignment = null,
            startAt = LocalDateTime.now().toApiString(),
            endAt = LocalDateTime.now().toApiString(),
            itemType = ScheduleItem.Type.TYPE_CALENDAR,
            contextCode = "course_1",
            isHidden = true
        )

        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                any(), any(), any(), any()
            )
        } returns DataResult.Success(listOf(assignment, assignmentHidden))

        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                any(), any(), any(), any()
            )
        } returns DataResult.Success(listOf(calendarEvent, calendarEventHidden))

        coEvery { plannerApi.getPlannerNotes(any(), any(), any(), any()) } returns DataResult.Success(emptyList())

        val result = calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", listOf("course_1"), true)

        assertEquals(2, result.size)
        val assignmentResult = result.find { it.plannableType == PlannableType.ASSIGNMENT }!!
        val calendarEventResult = result.find { it.plannableType == PlannableType.CALENDAR_EVENT }!!
        assertEquals(assignment.assignment?.id, assignmentResult.plannable.id)
        assertEquals(assignment.title, assignmentResult.plannable.title)

        assertEquals(calendarEvent.itemId, calendarEventResult.plannable.id.toString())
        assertEquals(calendarEvent.title, calendarEventResult.plannable.title)
    }

    @Test
    fun `getPlannerItems returns empty list when no canvas contexts are given`() = runTest {
        val assignment = ScheduleItem(
            itemId = "123",
            title = "assignment",
            assignment = Assignment(id = 123L, dueAt = LocalDateTime.now().toApiString()),
            itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
            contextCode = "course_1"
        )

        coEvery {
            calendarEventApi.getCalendarEvents(
                any(),
                CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                any(), any(), any(), any()
            )
        } returns DataResult.Success(listOf(assignment))

        val result = calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)

        assertEquals(0, result.size)
    }

    @Test
    fun `Get contexts return failed results if course request is failed`() = runTest {
        coEvery { coursesApi.firstPageObserveeCourses(any()) } returns DataResult.Fail()

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        assertEquals(DataResult.Fail(), canvasContextsResults)
    }

    @Test
    fun `Get contexts adds user context and returns it with valid courses when course request is successful`() = runTest {
        val courses = listOf(
            Course(44, enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Teacher))),
            Course(1, enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Observer, userId = 55))),
            Course(2, enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Observer, userId = 77)))
        )
        coEvery { parentPrefs.currentStudent } returns User(55, "Test Student")
        coEvery { coursesApi.firstPageObserveeCourses(any()) } returns DataResult.Success(courses)
        coEvery { apiPrefs.user } returns User(1, "Test User")

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        val userResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.USER] ?: emptyList()
        assertEquals(1, userResult.size)
        assertEquals(1, userResult[0].id)
        assertEquals("Test User", userResult[0].name)

        val coursesResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.COURSE] ?: emptyList()
        assertEquals(1, coursesResult.size)
        assertEquals(1, coursesResult[0].id)
    }

    @Test
    fun `Return 20 for calendar filter limit when context limit is increased`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(mapOf("calendar_contexts_limit" to true))

        val result = calendarRepository.getCalendarFilterLimit()

        assertEquals(20, result)
    }

    @Test
    fun `Return 10 for calendar filter limit when context limit is not increased`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(mapOf("calendar_contexts_limit" to false))

        val result = calendarRepository.getCalendarFilterLimit()

        assertEquals(10, result)
    }

    @Test
    fun `Return 10 for calendar filter limit when features request fails`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Fail()

        val result = calendarRepository.getCalendarFilterLimit()

        assertEquals(10, result)
    }

    @Test
    fun `getCalendarFilters calls dao with the correct params and returns results frm db`() = runTest {
        val filters = CalendarFilterEntity(1, "domain", "1", -1, setOf("filter1"))
        coEvery { calendarFilterDao.findByUserIdAndDomainAndObserveeId(any(), any(), any()) } returns filters
        coEvery { apiPrefs.user } returns User(1, "Test User")
        coEvery { apiPrefs.fullDomain } returns "domain"
        coEvery { parentPrefs.currentStudent } returns User(55, "Test Student")

        val result = calendarRepository.getCalendarFilters()

        assertEquals(filters, result)
        coVerify { calendarFilterDao.findByUserIdAndDomainAndObserveeId(1, "domain", 55) }
    }

    @Test
    fun `Update calendar filters updates db and adds observee id to the Entity`() = runTest {
        coEvery { parentPrefs.currentStudent } returns User(55, "Test student")
        val filters = CalendarFilterEntity(1, "domain", "1", -1, setOf("filter1"))

        calendarRepository.updateCalendarFilters(filters)

        val expectedArgument = filters.copy(observeeId = 55)
        coVerify { calendarFilterDao.insertOrUpdate(expectedArgument) }
    }
}