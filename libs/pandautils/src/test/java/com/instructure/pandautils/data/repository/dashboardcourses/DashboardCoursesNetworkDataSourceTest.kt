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

package com.instructure.pandautils.data.repository.dashboardcourses

import com.instructure.canvasapi2.DashboardCoursesQuery
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.type.EnrollmentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DashboardCoursesNetworkDataSourceTest {

    private val dashboardCoursesManager: DashboardCoursesManager = mockk()

    private lateinit var dataSource: DashboardCoursesNetworkDataSource

    @Before
    fun setup() {
        dataSource = DashboardCoursesNetworkDataSource(dashboardCoursesManager)
    }

    @Test
    fun `course fields are mapped correctly`() = runTest {
        val data = buildQueryData(
            allCourse(
                id = "42",
                name = "Test Course",
                courseCode = "TC101",
                imageUrl = "https://example.com/img.jpg",
                dashboardCard = dashboardCard(isFavorited = true, position = 0, color = "#FF0000")
            )
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = dataSource.getCourses(false)

        val course = result.courses[0]
        assertEquals(42L, course.id)
        assertEquals("Test Course", course.name)
        assertEquals("TC101", course.courseCode)
        assertEquals("https://example.com/img.jpg", course.imageUrl)
        assertEquals("#FF0000", course.courseColor)
        assertTrue(course.isFavorite)
    }

    @Test
    fun `enrollment grades are mapped correctly`() = runTest {
        val grades = DashboardCoursesQuery.Grades(currentGrade = "A-", currentScore = 92.5)
        val enrollmentNode = DashboardCoursesQuery.Node(
            _id = "100",
            type = EnrollmentType.StudentEnrollment,
            grades = grades
        )
        val enrollmentsConnection = DashboardCoursesQuery.EnrollmentsConnection(nodes = listOf(enrollmentNode))
        val data = buildQueryData(
            allCourse(
                id = "1",
                name = "Course",
                enrollmentsConnection = enrollmentsConnection,
                dashboardCard = dashboardCard(position = 0)
            )
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = dataSource.getCourses(false)

        val enrollment = result.courses[0].enrollments?.first()
        assertEquals("A-", enrollment?.currentGrade)
        assertEquals(92.5, enrollment?.currentScore)
    }

    @Test
    fun `announcements are not loaded by the network source`() = runTest {
        // Announcements are decoupled from the courses query and loaded separately in a capped
        // background pass, so the network data source must never populate the announcements map.
        val data = buildQueryData(
            allCourse(id = "1", name = "Course", dashboardCard = dashboardCard(position = 0))
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = dataSource.getCourses(false)

        assertTrue(result.announcementsMap.isEmpty())
        coVerify(exactly = 0) { dashboardCoursesManager.getCourseAnnouncements(any(), any()) }
    }

    @Test
    fun `forceRefresh is propagated to manager`() = runTest {
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns buildQueryData()

        dataSource.getCourses(true)

        coVerify { dashboardCoursesManager.getDashboardCourses(forceNetwork = true) }
    }

    @Test
    fun `empty allCourses response returns empty result`() = runTest {
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns DashboardCoursesQuery.Data(allCourses = emptyList())

        val result = dataSource.getCourses(false)

        assertTrue(result.courses.isEmpty())
        assertTrue(result.announcementsMap.isEmpty())
    }

    @Test
    fun `defaultView modules is mapped to HOME_MODULES and routes to MODULES_ID`() = runTest {
        val data = buildQueryData(
            allCourse(id = "1", name = "Course", dashboardCard = dashboardCard(position = 0, defaultView = "modules"))
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = dataSource.getCourses(false)

        val course = result.courses[0]
        assertEquals(Course.HomePage.HOME_MODULES, course.homePage)
        assertEquals(Tab.MODULES_ID, course.homePageID)
    }

    @Test
    fun `null defaultView maps to null homePage and routes to NOTIFICATIONS_ID`() = runTest {
        val data = buildQueryData(
            allCourse(id = "1", name = "Course", dashboardCard = dashboardCard(position = 0, defaultView = null))
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = dataSource.getCourses(false)

        val course = result.courses[0]
        assertEquals(null, course.homePage)
        assertEquals(Tab.NOTIFICATIONS_ID, course.homePageID)
    }

    private fun buildQueryData(vararg courses: DashboardCoursesQuery.AllCourse): DashboardCoursesQuery.Data {
        return DashboardCoursesQuery.Data(allCourses = courses.toList())
    }

    private fun allCourse(
        id: String,
        name: String,
        courseCode: String? = null,
        imageUrl: String? = null,
        dashboardCard: DashboardCoursesQuery.DashboardCard? = null,
        enrollmentsConnection: DashboardCoursesQuery.EnrollmentsConnection? = null
    ): DashboardCoursesQuery.AllCourse {
        return DashboardCoursesQuery.AllCourse(
            _id = id,
            name = name,
            courseCode = courseCode,
            imageUrl = imageUrl,
            dashboardCard = dashboardCard,
            enrollmentsConnection = enrollmentsConnection
        )
    }

    private fun dashboardCard(
        isFavorited: Boolean = false,
        position: Int? = null,
        color: String? = null,
        defaultView: String? = null
    ): DashboardCoursesQuery.DashboardCard {
        return DashboardCoursesQuery.DashboardCard(
            isFavorited = isFavorited,
            position = position,
            color = color,
            image = null,
            defaultView = defaultView
        )
    }
}
