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
package com.instructure.student.features.calendar

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class StudentCalendarRepositoryTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupsApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val calendarFilterDao: CalendarFilterDao = mockk(relaxed = true)

    private val calendarRepository: CalendarRepository = StudentCalendarRepository(plannerApi, coursesApi, groupsApi, apiPrefs, calendarFilterDao)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when request fails`() = runTest {
        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Fail()

        calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)
    }

    @Test
    fun `Return results from the api and filter announcments and assessment requests on successful request`() = runTest {
        val filteredItem = createPlannerItem(1, 3, PlannableType.ANNOUNCEMENT)
        val filteredItem2 = createPlannerItem(1, 7, PlannableType.ASSESSMENT_REQUEST)
        val plannerItems = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT),
            createPlannerItem(1, 2, PlannableType.QUIZ),
            filteredItem,
            createPlannerItem(1, 4, PlannableType.DISCUSSION_TOPIC),
            createPlannerItem(2, 5, PlannableType.PLANNER_NOTE),
            createPlannerItem(2, 6, PlannableType.CALENDAR_EVENT)
        )

        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Success(plannerItems)

        val result = calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)

        assertEquals(plannerItems.minus(listOf(filteredItem, filteredItem2).toSet()), result)
    }

    @Test
    fun `Return depaginated result when has next page`() = runTest {
        val plannerItems1 = listOf(
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT),
            createPlannerItem(1, 2, PlannableType.QUIZ),
        )

        val plannerItems2 = listOf(
            createPlannerItem(2, 5, PlannableType.PLANNER_NOTE),
            createPlannerItem(2, 6, PlannableType.CALENDAR_EVENT)
        )

        coEvery { plannerApi.getPlannerItems(any(), any(), any(), any()) } returns DataResult.Success(
            plannerItems1,
            linkHeaders = LinkHeaders(nextUrl = "next")
        )
        coEvery { plannerApi.nextPagePlannerItems(eq("next"), any()) } returns DataResult.Success(plannerItems2)

        val result = calendarRepository.getPlannerItems("2023-1-1", "2023-1-2", emptyList(), true)

        assertEquals(plannerItems1.plus(plannerItems2), result)
    }

    @Test
    fun `Get contexts return failed results and don't request groups if course request is failed`() = runTest {
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Fail()

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        assertEquals(DataResult.Fail(), canvasContextsResults)
        coVerify(exactly = 0) { groupsApi.getFirstPageGroups(any()) }
    }

    @Test
    fun `Get contexts returns only courses if group request is failed`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Fail()

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        val coursesResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.COURSE] ?: emptyList()
        assertEquals(1, coursesResult.size)
        assertEquals(courses[0].id, coursesResult[0].id)
    }

    @Test
    fun `Get contexts adds user context when course request is successful`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Fail()
        coEvery { apiPrefs.user } returns User(1, "Test User")

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        val userResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.USER] ?: emptyList()
        assertEquals(1, userResult.size)
        assertEquals(1, userResult[0].id)
        assertEquals("Test User", userResult[0].name)
    }

    @Test
    fun `Get contexts returns courses and groups if successful`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        val groups = listOf(Group(id = 63, courseId = 44, name = "First group"))
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        val coursesResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.COURSE] ?: emptyList()
        val groupsResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.GROUP] ?: emptyList()
        assertEquals(1, coursesResult.size)
        assertEquals(1, groupsResult.size)
        assertEquals(courses[0].id, coursesResult[0].id)
        assertEquals(groups[0].id, groupsResult[0].id)
        assertEquals(groups[0].name, groupsResult[0].name)
    }

    @Test
    fun `Get contexts returns only valid courses`() = runTest {
        val courses = listOf(
            Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
            Course(11) // no active enrollment
        )
        val groups = listOf(Group(id = 63, courseId = 44, name = "First group"))
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        val coursesResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.COURSE] ?: emptyList()
        assertEquals(1, coursesResult.size)
        assertEquals(courses[0].id, coursesResult[0].id)
    }

    @Test
    fun `Get contexts returns only valid groups`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        val groups = listOf(
            Group(id = 63, courseId = 44, name = "First group"),
            Group(id = 63, courseId = 33, name = "First group"), // Invalid course id
        )
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = calendarRepository.getCanvasContexts()

        val groupsResult = canvasContextsResults.dataOrThrow[CanvasContext.Type.GROUP] ?: emptyList()
        assertEquals(1, groupsResult.size)
        assertEquals(groups[0].id, groupsResult[0].id)
        assertEquals(groups[0].name, groupsResult[0].name)
    }

    @Test
    fun `Get calendar filter limit returns -1`() = runTest {
        assertEquals(-1, calendarRepository.getCalendarFilterLimit())
    }

    @Test
    fun `getCalendarFilters calls dao with the correct params and returns results frm db`() = runTest {
        val filters = CalendarFilterEntity(1, "domain", "1", -1, setOf("filter1"))
        coEvery { calendarFilterDao.findByUserIdAndDomain(any(), any()) } returns filters
        coEvery { apiPrefs.user } returns User(1, "Test User")
        coEvery { apiPrefs.fullDomain } returns "domain"

        val result = calendarRepository.getCalendarFilters()

        assertEquals(filters, result)
        coVerify { calendarFilterDao.findByUserIdAndDomain(1, "domain") }
    }

    @Test
    fun `Update calendar filters updates db`() = runTest {
        val filters = CalendarFilterEntity(1, "domain", "1", -1, setOf("filter1"))

        calendarRepository.updateCalendarFilters(filters)

        coVerify { calendarFilterDao.insertOrUpdate(filters) }
    }

    private fun createPlannerItem(
        courseId: Long,
        plannableId: Long,
        plannableType: PlannableType
    ): PlannerItem {
        val plannable = Plannable(
            id = plannableId,
            title = "Plannable $plannableId",
            courseId,
            null,
            null,
            null,
            null,
            plannableId,
            null,
            null,
            null,
            null,
            null
        )
        return PlannerItem(
            courseId,
            null,
            null,
            null,
            null,
            plannableType,
            plannable,
            Date(),
            null,
            SubmissionState(submitted = false),
            plannerOverride = PlannerOverride(plannableType = plannableType, plannableId = plannableId),
            newActivity = false
        )
    }
}