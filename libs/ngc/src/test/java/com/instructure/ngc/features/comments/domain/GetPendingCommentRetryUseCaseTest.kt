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
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val WORKER_ID = "22222222-2222-2222-2222-222222222222"

@OptIn(ExperimentalCoroutinesApi::class)
class GetPendingCommentRetryUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val fileUploadInputDao: FileUploadInputDao = mockk(relaxed = true)
    private val useCase = GetPendingCommentRetryUseCase(pendingSubmissionCommentDao, fileUploadInputDao)

    @Test
    fun `returns null when no pending row matches the id`() = runTest {
        coEvery { pendingSubmissionCommentDao.findById(1L) } returns null

        val result = useCase(GetPendingCommentRetryUseCase.Params(commentId = 1L))

        assertNull(result)
    }

    @Test
    fun `returns text comment data without looking up file input`() = runTest {
        coEvery { pendingSubmissionCommentDao.findById(1L) } returns PendingSubmissionCommentEntity(
            id = 1L,
            pageId = "page-1",
            comment = "Hello"
        )

        val result = useCase(GetPendingCommentRetryUseCase.Params(commentId = 1L))

        assertEquals("Hello", result?.comment)
        assertNull(result?.workerId)
        assertNull(result?.filePaths)
        coVerify(exactly = 0) { fileUploadInputDao.findByWorkerId(any()) }
    }

    @Test
    fun `resolves file paths for a valid worker id`() = runTest {
        coEvery { pendingSubmissionCommentDao.findById(2L) } returns PendingSubmissionCommentEntity(
            id = 2L,
            pageId = "page-1",
            workerId = WORKER_ID
        )
        coEvery { fileUploadInputDao.findByWorkerId(WORKER_ID) } returns FileUploadInputEntity(
            workerId = WORKER_ID,
            filePaths = listOf("/tmp/a.png"),
            action = "action"
        )

        val result = useCase(GetPendingCommentRetryUseCase.Params(commentId = 2L))

        assertEquals(WORKER_ID, result?.workerId)
        assertEquals(listOf("/tmp/a.png"), result?.filePaths)
    }

    @Test
    fun `treats the literal string null worker id as absent`() = runTest {
        coEvery { pendingSubmissionCommentDao.findById(3L) } returns PendingSubmissionCommentEntity(
            id = 3L,
            pageId = "page-1",
            workerId = "null",
            filePath = "/tmp/media.mp4"
        )

        val result = useCase(GetPendingCommentRetryUseCase.Params(commentId = 3L))

        assertNull(result?.workerId)
        assertEquals("/tmp/media.mp4", result?.filePath)
        coVerify(exactly = 0) { fileUploadInputDao.findByWorkerId(any()) }
    }
}
