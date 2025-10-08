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

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
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

class HorizonInboxComposeRepositoryTest {
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val recipientApi: RecipientAPI.RecipientInterface = mockk(relaxed = true)
    private val inboxApi: InboxApi.InboxInterface = mockk(relaxed = true)

    private lateinit var repository: HorizonInboxComposeRepository

    private val testCourses = listOf(
        Course(id = 1L, name = "Course 1"),
        Course(id = 2L, name = "Course 2")
    )

    private val testRecipients = listOf(
        Recipient(stringId = "1", name = "Student 1"),
        Recipient(stringId = "2", name = "Student 2"),
    )

    @Before
    fun setup() {
        repository = HorizonInboxComposeRepository(courseApi, recipientApi, inboxApi)
        mockkObject(CanvasRestAdapter)
        every { CanvasRestAdapter.clearCacheUrls(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAllInboxCourses returns course list`() = runTest {
        coEvery { courseApi.getFirstPageCoursesInbox(any()) } returns DataResult.Success(testCourses)

        val result = repository.getAllInboxCourses(forceNetwork = true)

        assertEquals(2, result.size)
        assertEquals("Course 1", result.first().name)
        coVerify { courseApi.getFirstPageCoursesInbox(any()) }
    }

    @Test
    fun `getAllInboxCourses with forceNetwork false`() = runTest {
        coEvery { courseApi.getFirstPageCoursesInbox(any()) } returns DataResult.Success(testCourses)

        repository.getAllInboxCourses(forceNetwork = false)

        coVerify { courseApi.getFirstPageCoursesInbox(match { !it.isForceReadFromNetwork }) }
    }

    @Test
    fun `getAllInboxCourses with forceNetwork true`() = runTest {
        coEvery { courseApi.getFirstPageCoursesInbox(any()) } returns DataResult.Success(testCourses)

        repository.getAllInboxCourses(forceNetwork = true)

        coVerify { courseApi.getFirstPageCoursesInbox(match { it.isForceReadFromNetwork }) }
    }

    @Test
    fun `getRecipients returns filtered list`() = runTest {
        coEvery { recipientApi.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Success(testRecipients)

        val result = repository.getRecipients(courseId = 1L, searchQuery = "student")

        assertEquals(2, result.size)
        assertTrue(result.all { it.recipientType == Recipient.Type.Person })
        coVerify { recipientApi.getFirstPageRecipientList("student", "1", any()) }
    }

    @Test
    fun `getRecipients filters out non-person recipients`() = runTest {
        coEvery { recipientApi.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Success(testRecipients)

        val result = repository.getRecipients(courseId = 1L, searchQuery = null)

        assertEquals(2, result.size)
        assertTrue(result.none { it.recipientType == Recipient.Type.Group })
    }

    @Test
    fun `getRecipients with null search query`() = runTest {
        coEvery { recipientApi.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Success(testRecipients)

        repository.getRecipients(courseId = 1L, searchQuery = null)

        coVerify { recipientApi.getFirstPageRecipientList(null, "1", any()) }
    }

    @Test
    fun `createConversation calls API with correct parameters`() = runTest {
        coEvery { inboxApi.createConversation(any(), any(), any(), any(), any(), any(), any()) } returns mockk(relaxed = true)

        repository.createConversation(
            recipientIds = listOf("1", "2"),
            body = "Test message",
            subject = "Test subject",
            contextCode = "course_1",
            attachmentIds = longArrayOf(100L, 200L),
            isBulkMessage = false
        )

        coVerify {
            inboxApi.createConversation(
                recipients = listOf("1", "2"),
                message = "Test message",
                subject = "Test subject",
                contextCode = "course_1",
                isBulk = 0,
                attachmentIds = longArrayOf(100L, 200L),
                params = any()
            )
        }
    }

    @Test
    fun `createConversation with bulk message flag`() = runTest {
        coEvery { inboxApi.createConversation(any(), any(), any(), any(), any(), any(), any()) } returns mockk(relaxed = true)

        repository.createConversation(
            recipientIds = listOf("1", "2"),
            body = "Bulk message",
            subject = "Bulk subject",
            contextCode = "course_1",
            attachmentIds = longArrayOf(),
            isBulkMessage = true
        )

        coVerify {
            inboxApi.createConversation(
                recipients = any(),
                message = any(),
                subject = any(),
                contextCode = any(),
                isBulk = 1,
                attachmentIds = any(),
                params = any()
            )
        }
    }

    @Test
    fun `invalidateConversationListCachedResponse clears cache`() {
        repository.invalidateConversationListCachedResponse()

        verify { CanvasRestAdapter.clearCacheUrls("conversations") }
    }

    @Test
    fun `createConversation with empty attachments`() = runTest {
        coEvery { inboxApi.createConversation(any(), any(), any(), any(), any(), any(), any()) } returns mockk(relaxed = true)

        repository.createConversation(
            recipientIds = listOf("1"),
            body = "Message",
            subject = "Subject",
            contextCode = "course_1",
            attachmentIds = longArrayOf(),
            isBulkMessage = false
        )

        coVerify {
            inboxApi.createConversation(
                recipients = any(),
                message = any(),
                subject = any(),
                contextCode = any(),
                isBulk = any(),
                attachmentIds = longArrayOf(),
                params = any()
            )
        }
    }

    @Test
    fun `getRecipients with forceNetwork parameter`() = runTest {
        coEvery { recipientApi.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Success(testRecipients)

        repository.getRecipients(courseId = 1L, searchQuery = "test", forceNetwork = true)

        coVerify { recipientApi.getFirstPageRecipientList(any(), any(), match { it.isForceReadFromNetwork }) }
    }
}
