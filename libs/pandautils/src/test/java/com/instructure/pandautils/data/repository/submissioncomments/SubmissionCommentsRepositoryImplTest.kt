/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.data.repository.submissioncomments

import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.MarkSubmissionCommentsReadMutation
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class SubmissionCommentsRepositoryImplTest {

    private val submissionCommentsManager: SubmissionCommentsManager = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)

    private lateinit var repository: SubmissionCommentsRepositoryImpl

    @Before
    fun setUp() {
        repository = SubmissionCommentsRepositoryImpl(submissionCommentsManager, submissionApi)
    }

    @Test
    fun `getSubmissionComments delegates to the manager and returns its result`() = runTest {
        val wrapper = mockk<SubmissionCommentsResponseWrapper>()
        coEvery { submissionCommentsManager.getSubmissionComments(7L, 1L, true) } returns wrapper

        val result = repository.getSubmissionComments(userId = 7L, assignmentId = 1L, forceNetwork = true)

        assertSame(wrapper, result)
        coVerify { submissionCommentsManager.getSubmissionComments(7L, 1L, true) }
    }

    @Test
    fun `getSingleSubmission returns the submission from the api`() = runTest {
        val submission = Submission(id = 42L)
        coEvery {
            submissionApi.getSingleSubmission(10L, 1L, 7L, any())
        } returns DataResult.Success(submission)

        val result = repository.getSingleSubmission(courseId = 10L, assignmentId = 1L, studentId = 7L)

        assertEquals(42L, result?.id)
    }

    @Test
    fun `getSingleSubmission returns null when the api fails`() = runTest {
        coEvery { submissionApi.getSingleSubmission(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getSingleSubmission(courseId = 10L, assignmentId = 1L, studentId = 7L)

        assertNull(result)
    }

    @Test
    fun `createSubmissionComment delegates to the manager`() = runTest {
        val data = mockk<CreateSubmissionCommentMutation.Data>()
        coEvery {
            submissionCommentsManager.createSubmissionComment(500L, "Hello", 2, false)
        } returns data

        val result = repository.createSubmissionComment(
            submissionId = 500L,
            comment = "Hello",
            attempt = 2,
            isGroupComment = false
        )

        assertSame(data, result)
        coVerify { submissionCommentsManager.createSubmissionComment(500L, "Hello", 2, false) }
    }

    @Test
    fun `markSubmissionCommentsRead delegates to the manager`() = runTest {
        val data = mockk<MarkSubmissionCommentsReadMutation.Data>()
        coEvery {
            submissionCommentsManager.markSubmissionCommentsRead(500L, listOf(1L, 2L))
        } returns data

        val result = repository.markSubmissionCommentsRead(submissionId = 500L, submissionCommentIds = listOf(1L, 2L))

        assertSame(data, result)
        coVerify { submissionCommentsManager.markSubmissionCommentsRead(500L, listOf(1L, 2L)) }
    }
}
