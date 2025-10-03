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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.PlannerItemDao
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.entities.PlannerItemEntity
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade
import java.util.Date
import com.instructure.student.mobius.syllabus.datasource.SyllabusLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyllabusLocalDataSourceTest {

    private val courseSettingsDao: CourseSettingsDao = mockk(relaxed = true)
    private val courseFacade: CourseFacade = mockk(relaxed = true)
    private val scheduleItemFacade: ScheduleItemFacade = mockk(relaxed = true)
    private val plannerItemDao: PlannerItemDao = mockk(relaxed = true)

    private lateinit var syllabusLocalDataSource: SyllabusLocalDataSource

    @Before
    fun setup() {
        syllabusLocalDataSource = SyllabusLocalDataSource(courseSettingsDao, courseFacade, scheduleItemFacade, plannerItemDao)
    }

    @Test
    fun `Return course settings api model`() = runTest {
        val expected = CourseSettings(courseSummary = true)
        coEvery { courseSettingsDao.findByCourseId(any()) } returns CourseSettingsEntity(1L, true, false)

        val result = syllabusLocalDataSource.getCourseSettings(1L, false)

        assertEquals(expected, result)
        coVerify(exactly = 1) {
            courseSettingsDao.findByCourseId(1L)
        }
    }

    @Test
    fun `Return course api model`() = runTest {
        val expected = Course(id = 1L, syllabusBody = "Syllabus Body")
        coEvery { courseFacade.getCourseById(any()) } returns expected

        val result = syllabusLocalDataSource.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            courseFacade.getCourseById(1L)
        }
    }

    @Test
    fun `Return failed result if the course is missing`() = runTest {
        coEvery { courseFacade.getCourseById(any()) } returns null

        val result = syllabusLocalDataSource.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return calendar events`() = runTest {
        val expected = listOf(ScheduleItem(itemId = "event_1"), ScheduleItem(itemId = "event_2"))

        coEvery { scheduleItemFacade.findByItemType(any(), any()) } returns expected

        val result = syllabusLocalDataSource.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.ASSIGNMENT,
            null,
            null,
            listOf("course_1"),
            false
        )

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            scheduleItemFacade.findByItemType(listOf("course_1"), CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName)
        }
    }

    @Test
    fun `Return failed data result on calendar event error`() = runTest {
        coEvery { scheduleItemFacade.findByItemType(any(), any()) } throws Exception()

        val result = syllabusLocalDataSource.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.ASSIGNMENT,
            null,
            null,
            listOf("course_1"),
            false
        )

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return planner items for single course`() = runTest {
        val plannable = Plannable(
            id = 1L,
            title = "Assignment 1",
            courseId = 1L,
            groupId = null,
            userId = null,
            pointsPossible = 10.0,
            dueAt = Date(),
            assignmentId = 1L,
            todoDate = null,
            startAt = null,
            endAt = null,
            details = "Assignment details",
            allDay = false
        )
        val plannerItem = PlannerItem(
            courseId = 1L,
            groupId = null,
            userId = null,
            contextType = "course",
            contextName = "Course 1",
            plannableType = PlannableType.ASSIGNMENT,
            plannable = plannable,
            plannableDate = Date(),
            htmlUrl = "https://example.com",
            submissionState = null,
            newActivity = false
        )
        val entity = PlannerItemEntity(plannerItem, 1L)
        val expected = listOf(plannerItem)

        coEvery { plannerItemDao.findByCourseIds(listOf(1L)) } returns listOf(entity)

        val result = syllabusLocalDataSource.getPlannerItems(null, null, listOf("course_1"), null, false)

        assertEquals(DataResult.Success(expected).isSuccess, result.isSuccess)
        coVerify(exactly = 1) {
            plannerItemDao.findByCourseIds(listOf(1L))
        }
    }

    @Test
    fun `Return planner items for multiple courses`() = runTest {
        val plannable1 = Plannable(
            id = 1L,
            title = "Assignment 1",
            courseId = 1L,
            groupId = null,
            userId = null,
            pointsPossible = 10.0,
            dueAt = Date(),
            assignmentId = 1L,
            todoDate = null,
            startAt = null,
            endAt = null,
            details = "Assignment details",
            allDay = false
        )
        val plannerItem1 = PlannerItem(
            courseId = 1L,
            groupId = null,
            userId = null,
            contextType = "course",
            contextName = "Course 1",
            plannableType = PlannableType.ASSIGNMENT,
            plannable = plannable1,
            plannableDate = Date(),
            htmlUrl = "https://example.com",
            submissionState = null,
            newActivity = false
        )
        val entity1 = PlannerItemEntity(plannerItem1, 1L)

        val plannable2 = Plannable(
            id = 2L,
            title = "Assignment 2",
            courseId = 2L,
            groupId = null,
            userId = null,
            pointsPossible = 10.0,
            dueAt = Date(),
            assignmentId = 2L,
            todoDate = null,
            startAt = null,
            endAt = null,
            details = "Assignment details",
            allDay = false
        )
        val plannerItem2 = PlannerItem(
            courseId = 2L,
            groupId = null,
            userId = null,
            contextType = "course",
            contextName = "Course 2",
            plannableType = PlannableType.ASSIGNMENT,
            plannable = plannable2,
            plannableDate = Date(),
            htmlUrl = "https://example.com",
            submissionState = null,
            newActivity = false
        )
        val entity2 = PlannerItemEntity(plannerItem2, 2L)

        coEvery { plannerItemDao.findByCourseIds(listOf(1L, 2L)) } returns listOf(entity1, entity2)

        val result = syllabusLocalDataSource.getPlannerItems(null, null, listOf("course_1", "course_2"), null, false)

        assertEquals(DataResult.Success(listOf(plannerItem1, plannerItem2)).isSuccess, result.isSuccess)
        coVerify(exactly = 1) {
            plannerItemDao.findByCourseIds(listOf(1L, 2L))
        }
    }

    @Test
    fun `Return empty list when no context codes provided`() = runTest {
        val result = syllabusLocalDataSource.getPlannerItems(null, null, emptyList(), null, false)

        assertEquals(DataResult.Success(emptyList<PlannerItem>()), result)
        coVerify(exactly = 0) {
            plannerItemDao.findByCourseIds(any())
        }
    }

    @Test
    fun `Return empty list for invalid context codes`() = runTest {
        val result = syllabusLocalDataSource.getPlannerItems(null, null, listOf("user_1", "group_2"), null, false)

        assertEquals(DataResult.Success(emptyList<PlannerItem>()), result)
        coVerify(exactly = 0) {
            plannerItemDao.findByCourseIds(any())
        }
    }

    @Test
    fun `Return failed data result on planner items error`() = runTest {
        coEvery { plannerItemDao.findByCourseIds(any()) } throws Exception()

        val result = syllabusLocalDataSource.getPlannerItems(null, null, listOf("course_1"), null, false)

        assertEquals(DataResult.Fail(), result)
    }
}