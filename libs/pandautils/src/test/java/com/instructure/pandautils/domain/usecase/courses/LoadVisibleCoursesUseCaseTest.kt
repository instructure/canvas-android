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

import com.instructure.canvasapi2.CourseAnnouncementsQuery
import com.instructure.canvasapi2.DashboardCoursesQuery
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import com.instructure.canvasapi2.models.DashboardCard
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

class LoadVisibleCoursesUseCaseTest {

    private val dashboardCoursesManager: DashboardCoursesManager = mockk()
    private val loadDashboardCardsUseCase: LoadDashboardCardsUseCase = mockk()

    private lateinit var useCase: LoadVisibleCoursesUseCase

    @Before
    fun setup() {
        coEvery { loadDashboardCardsUseCase(any()) } returns emptyList()
        useCase = LoadVisibleCoursesUseCase(dashboardCoursesManager, loadDashboardCardsUseCase)
    }

    @Test
    fun `visible courses match dashboard cards sorted by position`() = runTest {
        val data = buildQueryData(
            allCourse("1", "Course A"),
            allCourse("2", "Course B"),
            allCourse("3", "Course C")
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(
            DashboardCard(id = 3, position = 0),
            DashboardCard(id = 1, position = 1),
            DashboardCard(id = 2, position = 2)
        )

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(listOf(3L, 1L, 2L), result.visibleCourses.map { it.id })
    }

    @Test
    fun `only dashboard card courses are visible`() = runTest {
        val data = buildQueryData(
            allCourse("1", "On Dashboard"),
            allCourse("2", "Not on Dashboard")
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(
            DashboardCard(id = 1, position = 0)
        )

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(1, result.visibleCourses.size)
        assertEquals(1L, result.visibleCourses[0].id)
        assertEquals(2, result.allCourses.size)
    }

    @Test
    fun `empty dashboard cards returns empty visible courses`() = runTest {
        val data = buildQueryData(
            allCourse("1", "Course A"),
            allCourse("2", "Course B")
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data
        coEvery { loadDashboardCardsUseCase(any()) } returns emptyList()

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertTrue(result.visibleCourses.isEmpty())
        assertEquals(2, result.allCourses.size)
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
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(DashboardCard(id = 42, position = 0))

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        val course = result.visibleCourses[0]
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
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(DashboardCard(id = 1, position = 0))

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        val enrollment = result.visibleCourses[0].enrollments?.first()
        assertEquals("A-", enrollment?.currentGrade)
        assertEquals(92.5, enrollment?.currentScore)
    }

    @Test
    fun `unread announcements are included in announcementsMap`() = runTest {
        val announcementNode = DashboardCoursesQuery.Node1(
            _id = "10",
            title = "Important Update",
            message = "Hello students",
            postedAt = Date(),
            participant = DashboardCoursesQuery.Participant(read = false)
        )
        val announcements = DashboardCoursesQuery.Announcements(
            pageInfo = DashboardCoursesQuery.PageInfo(hasNextPage = false, endCursor = null),
            nodes = listOf(announcementNode)
        )
        val data = buildQueryData(
            allCourse(
                id = "1",
                name = "Course",
                announcements = announcements,
                dashboardCard = dashboardCard(position = 0)
            )
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        val courseAnnouncements = result.announcementsMap[1L]
        assertEquals(1, courseAnnouncements?.size)
        assertEquals(10L, courseAnnouncements?.first()?.id)
        assertEquals("Important Update", courseAnnouncements?.first()?.title)
        assertTrue(courseAnnouncements?.first()?.announcement == true)
    }

    @Test
    fun `read announcements are filtered out`() = runTest {
        val readAnnouncement = DashboardCoursesQuery.Node1(
            _id = "10",
            title = "Old Announcement",
            message = "Already read",
            postedAt = Date(),
            participant = DashboardCoursesQuery.Participant(read = true)
        )
        val announcements = DashboardCoursesQuery.Announcements(
            pageInfo = DashboardCoursesQuery.PageInfo(hasNextPage = false, endCursor = null),
            nodes = listOf(readAnnouncement)
        )
        val data = buildQueryData(
            allCourse(
                id = "1",
                name = "Course",
                announcements = announcements,
                dashboardCard = dashboardCard(position = 0)
            )
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        val courseAnnouncements = result.announcementsMap[1L]
        assertTrue(courseAnnouncements.isNullOrEmpty())
    }

    @Test
    fun `announcements are depaginated per course when hasNextPage is true`() = runTest {
        val firstPageNode = DashboardCoursesQuery.Node1(
            _id = "10",
            title = "First Page",
            message = "msg",
            postedAt = Date(),
            participant = DashboardCoursesQuery.Participant(read = false)
        )
        val announcements = DashboardCoursesQuery.Announcements(
            pageInfo = DashboardCoursesQuery.PageInfo(hasNextPage = true, endCursor = "cursor1"),
            nodes = listOf(firstPageNode)
        )
        val data = buildQueryData(
            allCourse(id = "1", name = "Course", announcements = announcements, dashboardCard = dashboardCard(position = 0))
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data
        coEvery { dashboardCoursesManager.getCourseAnnouncements(1L, any()) } returns CourseAnnouncementsQuery.Data(course = null)

        useCase(LoadVisibleCoursesUseCase.Params())

        coVerify { dashboardCoursesManager.getCourseAnnouncements(1L, any()) }
    }

    @Test
    fun `announcements are not depaginated when hasNextPage is false`() = runTest {
        val announcements = DashboardCoursesQuery.Announcements(
            pageInfo = DashboardCoursesQuery.PageInfo(hasNextPage = false, endCursor = null),
            nodes = emptyList()
        )
        val data = buildQueryData(
            allCourse(id = "1", name = "Course", announcements = announcements, dashboardCard = dashboardCard(position = 0))
        )
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        useCase(LoadVisibleCoursesUseCase.Params())

        coVerify(exactly = 0) { dashboardCoursesManager.getCourseAnnouncements(any(), any()) }
    }

    @Test
    fun `forceRefresh is propagated to manager`() = runTest {
        val data = buildQueryData()
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        useCase(LoadVisibleCoursesUseCase.Params(forceRefresh = true))

        coVerify { dashboardCoursesManager.getDashboardCourses(forceNetwork = true) }
    }

    @Test
    fun `empty allCourses response returns empty result`() = runTest {
        val data = DashboardCoursesQuery.Data(allCourses = emptyList())
        coEvery { dashboardCoursesManager.getDashboardCourses(any()) } returns data

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertTrue(result.visibleCourses.isEmpty())
        assertTrue(result.allCourses.isEmpty())
        assertTrue(result.announcementsMap.isEmpty())
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
        enrollmentsConnection: DashboardCoursesQuery.EnrollmentsConnection? = null,
        announcements: DashboardCoursesQuery.Announcements? = null
    ): DashboardCoursesQuery.AllCourse {
        return DashboardCoursesQuery.AllCourse(
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
    ): DashboardCoursesQuery.DashboardCard {
        return DashboardCoursesQuery.DashboardCard(
            isFavorited = isFavorited,
            position = position,
            color = color,
            image = null
        )
    }
}
