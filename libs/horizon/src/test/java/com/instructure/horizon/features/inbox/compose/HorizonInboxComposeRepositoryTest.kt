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
package com.instructure.horizon.features.inbox.compose

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.horizon.domain.usecase.inbox.CreateConversationUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxCoursesUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxRecipientsUseCase
import com.instructure.horizon.domain.usecase.inbox.InvalidateConversationListCacheUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The old HorizonInboxComposeRepository was a thin pass-through over inbox use cases.
 * It is gone; the view model depends on the use cases directly. These tests now
 * exercise those use cases against InboxRepository.
 */
class HorizonInboxComposeRepositoryTest {
    private val inboxRepository: InboxRepository = mockk(relaxed = true)
    private val getInboxCoursesUseCase = GetInboxCoursesUseCase(inboxRepository)
    private val getInboxRecipientsUseCase = GetInboxRecipientsUseCase(inboxRepository)
    private val createConversationUseCase = CreateConversationUseCase(inboxRepository)
    private val invalidateConversationListCacheUseCase = InvalidateConversationListCacheUseCase(inboxRepository)

    private val testCourses = listOf(
        Course(id = 1L, name = "Course 1"),
        Course(id = 2L, name = "Course 2"),
    )

    private val testRecipients = listOf(
        Recipient(stringId = "1", name = "Student 1"),
        Recipient(stringId = "2", name = "Student 2"),
    )

    @Test
    fun `getAllInboxCourses delegates to InboxRepository`() = runTest {
        coEvery { inboxRepository.getInboxCourses(any()) } returns testCourses

        val result = getInboxCoursesUseCase(GetInboxCoursesUseCase.Params(forceNetwork = true))

        assertEquals(testCourses, result)
        coVerify { inboxRepository.getInboxCourses(true) }
    }

    @Test
    fun `getRecipients delegates to InboxRepository`() = runTest {
        coEvery { inboxRepository.getRecipientsForCourse(any(), any(), any()) } returns testRecipients

        val result = getInboxRecipientsUseCase(
            GetInboxRecipientsUseCase.Params(courseId = 1L, searchQuery = "student", forceNetwork = false)
        )

        assertEquals(testRecipients, result)
        coVerify { inboxRepository.getRecipientsForCourse(1L, "student", false) }
    }

    @Test
    fun `createConversation delegates to InboxRepository and invalidates cache`() = runTest {
        createConversationUseCase(
            CreateConversationUseCase.Params(
                recipientIds = listOf("1", "2"),
                body = "Body",
                subject = "Subject",
                contextCode = "course_1",
                attachmentIds = longArrayOf(100L),
                isBulkMessage = true,
            )
        )

        coVerify {
            inboxRepository.createConversation(
                recipientIds = listOf("1", "2"),
                subject = "Subject",
                body = "Body",
                contextCode = "course_1",
                attachmentIds = longArrayOf(100L),
                isBulkMessage = true,
            )
        }
        verify { inboxRepository.invalidateConversationListCachedResponse() }
    }

    @Test
    fun `invalidateConversationListCache delegates to InboxRepository`() {
        invalidateConversationListCacheUseCase()

        verify { inboxRepository.invalidateConversationListCachedResponse() }
    }
}
