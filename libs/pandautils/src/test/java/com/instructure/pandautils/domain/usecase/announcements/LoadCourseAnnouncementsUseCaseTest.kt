/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.announcements

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.course.CourseRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadCourseAnnouncementsUseCaseTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val useCase = LoadCourseAnnouncementsUseCase(courseRepository)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute returns only unread announcements with unreadCount greater than 0`() = runTest {
        val courseId = 1L
        val params = LoadCourseAnnouncementsParams(courseId)

        val announcements = listOf(
            DiscussionTopicHeader(id = 1, unreadCount = 2, readState = DiscussionTopicHeader.ReadState.READ.name),
            DiscussionTopicHeader(id = 2, unreadCount = 0, readState = DiscussionTopicHeader.ReadState.READ.name),
            DiscussionTopicHeader(id = 3, unreadCount = 1, readState = DiscussionTopicHeader.ReadState.READ.name)
        )

        coEvery {
            courseRepository.getCourseAnnouncements(courseId, any())
        } returns DataResult.Success(announcements)

        val result = useCase(params)

        assertEquals(2, result.size)
        assertTrue(result.all { it.unreadCount > 0 })
        assertEquals(1L, result[0].id)
        assertEquals(3L, result[1].id)
    }

    @Test
    fun `execute returns announcements with UNREAD status`() = runTest {
        val courseId = 1L
        val params = LoadCourseAnnouncementsParams(courseId)

        val announcements = listOf(
            DiscussionTopicHeader(id = 1, unreadCount = 0, readState = DiscussionTopicHeader.ReadState.UNREAD.name),
            DiscussionTopicHeader(id = 2, unreadCount = 0, readState = DiscussionTopicHeader.ReadState.READ.name)
        )

        coEvery {
            courseRepository.getCourseAnnouncements(courseId, any())
        } returns DataResult.Success(announcements)

        val result = useCase(params)

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(DiscussionTopicHeader.ReadState.UNREAD.name, result[0].readState)
    }

    @Test
    fun `execute returns announcements with either unreadCount or UNREAD status`() = runTest {
        val courseId = 1L
        val params = LoadCourseAnnouncementsParams(courseId)

        val announcements = listOf(
            DiscussionTopicHeader(id = 1, unreadCount = 2, readState = DiscussionTopicHeader.ReadState.READ.name),
            DiscussionTopicHeader(id = 2, unreadCount = 0, readState = DiscussionTopicHeader.ReadState.UNREAD.name),
            DiscussionTopicHeader(id = 3, unreadCount = 0, readState = DiscussionTopicHeader.ReadState.READ.name),
            DiscussionTopicHeader(id = 4, unreadCount = 1, readState = DiscussionTopicHeader.ReadState.UNREAD.name)
        )

        coEvery {
            courseRepository.getCourseAnnouncements(courseId, any())
        } returns DataResult.Success(announcements)

        val result = useCase(params)

        assertEquals(3, result.size)
        assertEquals(listOf(1L, 2L, 4L), result.map { it.id })
    }

    @Test
    fun `execute returns empty list when no unread announcements`() = runTest {
        val courseId = 1L
        val params = LoadCourseAnnouncementsParams(courseId)

        val announcements = listOf(
            DiscussionTopicHeader(id = 1, unreadCount = 0, readState = DiscussionTopicHeader.ReadState.READ.name),
            DiscussionTopicHeader(id = 2, unreadCount = 0, readState = DiscussionTopicHeader.ReadState.READ.name)
        )

        coEvery {
            courseRepository.getCourseAnnouncements(courseId, any())
        } returns DataResult.Success(announcements)

        val result = useCase(params)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute returns empty list when API returns empty list`() = runTest {
        val courseId = 1L
        val params = LoadCourseAnnouncementsParams(courseId)

        coEvery {
            courseRepository.getCourseAnnouncements(courseId, any())
        } returns DataResult.Success(emptyList())

        val result = useCase(params)

        assertTrue(result.isEmpty())
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws exception when API call fails`() = runTest {
        val courseId = 1L
        val params = LoadCourseAnnouncementsParams(courseId)

        coEvery {
            courseRepository.getCourseAnnouncements(courseId, any())
        } returns DataResult.Fail()

        useCase(params)
    }
}