package com.instructure.pandautils.features.inbox.list/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.Assert
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxRepositoryTest {

    private val inboxApi: InboxApi.InboxInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupsApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val progressApi: ProgressAPI.ProgressInterface = mockk(relaxed = true)
    private val inboxSettingsManager: InboxSettingsManager = mockk(relaxed = true)

    private val inboxRepository = object : InboxRepository(inboxApi, groupsApi, progressApi, inboxSettingsManager) {
        override suspend fun getCourses(params: RestParams): DataResult<List<Course>> {
            return coursesApi.getFirstPageCourses(params)
        }
    }

    @Test
    fun `Get all conversations for scope if filter is null`() = runTest {
        inboxRepository.getConversations(InboxApi.Scope.INBOX, true, null)

        coVerify { inboxApi.getConversations("", RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)) }
    }

    @Test
    fun `Get filtered conversations for scope if filter is not null`() = runTest {
        inboxRepository.getConversations(InboxApi.Scope.UNREAD, true, CanvasContext.emptyCourseContext(55))

        coVerify { inboxApi.getConversationsFiltered("unread", "course_55", RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)) }
    }

    @Test
    fun `Get next page if next page is not null`() = runTest {
        inboxRepository.getConversations(InboxApi.Scope.STARRED, false, null, "http://nextpage.com")

        coVerify { inboxApi.getNextPage("http://nextpage.com", RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)) }
    }

    @Test
    fun `Batch update conversations`() = runTest {
        inboxRepository.batchUpdateConversations(listOf(16L, 55L), "archive")

        coVerify { inboxApi.batchUpdateConversations(listOf(16L, 55L), "archive") }
    }

    @Test
    fun `Get contexts return course results and don't request groups if course request is failed`() = runTest {
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Fail()

        val canvasContextsResults = inboxRepository.getCanvasContexts()

        assertEquals(DataResult.Fail(), canvasContextsResults)
        coVerify(exactly = 0) { groupsApi.getFirstPageGroups(any()) }
    }

    @Test
    fun `Get contexts returns only courses if group request is failed`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Fail()

        val canvasContextsResults = inboxRepository.getCanvasContexts()

        assertEquals(1, canvasContextsResults.dataOrNull!!.size)
        assertEquals(courses[0].id, canvasContextsResults.dataOrNull!![0].id)
    }

    @Test
    fun `Get contexts returns courses and groups if successful`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        val groups = listOf(Group(id = 63, courseId = 44, name = "First group"))
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = inboxRepository.getCanvasContexts()

        assertEquals(2, canvasContextsResults.dataOrNull!!.size)
        assertEquals(courses[0].id, canvasContextsResults.dataOrNull!![0].id)
        assertEquals(groups[0].id, canvasContextsResults.dataOrNull!![1].id)
        assertEquals(groups[0].name, canvasContextsResults.dataOrNull!![1].name)
    }

    @Test
    fun `Get contexts returns only valid groups`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        val groups = listOf(
            Group(id = 63, courseId = 44, name = "First group"),
            Group(id = 63, courseId = 33, name = "First group"), // Invalid course id
        )
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = inboxRepository.getCanvasContexts()

        assertEquals(2, canvasContextsResults.dataOrNull!!.size)
        assertEquals(courses[0].id, canvasContextsResults.dataOrNull!![0].id)
        assertEquals(groups[0].id, canvasContextsResults.dataOrNull!![1].id)
        assertEquals(groups[0].name, canvasContextsResults.dataOrNull!![1].name)
    }

    @Test
    fun `Poll progress returns on first try`() = runTest {
        val initialProgress = Progress(id = 1, workflowState = "running")
        coEvery { progressApi.getProgress(initialProgress.id.toString(), any()) } returns DataResult.Success(initialProgress.copy(workflowState = "completed"))

        val progressResult = inboxRepository.pollProgress(initialProgress)

        assertEquals(initialProgress.id, progressResult.dataOrNull!!.id)
        assertTrue(progressResult.dataOrNull!!.isCompleted)
        coVerify(exactly = 1) { progressApi.getProgress(any(), any()) }
    }

    @Test
    fun `Poll progress returns after multiple tries`() = runTest {
        val initialProgress = Progress(id = 1, workflowState = "running")
        coEvery { progressApi.getProgress(initialProgress.id.toString(), any()) }.returnsMany(
            DataResult.Success(initialProgress.copy(workflowState = "running")),
            DataResult.Success(initialProgress.copy(workflowState = "running")),
            DataResult.Success(initialProgress.copy(workflowState = "completed"))
        )

        val progressResult = inboxRepository.pollProgress(initialProgress)

        assertEquals(initialProgress.id, progressResult.dataOrNull!!.id)
        assertTrue(progressResult.dataOrNull!!.isCompleted)
        coVerify(exactly = 3) { progressApi.getProgress(any(), any()) }
    }

    @Test
    fun `Poll progress returns failed result if call failed`() = runTest {
        val initialProgress = Progress(id = 1, workflowState = "running")
        coEvery { progressApi.getProgress(initialProgress.id.toString(), any()) } returns DataResult.Fail()

        val progressResult = inboxRepository.pollProgress(initialProgress)

        assertEquals(DataResult.Fail(), progressResult)
    }

    @Test
    fun `Poll progress times out after failing to get a completed result`() = runTest {
        val initialProgress = Progress(id = 1, workflowState = "running")
        coEvery { progressApi.getProgress(initialProgress.id.toString(), any()) } returns DataResult.Success(initialProgress.copy(workflowState = "running"))

        val progressResult = inboxRepository.pollProgress(initialProgress)

        assertEquals(DataResult.Fail(Failure.Network("Progress timed out")), progressResult)
        coVerify(exactly = 10) { progressApi.getProgress(any(), any()) }
    }

    @Test
    fun `Test update conversations`() = runTest {
        inboxRepository.updateConversation(16L, Conversation.WorkflowState.ARCHIVED)

        coVerify { inboxApi.updateConversation(16L, "archived", any<Boolean>(), any<RestParams>()) }
    }

    @Test
    fun `Get signature successfully`() = runTest {
        val expected = InboxSignatureSettings("signature", true)

        coEvery { inboxSettingsManager.getInboxSignatureSettings() } returns DataResult.Success(expected)

        inboxRepository.getInboxSignature()

        coVerify { inboxSettingsManager.getInboxSignatureSettings() }
    }
}