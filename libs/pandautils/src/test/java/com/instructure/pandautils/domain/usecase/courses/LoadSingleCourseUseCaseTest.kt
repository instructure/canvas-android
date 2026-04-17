/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.DashboardSingleCourseQuery
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import com.instructure.canvasapi2.type.EnrollmentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class LoadSingleCourseUseCaseTest {

    private val dashboardCoursesManager: DashboardCoursesManager = mockk()

    private lateinit var useCase: LoadSingleCourseUseCase

    @Before
    fun setup() {
        useCase = LoadSingleCourseUseCase(dashboardCoursesManager)
    }

    @Test
    fun `course fields are mapped correctly`() = runTest {
        val data = buildData(
            onCourse(
                id = "42",
                name = "Test Course",
                courseCode = "TC101",
                imageUrl = "https://example.com/img.jpg",
                dashboardCard = dashboardCard(isFavorited = true, color = "#FF0000")
            )
        )
        coEvery { dashboardCoursesManager.getSingleCourse(any(), any()) } returns data

        val result = useCase(LoadSingleCourseUseCase.Params(courseId = 42))

        assertEquals(42L, result.course.id)
        assertEquals("Test Course", result.course.name)
        assertEquals("TC101", result.course.courseCode)
        assertEquals("https://example.com/img.jpg", result.course.imageUrl)
        assertEquals("#FF0000", result.course.courseColor)
        assertTrue(result.course.isFavorite)
    }

    @Test
    fun `enrollment grades are mapped correctly`() = runTest {
        val grades = DashboardSingleCourseQuery.Grades(currentGrade = "B+", currentScore = 88.0)
        val enrollmentNode = DashboardSingleCourseQuery.Node(
            _id = "100",
            type = EnrollmentType.StudentEnrollment,
            grades = grades
        )
        val enrollmentsConnection = DashboardSingleCourseQuery.EnrollmentsConnection(nodes = listOf(enrollmentNode))
        val data = buildData(
            onCourse(
                id = "1",
                name = "Course",
                enrollmentsConnection = enrollmentsConnection,
                dashboardCard = dashboardCard(isFavorited = true)
            )
        )
        coEvery { dashboardCoursesManager.getSingleCourse(any(), any()) } returns data

        val result = useCase(LoadSingleCourseUseCase.Params(courseId = 1))

        val enrollment = result.course.enrollments?.first()
        assertEquals("B+", enrollment?.currentGrade)
        assertEquals(88.0, enrollment?.currentScore)
    }

    @Test
    fun `unread announcements are returned`() = runTest {
        val announcementNode = DashboardSingleCourseQuery.Node1(
            _id = "10",
            title = "Important Update",
            message = "Hello students",
            postedAt = Date(),
            participant = DashboardSingleCourseQuery.Participant(read = false)
        )
        val announcements = DashboardSingleCourseQuery.Announcements(
            nodes = listOf(announcementNode)
        )
        val data = buildData(
            onCourse(id = "1", name = "Course", announcements = announcements, dashboardCard = dashboardCard(isFavorited = true))
        )
        coEvery { dashboardCoursesManager.getSingleCourse(any(), any()) } returns data

        val result = useCase(LoadSingleCourseUseCase.Params(courseId = 1))

        assertEquals(1, result.announcements.size)
        assertEquals(10L, result.announcements.first().id)
        assertEquals("Important Update", result.announcements.first().title)
        assertTrue(result.announcements.first().announcement)
    }

    @Test
    fun `read announcements are filtered out`() = runTest {
        val readAnnouncement = DashboardSingleCourseQuery.Node1(
            _id = "10",
            title = "Old Announcement",
            message = "Already read",
            postedAt = Date(),
            participant = DashboardSingleCourseQuery.Participant(read = true)
        )
        val announcements = DashboardSingleCourseQuery.Announcements(
            nodes = listOf(readAnnouncement)
        )
        val data = buildData(
            onCourse(id = "1", name = "Course", announcements = announcements, dashboardCard = dashboardCard(isFavorited = true))
        )
        coEvery { dashboardCoursesManager.getSingleCourse(any(), any()) } returns data

        val result = useCase(LoadSingleCourseUseCase.Params(courseId = 1))

        assertTrue(result.announcements.isEmpty())
    }

    @Test(expected = IllegalStateException::class)
    fun `throws when course not found`() = runTest {
        val data = DashboardSingleCourseQuery.Data(course = null)
        coEvery { dashboardCoursesManager.getSingleCourse(any(), any()) } returns data

        useCase(LoadSingleCourseUseCase.Params(courseId = 999))
    }

    @Test
    fun `forceNetwork is propagated to manager`() = runTest {
        val data = buildData(onCourse(id = "1", name = "Course", dashboardCard = dashboardCard(isFavorited = true)))
        coEvery { dashboardCoursesManager.getSingleCourse(any(), any()) } returns data

        useCase(LoadSingleCourseUseCase.Params(courseId = 1, forceNetwork = true))

        coVerify { dashboardCoursesManager.getSingleCourse(courseId = 1L, forceNetwork = true) }
    }

    private fun buildData(onCourse: DashboardSingleCourseQuery.OnCourse): DashboardSingleCourseQuery.Data {
        val course = DashboardSingleCourseQuery.Course(__typename = "Course", onCourse = onCourse)
        return DashboardSingleCourseQuery.Data(course = course)
    }

    private fun onCourse(
        id: String,
        name: String,
        courseCode: String? = null,
        imageUrl: String? = null,
        dashboardCard: DashboardSingleCourseQuery.DashboardCard? = null,
        enrollmentsConnection: DashboardSingleCourseQuery.EnrollmentsConnection? = null,
        announcements: DashboardSingleCourseQuery.Announcements? = null
    ): DashboardSingleCourseQuery.OnCourse {
        return DashboardSingleCourseQuery.OnCourse(
            _id = id,
            name = name,
            courseCode = courseCode,
            imageUrl = imageUrl,
            dashboardCard = dashboardCard,
            enrollmentsConnection = enrollmentsConnection,
            announcements = announcements
        )
    }

    private fun dashboardCard(
        isFavorited: Boolean = false,
        position: Int? = null,
        color: String? = null
    ): DashboardSingleCourseQuery.DashboardCard {
        return DashboardSingleCourseQuery.DashboardCard(
            isFavorited = isFavorited,
            position = position,
            color = color,
            image = null
        )
    }
}
