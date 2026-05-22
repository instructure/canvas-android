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
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.horizon.domain.usecase.inbox.GetConversationsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxCoursesUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxRecipientsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HorizonInboxListRepositoryTest {
    private val getConversationsUseCase: GetConversationsUseCase = mockk(relaxed = true)
    private val getInboxRecipientsUseCase: GetInboxRecipientsUseCase = mockk(relaxed = true)
    private val getInboxCoursesUseCase: GetInboxCoursesUseCase = mockk(relaxed = true)
    private val inboxRepository: InboxRepository = mockk(relaxed = true)
    private val announcementsApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface = mockk(relaxed = true)

    private fun getRepository() = HorizonInboxListRepository(
        getConversationsUseCase,
        getInboxRecipientsUseCase,
        getInboxCoursesUseCase,
        inboxRepository,
        announcementsApi,
        accountNotificationApi,
    )

    @Test
    fun `getConversations delegates to use case`() = runTest {
        val conversations = listOf(Conversation(id = 1L, subject = "Convo"))
        coEvery { getConversationsUseCase(any()) } returns conversations

        val result = getRepository().getConversations(forceNetwork = true)

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
        coEvery { getInboxRecipientsUseCase(any()) } returns recipients

        val result = getRepository().getRecipients(searchQuery = "p", forceNetwork = false)

        assertEquals(recipients, result)
        coVerify {
            getInboxRecipientsUseCase(
                GetInboxRecipientsUseCase.Params(searchQuery = "p", forceNetwork = false)
            )
        }
    }

    @Test
    fun `getCourseAnnouncements with no courses returns empty`() = runTest {
        coEvery { getInboxCoursesUseCase(any()) } returns emptyList()

        val result = getRepository().getCourseAnnouncements(false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCourseAnnouncements maps to course-announcement pairs`() = runTest {
        val course = Course(id = 1L, name = "Course 1")
        val announcement = DiscussionTopicHeader(id = 1L, title = "Hello", contextCode = course.contextId)
        coEvery { getInboxCoursesUseCase(any()) } returns listOf(course)
        coEvery { announcementsApi.getFirstPageAnnouncements(any(), params = any()) } returns DataResult.Success(listOf(announcement))

        val result = getRepository().getCourseAnnouncements(false)

        assertEquals(1, result.size)
        assertEquals(course, result.first().first)
        assertEquals(announcement, result.first().second)
    }

    @Test
    fun `getAccountAnnouncements depaginates from api`() = runTest {
        val notifications = listOf(AccountNotification(id = 1L, subject = "S"))
        coEvery { accountNotificationApi.getAccountNotifications(any(), any(), any()) } returns DataResult.Success(notifications)

        val result = getRepository().getAccountAnnouncements(false)

        assertEquals(notifications, result)
    }

    @Test
    fun `invalidate delegates to inbox repository`() {
        getRepository().invalidateConversationListCachedResponse()

        verify { inboxRepository.invalidateConversationListCachedResponse() }
    }
}
