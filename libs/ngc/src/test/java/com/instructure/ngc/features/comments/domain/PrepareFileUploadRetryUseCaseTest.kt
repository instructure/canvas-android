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

import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val WORKER_ID = "33333333-3333-3333-3333-333333333334"

@OptIn(ExperimentalCoroutinesApi::class)
class PrepareFileUploadRetryUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val fileUploadInputDao: FileUploadInputDao = mockk(relaxed = true)
    private val useCase = PrepareFileUploadRetryUseCase(pendingSubmissionCommentDao, fileUploadInputDao)

    @Test
    fun `deletes the stale file input and the pending row`() = runTest {
        val existingInput = FileUploadInputEntity(
            workerId = WORKER_ID,
            filePaths = listOf("/tmp/a.png"),
            action = "action"
        )
        coEvery { fileUploadInputDao.findByWorkerId(WORKER_ID) } returns existingInput

        useCase(PrepareFileUploadRetryUseCase.Params(commentId = 1L, workerId = WORKER_ID))

        coVerify { fileUploadInputDao.delete(existingInput) }
        coVerify { pendingSubmissionCommentDao.deleteById(1L) }
    }

    @Test
    fun `deletes only the pending row when no file input exists for the worker`() = runTest {
        coEvery { fileUploadInputDao.findByWorkerId(WORKER_ID) } returns null

        useCase(PrepareFileUploadRetryUseCase.Params(commentId = 1L, workerId = WORKER_ID))

        coVerify(exactly = 0) { fileUploadInputDao.delete(any()) }
        coVerify { pendingSubmissionCommentDao.deleteById(1L) }
    }
}
