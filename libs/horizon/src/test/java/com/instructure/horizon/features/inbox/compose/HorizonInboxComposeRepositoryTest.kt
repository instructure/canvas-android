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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HorizonInboxComposeRepositoryTest {
    private val getInboxCoursesUseCase: GetInboxCoursesUseCase = mockk(relaxed = true)
    private val getInboxRecipientsUseCase: GetInboxRecipientsUseCase = mockk(relaxed = true)
    private val createConversationUseCase: CreateConversationUseCase = mockk(relaxed = true)
    private val inboxRepository: InboxRepository = mockk(relaxed = true)

    private val repository = HorizonInboxComposeRepository(
        getInboxCoursesUseCase,
        getInboxRecipientsUseCase,
        createConversationUseCase,
        inboxRepository,
    )

    private val testCourses = listOf(
        Course(id = 1L, name = "Course 1"),
        Course(id = 2L, name = "Course 2"),
    )

    private val testRecipients = listOf(
        Recipient(stringId = "1", name = "Student 1"),
        Recipient(stringId = "2", name = "Student 2"),
    )

    @Test
    fun `getAllInboxCourses delegates to use case`() = runTest {
        coEvery { getInboxCoursesUseCase(any()) } returns testCourses

        val result = repository.getAllInboxCourses(forceNetwork = true)

        assertEquals(testCourses, result)
        coVerify { getInboxCoursesUseCase(GetInboxCoursesUseCase.Params(true)) }
    }

    @Test
    fun `getRecipients delegates to use case`() = runTest {
        coEvery { getInboxRecipientsUseCase(any()) } returns testRecipients

        val result = repository.getRecipients(courseId = 1L, searchQuery = "student")

        assertEquals(testRecipients, result)
        coVerify {
            getInboxRecipientsUseCase(
                GetInboxRecipientsUseCase.Params(courseId = 1L, searchQuery = "student", forceNetwork = false)
            )
        }
    }

    @Test
    fun `createConversation delegates to use case`() = runTest {
        repository.createConversation(
            recipientIds = listOf("1", "2"),
            body = "Body",
            subject = "Subject",
            contextCode = "course_1",
            attachmentIds = longArrayOf(100L),
            isBulkMessage = true,
        )

        coVerify {
            createConversationUseCase(
                match {
                    it.recipientIds == listOf("1", "2") &&
                        it.subject == "Subject" &&
                        it.body == "Body" &&
                        it.contextCode == "course_1" &&
                        it.attachmentIds.contentEquals(longArrayOf(100L)) &&
                        it.isBulkMessage
                }
            )
        }
    }

    @Test
    fun `invalidateConversationListCachedResponse delegates`() {
        repository.invalidateConversationListCachedResponse()

        verify { inboxRepository.invalidateConversationListCachedResponse() }
    }
}
