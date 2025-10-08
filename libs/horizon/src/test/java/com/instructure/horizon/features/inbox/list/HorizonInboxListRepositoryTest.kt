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
package com.instructure.horizon.features.inbox.list

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HorizonInboxListRepositoryTest {
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val inboxApi: InboxApi.InboxInterface = mockk(relaxed = true)
    private val recipientsApi: RecipientAPI.RecipientInterface = mockk(relaxed = true)
    private val announcementsApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val userId = 1L

    @Before
    fun setup() {
        every { apiPrefs.user } returns User(id = userId, name = "Test User")
    }

    @Test
    fun `Test successful conversations retrieval`() = runTest {
        val conversations = listOf(
            Conversation(id = 1L, subject = "Conversation 1", lastMessage = "Message 1"),
            Conversation(id = 2L, subject = "Conversation 2", lastMessage = "Message 2")
        )
        coEvery { inboxApi.getConversations(any(), any()) } returns DataResult.Success(conversations)

        val result = getRepository().getConversations(forceNetwork = false)

        assertEquals(2, result.size)
        assertEquals(conversations, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed conversations retrieval throws exception`() = runTest {
        coEvery { inboxApi.getConversations(any(), any()) } returns DataResult.Fail()

        getRepository().getConversations(forceNetwork = false)
    }

    @Test
    fun `Test successful recipients retrieval filters by person type`() = runTest {
        val recipients = listOf(
            Recipient(stringId = "1", name = "Person 1"),
            Recipient(stringId = "3", name = "Person 2")
        )
        coEvery { recipientsApi.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Success(recipients)

        val result = getRepository().getRecipients("query", false)

        assertEquals(2, result.size)
        assertTrue(result.all { it.recipientType == Recipient.Type.Person })
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed recipients retrieval throws exception`() = runTest {
        coEvery { recipientsApi.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Fail()

        getRepository().getRecipients("query", false)
    }

    @Test
    fun `Test successful course announcements retrieval`() = runTest {
        val course = Course(id = 1L, name = "Course 1",)
        val announcement = DiscussionTopicHeader(id = 1L, title = "Announcement 1", contextCode = "course_1")

        coEvery { courseApi.getFirstPageCoursesInbox(any()) } returns DataResult.Success(listOf(course))
        coEvery { announcementsApi.getFirstPageAnnouncements(any(), startDate = any(), endDate = any(), params = any()) } returns
            DataResult.Success(listOf(announcement))

        val result = getRepository().getCourseAnnouncements(false)

        assertEquals(1, result.size)
        assertEquals(course, result[0].first)
        assertEquals(announcement, result[0].second)
    }

    @Test
    fun `Test course announcements with no courses returns empty list`() = runTest {
        coEvery { courseApi.getFirstPageCoursesInbox(any()) } returns DataResult.Success(emptyList())

        val result = getRepository().getCourseAnnouncements(false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Test successful account announcements retrieval`() = runTest {
        val notifications = listOf(
            AccountNotification(id = 1L, subject = "Notification 1"),
            AccountNotification(id = 2L, subject = "Notification 2")
        )
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns
            DataResult.Success(notifications)

        val result = getRepository().getAccountAnnouncements(false)

        assertEquals(2, result.size)
        assertEquals(notifications, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed account announcements retrieval throws exception`() = runTest {
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Fail()

        getRepository().getAccountAnnouncements(false)
    }

    @Test
    fun `Test conversations scope filter`() = runTest {
        val conversations = listOf(Conversation(id = 1L, subject = "Test"))
        coEvery { inboxApi.getConversations(any(), any()) } returns DataResult.Success(conversations)

        getRepository().getConversations(InboxApi.Scope.SENT, false)

        coEvery { inboxApi.getConversations("sent", any()) }
    }

    private fun getRepository(): HorizonInboxListRepository {
        return HorizonInboxListRepository(
            apiPrefs,
            inboxApi,
            recipientsApi,
            announcementsApi,
            accountNotificationApi,
            courseApi
        )
    }
}
