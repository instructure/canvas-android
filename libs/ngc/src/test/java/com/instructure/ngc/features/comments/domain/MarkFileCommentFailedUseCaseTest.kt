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

import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.PendingSubmissionCommentWithFileUploadInput
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val WORKER_ID = "33333333-3333-3333-3333-333333333333"

@OptIn(ExperimentalCoroutinesApi::class)
class MarkFileCommentFailedUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val useCase = MarkFileCommentFailedUseCase(pendingSubmissionCommentDao)

    @Test
    fun `marks the pending row for the worker as ERROR`() = runTest {
        val withInput = PendingSubmissionCommentWithFileUploadInput(
            pendingSubmissionCommentEntity = PendingSubmissionCommentEntity(
                id = 1L,
                pageId = "page-1",
                status = CommentSendStatus.SENDING.toString(),
                workerId = WORKER_ID
            ),
            fileUploadInput = null
        )
        coEvery { pendingSubmissionCommentDao.findByWorkerIdWithInputData(WORKER_ID) } returns withInput

        useCase(MarkFileCommentFailedUseCase.Params(workerId = WORKER_ID))

        coVerify {
            pendingSubmissionCommentDao.update(match { it.status == CommentSendStatus.ERROR.toString() })
        }
    }

    @Test
    fun `does nothing when no pending row matches the worker`() = runTest {
        coEvery { pendingSubmissionCommentDao.findByWorkerIdWithInputData(WORKER_ID) } returns null

        useCase(MarkFileCommentFailedUseCase.Params(workerId = WORKER_ID))

        coVerify(exactly = 0) { pendingSubmissionCommentDao.update(any()) }
    }
}
