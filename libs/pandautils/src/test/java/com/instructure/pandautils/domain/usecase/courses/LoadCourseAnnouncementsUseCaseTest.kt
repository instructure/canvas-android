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
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class LoadCourseAnnouncementsUseCaseTest {

    private val dashboardCoursesManager: DashboardCoursesManager = mockk()

    private lateinit var useCase: LoadCourseAnnouncementsUseCase

    @Before
    fun setup() {
        useCase = LoadCourseAnnouncementsUseCase(dashboardCoursesManager)
    }

    @Test
    fun `unread announcement is returned`() = runTest {
        val node = node(id = "10", title = "Unread", read = false, unreadCount = 0)
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns dataWithNodes(listOf(node))

        val result = useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 1L))

        assertEquals(1, result.size)
        assertEquals(10L, result[0].id)
        assertEquals("Unread", result[0].title)
        assertTrue(result[0].announcement)
    }

    @Test
    fun `read announcement with unread entries is returned`() = runTest {
        val node = node(id = "20", title = "Has Replies", read = true, unreadCount = 3)
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns dataWithNodes(listOf(node))

        val result = useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 1L))

        assertEquals(1, result.size)
        assertEquals(20L, result[0].id)
        assertEquals(3, result[0].unreadCount)
    }

    @Test
    fun `fully read announcement with no unread entries is filtered out`() = runTest {
        val node = node(id = "30", title = "Already Read", read = true, unreadCount = 0)
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns dataWithNodes(listOf(node))

        val result = useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 1L))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `null nodes returns empty list`() = runTest {
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns dataWithNodes(null)

        val result = useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 1L))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `null course response returns empty list`() = runTest {
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns CourseAnnouncementsQuery.Data(course = null)

        val result = useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 1L))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `multiple announcements are all mapped correctly`() = runTest {
        val nodes = listOf(
            node(id = "1", title = "First", read = false, unreadCount = 0),
            node(id = "2", title = "Second", read = true, unreadCount = 1),
            node(id = "3", title = "Third", read = true, unreadCount = 0)
        )
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns dataWithNodes(nodes)

        val result = useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 1L))

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
    }

    @Test
    fun `courseId is passed to manager`() = runTest {
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns dataWithNodes(emptyList())

        useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 42L))

        coVerify { dashboardCoursesManager.getCourseAnnouncements(42L, null, any()) }
    }

    @Test
    fun `forceNetwork param is propagated to manager`() = runTest {
        coEvery { dashboardCoursesManager.getCourseAnnouncements(any(), any(), any()) } returns dataWithNodes(emptyList())

        useCase(LoadCourseAnnouncementsUseCase.Params(courseId = 1L, forceNetwork = false))

        coVerify { dashboardCoursesManager.getCourseAnnouncements(1L, null, forceNetwork = false) }
    }

    private fun node(
        id: String,
        title: String,
        read: Boolean,
        unreadCount: Int
    ) = CourseAnnouncementsQuery.Node(
        _id = id,
        title = title,
        message = null,
        postedAt = Date(),
        participant = CourseAnnouncementsQuery.Participant(read = read),
        entryCounts = CourseAnnouncementsQuery.EntryCounts(unreadCount = unreadCount)
    )

    private fun dataWithNodes(nodes: List<CourseAnnouncementsQuery.Node?>?): CourseAnnouncementsQuery.Data {
        val announcements = CourseAnnouncementsQuery.Announcements(
            pageInfo = CourseAnnouncementsQuery.PageInfo(hasNextPage = false, endCursor = null),
            nodes = nodes
        )
        val onCourse = CourseAnnouncementsQuery.OnCourse(_id = "1", announcements = announcements)
        val course = CourseAnnouncementsQuery.Course(__typename = "Course", onCourse = onCourse)
        return CourseAnnouncementsQuery.Data(course = course)
    }
}
