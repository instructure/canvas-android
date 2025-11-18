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
import java.util.Date
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.syllabus.SyllabusRepository
import com.instructure.student.mobius.syllabus.datasource.SyllabusLocalDataSource
import com.instructure.student.mobius.syllabus.datasource.SyllabusNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyllabusRepositoryTest {

    private val syllabusLocalDataSource: SyllabusLocalDataSource = mockk(relaxed = true)
    private val syllabusNetworkDataSource: SyllabusNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var repository: SyllabusRepository

    @Before
    fun setUp() {
        repository = SyllabusRepository(syllabusLocalDataSource, syllabusNetworkDataSource, networkStateProvider, featureFlagProvider)
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Return course settings if online`() = runTest {
        val expected = CourseSettings(false)

        every { networkStateProvider.isOnline() } returns true

        coEvery { syllabusNetworkDataSource.getCourseSettings(any(), any()) } returns expected

        val result = repository.getCourseSettings(1L, false)

        assertEquals(expected, result)
        coVerify(exactly = 1) { syllabusNetworkDataSource.getCourseSettings(1L, false) }
        coVerify(exactly = 0) { syllabusLocalDataSource.getCourseSettings(any(), any()) }
    }

    @Test
    fun `Return course settings if offline`() = runTest {
        val expected = CourseSettings(false)

        every { networkStateProvider.isOnline() } returns false

        coEvery { syllabusLocalDataSource.getCourseSettings(any(), any()) } returns expected

        val result = repository.getCourseSettings(1L, false)

        assertEquals(expected, result)
        coVerify(exactly = 0) { syllabusNetworkDataSource.getCourseSettings(any(), any()) }
        coVerify(exactly = 1) { syllabusLocalDataSource.getCourseSettings(1L, false) }
    }

    @Test
    fun `Return course with syllabus when online`() = runTest {
        val expected = Course(1L, syllabusBody = "Syllabus body")

        every { networkStateProvider.isOnline() } returns true

        coEvery { syllabusNetworkDataSource.getCourseWithSyllabus(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) { syllabusNetworkDataSource.getCourseWithSyllabus(1L, false) }
        coVerify(exactly = 0) { syllabusLocalDataSource.getCourseWithSyllabus(any(), any()) }
    }

    @Test
    fun `Return course with syllabus when offline`() = runTest {
        val expected = Course(1L, syllabusBody = "Syllabus body")

        every { networkStateProvider.isOnline() } returns false

        coEvery { syllabusLocalDataSource.getCourseWithSyllabus(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) { syllabusLocalDataSource.getCourseWithSyllabus(1L, false) }
        coVerify(exactly = 0) { syllabusNetworkDataSource.getCourseWithSyllabus(any(), any()) }
    }

    @Test
    fun `Return failed result for course if network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery { syllabusNetworkDataSource.getCourseWithSyllabus(any(), any()) } returns DataResult.Fail()

        val result = repository.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return failed result for course if db error`() = runTest {
        every { networkStateProvider.isOnline() } returns false

        coEvery { syllabusLocalDataSource.getCourseWithSyllabus(any(), any()) } returns DataResult.Fail()

        val result = repository.getCourseWithSyllabus(1L, false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return events when online`() = runTest {
        val expected = listOf(
            ScheduleItem("assignment_1", type = "assignment"),
            ScheduleItem("assignment_2", type = "assignment")
        )

        every { networkStateProvider.isOnline() } returns true

        coEvery {
            syllabusNetworkDataSource.getCalendarEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(expected)

        val result = repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, listOf("course_1"), false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) { syllabusNetworkDataSource.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, listOf("course_1"), false) }
        coVerify(exactly = 0) { syllabusLocalDataSource.getCalendarEvents(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Return events when offline`() = runTest {
        val expected = listOf(
            ScheduleItem("assignment_1", type = "assignment"),
            ScheduleItem("assignment_2", type = "assignment")
        )

        every { networkStateProvider.isOnline() } returns false

        coEvery {
            syllabusLocalDataSource.getCalendarEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(expected)

        val result = repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, listOf("course_1"), false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) { syllabusLocalDataSource.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, listOf("course_1"), false) }
        coVerify(exactly = 0) { syllabusNetworkDataSource.getCalendarEvents(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Return failed result for events on network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery {
            syllabusNetworkDataSource.getCalendarEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        val result = repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, listOf("course_1"), false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return failed result for events on db error`() = runTest {
        every { networkStateProvider.isOnline() } returns false

        coEvery {
            syllabusLocalDataSource.getCalendarEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        val result = repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, listOf("course_1"), false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return planner items when online`() = runTest {
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
        val expected = listOf(
            PlannerItem(
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
        )

        every { networkStateProvider.isOnline() } returns true

        coEvery {
            syllabusNetworkDataSource.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(expected)

        val result = repository.getPlannerItems(null, null, listOf("course_1"), null, false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) { syllabusNetworkDataSource.getPlannerItems(null, null, listOf("course_1"), null, false) }
        coVerify(exactly = 0) { syllabusLocalDataSource.getPlannerItems(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Return planner items when offline`() = runTest {
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
        val expected = listOf(
            PlannerItem(
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
        )

        every { networkStateProvider.isOnline() } returns false

        coEvery {
            syllabusLocalDataSource.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(expected)

        val result = repository.getPlannerItems(null, null, listOf("course_1"), null, false)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) { syllabusLocalDataSource.getPlannerItems(null, null, listOf("course_1"), null, false) }
        coVerify(exactly = 0) { syllabusNetworkDataSource.getPlannerItems(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Return failed result for planner items on network error`() = runTest {
        every { networkStateProvider.isOnline() } returns true

        coEvery {
            syllabusNetworkDataSource.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        val result = repository.getPlannerItems(null, null, listOf("course_1"), null, false)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return failed result for planner items on db error`() = runTest {
        every { networkStateProvider.isOnline() } returns false

        coEvery {
            syllabusLocalDataSource.getPlannerItems(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        val result = repository.getPlannerItems(null, null, listOf("course_1"), null, false)

        assertEquals(DataResult.Fail(), result)
    }
}