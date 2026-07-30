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

import com.instructure.pandautils.data.repository.submissioncomments.SubmissionCommentsRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarkSubmissionCommentsReadUseCaseTest {

    private val repository: SubmissionCommentsRepository = mockk(relaxed = true)
    private val useCase = MarkSubmissionCommentsReadUseCase(repository)

    @Test
    fun `marks the given comments read`() = runTest {
        useCase(MarkSubmissionCommentsReadUseCase.Params(submissionId = 500L, commentIds = listOf(1L, 2L)))

        coVerify { repository.markSubmissionCommentsRead(500L, listOf(1L, 2L)) }
    }

    @Test
    fun `does nothing when there are no unread comments`() = runTest {
        useCase(MarkSubmissionCommentsReadUseCase.Params(submissionId = 500L, commentIds = emptyList()))

        coVerify(exactly = 0) { repository.markSubmissionCommentsRead(any(), any()) }
    }
}
