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
package com.instructure.horizon.features.inbox.details

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class HorizonInboxDetailsRepositoryTest {
    private val inboxApi: InboxApi.InboxInterface = mockk(relaxed = true)
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface = mockk(relaxed = true)
    private val announcementApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)
    private val discussionApi: DiscussionAPI.DiscussionInterface = mockk(relaxed = true)

    private lateinit var repository: HorizonInboxDetailsRepository

    private val testConversation = Conversation(
        id = 1L,
        subject = "Test Conversation",
        participants = mutableListOf(BasicUser(id = 1L, name = "User 1"))
    )

    private val testAccountNotification = AccountNotification(
        id = 100L,
        subject = "Account Announcement",
        message = "Test message"
    )

    private val testAnnouncement = DiscussionTopicHeader(
        id = 200L,
        title = "Course Announcement",
        message = "Announcement message",
        author = DiscussionParticipant(id = 1L, displayName = "Teacher")
    )

    private val testDiscussionTopic = DiscussionTopic(
        views = mutableListOf(
            DiscussionEntry(id = 1L, message = "Reply 1"),
            DiscussionEntry(id = 2L, message = "Reply 2")
        )
    )

    @Before
    fun setup() {
        repository = HorizonInboxDetailsRepository(inboxApi, accountNotificationApi, announcementApi, discussionApi)
        mockkObject(CanvasRestAdapter)
        every { CanvasRestAdapter.clearCacheUrls(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getConversation returns conversation`() = runTest {
        coEvery { inboxApi.getConversation(any(), any(), any()) } returns DataResult.Success(testConversation)

        val result = repository.getConversation(id = 1L, forceRefresh = false)

        assertEquals("Test Conversation", result.subject)
        coVerify { inboxApi.getConversation(1L, true, any()) }
    }

    @Test
    fun `getConversation with forceRefresh true`() = runTest {
        coEvery { inboxApi.getConversation(any(), any(), any()) } returns DataResult.Success(testConversation)

        repository.getConversation(id = 1L, forceRefresh = true)

        coVerify { inboxApi.getConversation(any(), any(), match { it.isForceReadFromNetwork }) }
    }

    @Test
    fun `getConversation with markAsRead false`() = runTest {
        coEvery { inboxApi.getConversation(any(), any(), any()) } returns DataResult.Success(testConversation)

        repository.getConversation(id = 1L, markAsRead = false, forceRefresh = false)

        coVerify { inboxApi.getConversation(1L, false, any()) }
    }

    @Test
    fun `getAccountAnnouncement returns filtered announcement`() = runTest {
        val announcements = listOf(
            testAccountNotification,
            AccountNotification(id = 101L, subject = "Other", message = "Other message")
        )
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(announcements)

        val result = repository.getAccountAnnouncement(id = 100L, forceRefresh = false)

        assertEquals("Account Announcement", result.subject)
        coVerify { accountNotificationApi.getAccountNotifications(any(), true, true) }
    }

    @Test
    fun `getAnnouncement returns course announcement`() = runTest {
        coEvery { announcementApi.getCourseAnnouncement(any(), any(), any()) } returns DataResult.Success(testAnnouncement)

        val result = repository.getAnnouncement(id = 200L, courseId = 1L, forceRefresh = false)

        assertEquals("Course Announcement", result.title)
        coVerify { announcementApi.getCourseAnnouncement(1L, 200L, any()) }
    }

    @Test
    fun `getAnnouncementTopic returns discussion topic`() = runTest {
        coEvery { discussionApi.getFullDiscussionTopic(any(), any(), any(), any(), any()) } returns DataResult.Success(testDiscussionTopic)

        val result = repository.getAnnouncementTopic(id = 200L, courseId = 1L, forceRefresh = false)

        assertEquals(2, result.views.size)
        coVerify { discussionApi.getFullDiscussionTopic("courses", 1L, 200L, 1, any()) }
    }

    @Test
    fun `markAnnouncementAsRead marks topic and entries as read`() = runTest {
        coEvery { discussionApi.markDiscussionTopicRead(any(), any(), any(), any()) } returns DataResult.Success(Unit)
        coEvery { discussionApi.markDiscussionTopicEntryRead(any(), any(), any(), any(), any()) } returns DataResult.Success(Unit)

        val result = repository.markAnnouncementAsRead(
            courseId = 1L,
            announcementId = 200L,
            entries = setOf(1L, 2L)
        )

        assertTrue(result is DataResult.Success)
        coVerify { discussionApi.markDiscussionTopicRead("courses", 1L, 200L, any()) }
        coVerify(exactly = 2) { discussionApi.markDiscussionTopicEntryRead("courses", 1L, 200L, any(), any()) }
    }

    @Test
    fun `markAnnouncementAsRead fails if topic marking fails`() = runTest {
        coEvery { discussionApi.markDiscussionTopicRead(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = repository.markAnnouncementAsRead(
            courseId = 1L,
            announcementId = 200L,
            entries = setOf(1L, 2L)
        )

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `markAnnouncementAsRead fails if not all entries marked`() = runTest {
        coEvery { discussionApi.markDiscussionTopicRead(any(), any(), any(), any()) } returns DataResult.Success(Unit)
        coEvery { discussionApi.markDiscussionTopicEntryRead(any(), any(), any(), 1L, any()) } returns DataResult.Success(Unit)
        coEvery { discussionApi.markDiscussionTopicEntryRead(any(), any(), any(), 2L, any()) } returns DataResult.Fail()

        val result = repository.markAnnouncementAsRead(
            courseId = 1L,
            announcementId = 200L,
            entries = setOf(1L, 2L)
        )

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `addMessageToConversation adds message successfully`() = runTest {
        coEvery { inboxApi.addMessage(any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(testConversation)

        val result = repository.addMessageToConversation(
            contextCode = "course_1",
            conversationId = 1L,
            recipientIds = listOf("1", "2"),
            body = "Reply message",
            includedMessageIds = listOf(10L, 20L),
            attachmentIds = listOf(100L, 200L)
        )

        assertEquals("Test Conversation", result.subject)
        coVerify {
            inboxApi.addMessage(
                conversationId = 1L,
                recipientIds = listOf("1", "2"),
                body = "Reply message",
                includedMessageIds = longArrayOf(10L, 20L),
                attachmentIds = longArrayOf(100L, 200L),
                contextCode = "course_1",
                params = any()
            )
        }
    }

    @Test
    fun `addMessageToConversation with empty attachments`() = runTest {
        coEvery { inboxApi.addMessage(any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(testConversation)

        repository.addMessageToConversation(
            contextCode = "course_1",
            conversationId = 1L,
            recipientIds = listOf("1"),
            body = "Reply",
            includedMessageIds = listOf(),
            attachmentIds = listOf()
        )

        coVerify {
            inboxApi.addMessage(
                conversationId = any(),
                recipientIds = any(),
                body = any(),
                includedMessageIds = longArrayOf(),
                attachmentIds = longArrayOf(),
                contextCode = any(),
                params = any()
            )
        }
    }

    @Test
    fun `invalidateConversationDetailsCachedResponse clears cache`() {
        repository.invalidateConversationDetailsCachedResponse(conversationId = 1L)

        verify { CanvasRestAdapter.clearCacheUrls("conversations/1") }
    }

    @Test
    fun `markAnnouncementAsRead with empty entries succeeds`() = runTest {
        coEvery { discussionApi.markDiscussionTopicRead(any(), any(), any(), any()) } returns DataResult.Success(Unit)

        val result = repository.markAnnouncementAsRead(
            courseId = 1L,
            announcementId = 200L,
            entries = emptySet()
        )

        assertTrue(result is DataResult.Success)
        coVerify(exactly = 0) { discussionApi.markDiscussionTopicEntryRead(any(), any(), any(), any(), any()) }
    }
}
