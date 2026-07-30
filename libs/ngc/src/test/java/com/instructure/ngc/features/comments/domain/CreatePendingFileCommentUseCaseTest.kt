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
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.PendingSubmissionCommentWithFileUploadInput
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val WORKER_ID = "11111111-1111-1111-1111-111111111111"

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePendingFileCommentUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val fileUploadInputDao: FileUploadInputDao = mockk(relaxed = true)
    private val useCase = CreatePendingFileCommentUseCase(pendingSubmissionCommentDao, fileUploadInputDao)

    @Test
    fun `inserts input and pending comment when neither exists`() = runTest {
        coEvery { fileUploadInputDao.findByWorkerId(WORKER_ID) } returns null
        coEvery { pendingSubmissionCommentDao.findByPageId("page-1") } returns null
        val inputSlot = slot<FileUploadInputEntity>()
        val commentSlot = slot<PendingSubmissionCommentEntity>()
        coEvery { fileUploadInputDao.insert(capture(inputSlot)) } returns Unit
        coEvery { pendingSubmissionCommentDao.insert(capture(commentSlot)) } returns 1L

        useCase(
            CreatePendingFileCommentUseCase.Params(
                workerId = WORKER_ID,
                courseId = 10L,
                assignmentId = 20L,
                studentId = 30L,
                filePaths = listOf("/tmp/a.png"),
                pageId = "page-1",
                attemptId = 5L
            )
        )

        assertEquals(WORKER_ID, inputSlot.captured.workerId)
        assertEquals(10L, inputSlot.captured.courseId)
        assertEquals(20L, inputSlot.captured.assignmentId)
        assertEquals(30L, inputSlot.captured.userId)
        assertEquals(listOf("/tmp/a.png"), inputSlot.captured.filePaths)
        assertEquals(5L, inputSlot.captured.attemptId)

        assertEquals(WORKER_ID, commentSlot.captured.workerId)
        assertEquals(CommentSendStatus.SENDING.toString(), commentSlot.captured.status)
        assertEquals("page-1", commentSlot.captured.pageId)
        assertEquals(5L, commentSlot.captured.attemptId)
    }

    @Test
    fun `does not insert duplicates when input and pending already exist for the worker`() = runTest {
        val existingInput = FileUploadInputEntity(
            workerId = WORKER_ID,
            courseId = 10L,
            assignmentId = 20L,
            userId = 30L,
            filePaths = listOf("/tmp/a.png"),
            action = "action"
        )
        coEvery { fileUploadInputDao.findByWorkerId(WORKER_ID) } returns existingInput
        val existingPending = PendingSubmissionCommentWithFileUploadInput(
            pendingSubmissionCommentEntity = PendingSubmissionCommentEntity(
                id = 1L,
                pageId = "page-1",
                status = CommentSendStatus.SENDING.toString(),
                workerId = WORKER_ID
            ),
            fileUploadInput = existingInput
        )
        coEvery { pendingSubmissionCommentDao.findByPageId("page-1") } returns listOf(existingPending)

        useCase(
            CreatePendingFileCommentUseCase.Params(
                workerId = WORKER_ID,
                courseId = 10L,
                assignmentId = 20L,
                studentId = 30L,
                filePaths = listOf("/tmp/a.png"),
                pageId = "page-1",
                attemptId = null
            )
        )

        coVerify(exactly = 0) { fileUploadInputDao.insert(any()) }
        coVerify(exactly = 0) { pendingSubmissionCommentDao.insert(any()) }
    }
}
