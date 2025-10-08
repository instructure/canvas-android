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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.comments

import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.Comment
import com.instructure.canvasapi2.managers.CommentsData
import com.instructure.canvasapi2.managers.HorizonGetCommentsManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class CommentsRepositoryTest {
    private val getCommentsManager: HorizonGetCommentsManager = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)

    private lateinit var repository: CommentsRepository

    private val testCommentsData = CommentsData(
        comments = listOf(
            Comment(
                authorId = 100L,
                authorName = "Student",
                createdAt = Date(),
                commentText = "Test comment",
                read = true,
                attachments = emptyList()
            )
        ),
        endCursor = "cursor-end",
        startCursor = "cursor-start",
        hasNextPage = true,
        hasPreviousPage = false
    )

    private val testSubmission = Submission(
        id = 1L,
        attempt = 1,
        userId = 100L
    )

    @Before
    fun setup() {
        repository = CommentsRepository(getCommentsManager, submissionApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getComments returns comments data successfully`() = runTest {
        coEvery { getCommentsManager.getComments(any(), any(), any(), any(), any(), any(), any()) } returns testCommentsData

        val result = repository.getComments(
            assignmentId = 1L,
            userId = 100L,
            attempt = 1,
            forceNetwork = false
        )

        assertEquals(1, result.comments.size)
        assertEquals("Test comment", result.comments.first().commentText)
        assertEquals("cursor-end", result.endCursor)
        assertTrue(result.hasNextPage)
        coVerify { getCommentsManager.getComments(1L, 100L, 1, false, false, null, null) }
    }

    @Test
    fun `getComments with pagination parameters`() = runTest {
        coEvery { getCommentsManager.getComments(any(), any(), any(), any(), any(), any(), any()) } returns testCommentsData

        repository.getComments(
            assignmentId = 1L,
            userId = 100L,
            attempt = 1,
            forceNetwork = true,
            startCursor = "start",
            endCursor = "end",
            nextPage = true
        )

        coVerify { getCommentsManager.getComments(1L, 100L, 1, true, true,"end", "start") }
    }

    @Test
    fun `getComments with forceNetwork true`() = runTest {
        coEvery { getCommentsManager.getComments(any(), any(), any(), any(), any(), any(), any()) } returns testCommentsData

        repository.getComments(
            assignmentId = 1L,
            userId = 100L,
            attempt = 1,
            forceNetwork = true
        )

        coVerify { getCommentsManager.getComments(any(), any(), any(), any(), true, any(), any()) }
    }

    @Test
    fun `getComments for next page`() = runTest {
        coEvery { getCommentsManager.getComments(any(), any(), any(), any(), any(), any(), any()) } returns testCommentsData

        repository.getComments(
            assignmentId = 1L,
            userId = 100L,
            attempt = 1,
            forceNetwork = false,
            endCursor = "cursor-end",
            nextPage = true
        )

        coVerify { getCommentsManager.getComments(any(), any(), any(), true, false, "cursor-end", null) }
    }

    @Test
    fun `getComments for previous page`() = runTest {
        coEvery { getCommentsManager.getComments(any(), any(), any(), any(), any(), any(), any()) } returns testCommentsData

        repository.getComments(
            assignmentId = 1L,
            userId = 100L,
            attempt = 1,
            forceNetwork = false,
            startCursor = "cursor-start",
            nextPage = false
        )

        coVerify { getCommentsManager.getComments(any(), any(), any(), false, false, null, "cursor-start") }
    }

    @Test
    fun `postComment returns success result`() = runTest {
        coEvery { submissionApi.postSubmissionComment(any(), any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(testSubmission)

        val result = repository.postComment(
            courseId = 1L,
            assignmentId = 10L,
            userId = 100L,
            attempt = 1,
            commentText = "My comment"
        )

        assertTrue(result is DataResult.Success)
        coVerify {
            submissionApi.postSubmissionComment(
                courseId = 1L,
                assignmentId = 10L,
                userId = 100L,
                comment = "My comment",
                attemptId = 1L,
                isGroupComment = false,
                attachments = listOf(),
                restParams = any<RestParams>()
            )
        }
    }

    @Test
    fun `postComment with different attempt`() = runTest {
        coEvery { submissionApi.postSubmissionComment(any(), any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(testSubmission)

        repository.postComment(
            courseId = 1L,
            assignmentId = 10L,
            userId = 100L,
            attempt = 5,
            commentText = "Comment on attempt 5"
        )

        coVerify {
            submissionApi.postSubmissionComment(
                courseId = any(),
                assignmentId = any(),
                userId = any(),
                comment = any(),
                attemptId = 5L,
                isGroupComment = any(),
                attachments = any(),
                restParams = any(),
            )
        }
    }

    @Test
    fun `postComment returns failure result`() = runTest {
        coEvery { submissionApi.postSubmissionComment(any(), any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Fail()

        val result = repository.postComment(
            courseId = 1L,
            assignmentId = 10L,
            userId = 100L,
            attempt = 1,
            commentText = "My comment"
        )

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getComments with attempt 0`() = runTest {
        coEvery { getCommentsManager.getComments(any(), any(), any(), any(), any(), any(), any()) } returns testCommentsData

        repository.getComments(
            assignmentId = 1L,
            userId = 100L,
            attempt = 0,
            forceNetwork = false
        )

        coVerify { getCommentsManager.getComments(1L, 100L, 0, false, false, null, null) }
    }

    @Test
    fun `postComment always sets isGroupComment to false`() = runTest {
        coEvery { submissionApi.postSubmissionComment(any(), any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(testSubmission)

        repository.postComment(
            courseId = 1L,
            assignmentId = 10L,
            userId = 100L,
            attempt = 1,
            commentText = "Comment"
        )

        coVerify {
            submissionApi.postSubmissionComment(
                courseId = any(),
                assignmentId = any(),
                userId = any(),
                comment = any(),
                attemptId = any(),
                isGroupComment = false,
                attachments = any(),
                restParams = any(),
            )
        }
    }

    @Test
    fun `postComment always sends empty attachments list`() = runTest {
        coEvery { submissionApi.postSubmissionComment(any(), any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(testSubmission)

        repository.postComment(
            courseId = 1L,
            assignmentId = 10L,
            userId = 100L,
            attempt = 1,
            commentText = "Comment"
        )

        coVerify {
            submissionApi.postSubmissionComment(
                courseId = any(),
                assignmentId = any(),
                userId = any(),
                comment = any(),
                attemptId = 1L,
                isGroupComment = any(),
                attachments = emptyList(),
                restParams = any(),
            )
        }
    }
}
