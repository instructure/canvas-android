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

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.horizon.domain.usecase.inbox.GetConversationsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxAccountAnnouncementsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxCourseAnnouncementsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxRecipientsUseCase
import com.instructure.horizon.domain.usecase.inbox.InvalidateConversationListCacheUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The old HorizonInboxListRepository was a thin pass-through over inbox use cases plus
 * a couple of direct AnnouncementAPI / AccountNotificationAPI calls that now live on
 * InboxRepository and are exposed via GetInboxCourseAnnouncementsUseCase /
 * GetInboxAccountAnnouncementsUseCase. These tests now exercise those use cases.
 */
class HorizonInboxListRepositoryTest {
    private val inboxRepository: InboxRepository = mockk(relaxed = true)
    private val getConversationsUseCase: GetConversationsUseCase = mockk(relaxed = true)
    private val getInboxRecipientsUseCase = GetInboxRecipientsUseCase(inboxRepository)
    private val getInboxCourseAnnouncementsUseCase = GetInboxCourseAnnouncementsUseCase(inboxRepository)
    private val getInboxAccountAnnouncementsUseCase = GetInboxAccountAnnouncementsUseCase(inboxRepository)
    private val invalidateConversationListCacheUseCase = InvalidateConversationListCacheUseCase(inboxRepository)

    @Test
    fun `getConversations delegates to use case`() = runTest {
        val conversations = listOf(Conversation(id = 1L, subject = "Convo"))
        coEvery { getConversationsUseCase(any()) } returns conversations

        val result = getConversationsUseCase(
            GetConversationsUseCase.Params(scope = InboxApi.Scope.INBOX, forceNetwork = true)
        )

        assertEquals(conversations, result)
        coVerify {
            getConversationsUseCase(
                GetConversationsUseCase.Params(scope = InboxApi.Scope.INBOX, forceNetwork = true)
            )
        }
    }

    @Test
    fun `getRecipients delegates to use case`() = runTest {
        val recipients = listOf(Recipient(stringId = "1", name = "P1"))
        coEvery { inboxRepository.getRecipientsForUserContext(any(), any()) } returns recipients

        val result = getInboxRecipientsUseCase(
            GetInboxRecipientsUseCase.Params(searchQuery = "p", forceNetwork = false)
        )

        assertEquals(recipients, result)
        coVerify { inboxRepository.getRecipientsForUserContext("p", false) }
    }

    @Test
    fun `getCourseAnnouncements with no courses returns empty`() = runTest {
        coEvery { inboxRepository.getInboxCourseAnnouncements(any()) } returns emptyList()

        val result = getInboxCourseAnnouncementsUseCase(false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCourseAnnouncements returns course-announcement pairs`() = runTest {
        val course = Course(id = 1L, name = "Course 1")
        val announcement = DiscussionTopicHeader(id = 1L, title = "Hello", contextCode = course.contextId)
        coEvery { inboxRepository.getInboxCourseAnnouncements(false) } returns listOf(course to announcement)

        val result = getInboxCourseAnnouncementsUseCase(false)

        assertEquals(1, result.size)
        assertEquals(course, result.first().first)
        assertEquals(announcement, result.first().second)
    }

    @Test
    fun `getAccountAnnouncements returns notifications from repository`() = runTest {
        val notifications = listOf(AccountNotification(id = 1L, subject = "S"))
        coEvery { inboxRepository.getInboxAccountAnnouncements(false) } returns notifications

        val result = getInboxAccountAnnouncementsUseCase(false)

        assertEquals(notifications, result)
    }

    @Test
    fun `invalidate delegates to inbox repository`() {
        invalidateConversationListCacheUseCase()

        verify { inboxRepository.invalidateConversationListCachedResponse() }
    }
}
