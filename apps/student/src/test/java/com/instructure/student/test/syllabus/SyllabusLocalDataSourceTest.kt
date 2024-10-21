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
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade
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

    private lateinit var syllabusLocalDataSource: SyllabusLocalDataSource

    @Before
    fun setup() {
        syllabusLocalDataSource = SyllabusLocalDataSource(courseSettingsDao, courseFacade, scheduleItemFacade)
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
}