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
package com.instructure.ngc.features.comments.domain

import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.pandautils.data.repository.submissioncomments.SubmissionCommentsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CreateSubmissionCommentUseCaseTest {

    private val repository: SubmissionCommentsRepository = mockk(relaxed = true)
    private val useCase = CreateSubmissionCommentUseCase(repository)

    @Test
    fun `maps created comment id and createdAt from the mutation result`() = runTest {
        val date = Date(1_700_000_000_000L)
        coEvery { repository.createSubmissionComment(any(), any(), any(), any()) } returns
            mockk<CreateSubmissionCommentMutation.Data> {
                every { createSubmissionComment?.submissionComment } returns mockk {
                    every { _id } returns "42"
                    every { createdAt } returns date
                }
            }

        val result = useCase(
            CreateSubmissionCommentUseCase.Params(submissionId = 500L, comment = "Nice work", attempt = 2)
        )

        assertEquals("42", result.id)
        assertEquals(date, result.createdAt)
        coVerify { repository.createSubmissionComment(500L, "Nice work", 2, any()) }
    }
}
