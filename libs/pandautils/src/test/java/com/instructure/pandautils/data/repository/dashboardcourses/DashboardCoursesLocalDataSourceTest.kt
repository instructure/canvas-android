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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DashboardCoursesLocalDataSourceTest {

    private val courseFacade: CourseFacade = mockk()
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade = mockk()

    private lateinit var dataSource: DashboardCoursesLocalDataSource

    @Before
    fun setup() {
        coEvery { discussionTopicHeaderFacade.getAnnouncementsForCourse(any()) } returns emptyList()
        dataSource = DashboardCoursesLocalDataSource(courseFacade, discussionTopicHeaderFacade)
    }

    @Test
    fun `getCourses returns synced courses from the offline database`() = runTest {
        val courses = listOf(Course(id = 1), Course(id = 2))
        coEvery { courseFacade.getAllCourses() } returns courses

        val result = dataSource.getCourses(false)

        assertEquals(courses, result.courses)
    }

    @Test
    fun `unread synced announcements are included per course`() = runTest {
        coEvery { courseFacade.getAllCourses() } returns listOf(Course(id = 1))
        coEvery { discussionTopicHeaderFacade.getAnnouncementsForCourse(1) } returns listOf(
            announcement(id = 10, readState = "unread", unreadCount = 0)
        )

        val result = dataSource.getCourses(false)

        assertEquals(listOf(10L), result.announcementsMap[1L]?.map { it.id })
    }

    @Test
    fun `read synced announcement with unread replies is included`() = runTest {
        coEvery { courseFacade.getAllCourses() } returns listOf(Course(id = 1))
        coEvery { discussionTopicHeaderFacade.getAnnouncementsForCourse(1) } returns listOf(
            announcement(id = 10, readState = "read", unreadCount = 3)
        )

        val result = dataSource.getCourses(false)

        assertEquals(listOf(10L), result.announcementsMap[1L]?.map { it.id })
    }

    @Test
    fun `fully read synced announcement with no unread replies is filtered out`() = runTest {
        coEvery { courseFacade.getAllCourses() } returns listOf(Course(id = 1))
        coEvery { discussionTopicHeaderFacade.getAnnouncementsForCourse(1) } returns listOf(
            announcement(id = 10, readState = "read", unreadCount = 0)
        )

        val result = dataSource.getCourses(false)

        assertTrue(result.announcementsMap[1L].isNullOrEmpty())
    }

    private fun announcement(id: Long, readState: String, unreadCount: Int) = DiscussionTopicHeader(
        id = id,
        readState = readState,
        unreadCount = unreadCount,
        announcement = true
    )
}
