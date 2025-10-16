/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.dashboard.widget.announcement

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class DashboardAnnouncementBannerRepositoryTest {
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val announcementApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface = mockk(relaxed = true)

    @Test
    fun `Test successful unread course announcements retrieval`() = runTest {
        val courses = listOf(
            Course(id = 1L, name = "Course 1", contextId = "course_1"),
            Course(id = 2L, name = "Course 2", contextId = "course_2")
        )
        val announcements = listOf(
            createDiscussionTopicHeader("1", "Course Announcement 1", "course_1", false),
            createDiscussionTopicHeader("2", "Course Announcement 2", "course_2", false)
        )

        setupCourseMocks(courses, announcements)
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(emptyList())

        val result = getRepository().getUnreadAnnouncements(false)

        assertEquals(2, result.size)
        assertEquals("Course Announcement 1", result[0].title)
        assertEquals("Course 1", result[0].source)
        assertEquals(AnnouncementType.COURSE, result[0].type)
    }

    @Test
    fun `Test filters out read announcements`() = runTest {
        val courses = listOf(Course(id = 1L, name = "Course 1", contextId = "course_1"))
        val announcements = listOf(
            createDiscussionTopicHeader("1", "Unread Announcement", "course_1", false),
            createDiscussionTopicHeader("2", "Read Announcement", "course_1", true)
        )

        setupCourseMocks(courses, announcements)
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(emptyList())

        val result = getRepository().getUnreadAnnouncements(false)

        assertEquals(1, result.size)
        assertEquals("Unread Announcement", result[0].title)
    }

    @Test
    fun `Test successful global announcements retrieval`() = runTest {
        val user = User(id = 1L, shortName = "Test User")
        every { apiPrefs.user } returns user

        val globalAnnouncements = listOf(
            AccountNotification(id = 1L, subject = "Global Announcement 1", startDate = Date()),
            AccountNotification(id = 2L, subject = "Global Announcement 2", startDate = Date())
        )

        setupCourseMocks(emptyList(), emptyList())
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(globalAnnouncements)

        val result = getRepository().getUnreadAnnouncements(false)

        assertEquals(2, result.size)
        assertEquals("Global Announcement 1", result[0].title)
        assertEquals("Test User", result[0].source)
        assertEquals(AnnouncementType.GLOBAL, result[0].type)
    }

    @Test
    fun `Test combined course and global announcements sorted by date`() = runTest {
        val oldDate = Date(System.currentTimeMillis() - 100000)
        val newDate = Date()

        val user = User(id = 1L, shortName = "Test User")
        every { apiPrefs.user } returns user

        val courses = listOf(Course(id = 1L, name = "Course 1", contextId = "course_1"))
        val courseAnnouncements = listOf(
            createDiscussionTopicHeader("1", "Old Course Announcement", "course_1", false, oldDate)
        )

        val globalAnnouncements = listOf(
            AccountNotification(id = 1L, subject = "New Global Announcement", startDate = newDate)
        )

        setupCourseMocks(courses, courseAnnouncements)
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(globalAnnouncements)

        val result = getRepository().getUnreadAnnouncements(false)

        assertEquals(2, result.size)
        assertEquals("New Global Announcement", result[0].title)
        assertEquals("Old Course Announcement", result[1].title)
    }

    @Test
    fun `Test empty announcements list`() = runTest {
        setupCourseMocks(emptyList(), emptyList())
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(emptyList())

        val result = getRepository().getUnreadAnnouncements(false)

        assertEquals(0, result.size)
    }

    @Test
    fun `Test forceRefresh parameter is passed correctly`() = runTest {
        setupCourseMocks(emptyList(), emptyList())
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(emptyList())

        getRepository().getUnreadAnnouncements(true)

        coVerify { courseApi.getFirstPageCoursesInbox(match { it.isForceReadFromNetwork }) }
        coVerify { accountNotificationApi.getAccountNotifications(match { it.isForceReadFromNetwork }, any(), any()) }
    }

    @Test
    fun `Test empty courses list returns empty announcements`() = runTest {
        coEvery { courseApi.getFirstPageCoursesInbox(any()) } returns DataResult.Success(emptyList())
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(emptyList())

        val result = getRepository().getUnreadAnnouncements(false)

        assertEquals(0, result.size)
    }

    private fun createDiscussionTopicHeader(
        id: String,
        title: String,
        contextCode: String,
        isRead: Boolean,
        postedDate: Date = Date()
    ): DiscussionTopicHeader {
        return DiscussionTopicHeader(
            id = id.toLong(),
            title = title,
            contextCode = contextCode,
            read = isRead,
            postedDate = postedDate,
            htmlUrl = "test/url/$id"
        )
    }

    private fun setupCourseMocks(
        courses: List<Course>,
        announcements: List<DiscussionTopicHeader>
    ) {
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { courseApi.getFirstPageCoursesInbox(any()) } returns DataResult.Success(courses)
        coEvery { announcementApi.getFirstPageAnnouncements(any(), any(), any(), any()) } returns DataResult.Success(announcements)
    }

    private fun getRepository(): DashboardAnnouncementBannerRepository {
        return DashboardAnnouncementBannerRepository(
            apiPrefs,
            announcementApi,
            courseApi,
            accountNotificationApi
        )
    }
}
