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
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePendingTextCommentUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val useCase = CreatePendingTextCommentUseCase(pendingSubmissionCommentDao)

    private fun withInput(id: Long, status: CommentSendStatus, comment: String) =
        PendingSubmissionCommentWithFileUploadInput(
            pendingSubmissionCommentEntity = PendingSubmissionCommentEntity(
                id = id,
                pageId = "page-1",
                comment = comment,
                status = status.toString()
            ),
            fileUploadInput = null
        )

    @Test
    fun `deletes existing drafts and inserts a pending comment in sending state`() = runTest {
        val draft = withInput(1L, CommentSendStatus.DRAFT, "old draft")
        coEvery { pendingSubmissionCommentDao.findByPageId("page-1") } returns listOf(draft)
        val slot = slot<PendingSubmissionCommentEntity>()
        coEvery { pendingSubmissionCommentDao.insert(capture(slot)) } returns 99L

        val result = useCase(
            CreatePendingTextCommentUseCase.Params(pageId = "page-1", comment = "New comment", attemptId = 5L)
        )

        coVerify { pendingSubmissionCommentDao.deleteAll(listOf(draft.pendingSubmissionCommentEntity)) }
        assertEquals("New comment", slot.captured.comment)
        assertEquals(CommentSendStatus.SENDING.toString(), slot.captured.status)
        assertEquals(5L, slot.captured.attemptId)
        assertEquals(slot.captured.id, result)
    }

    @Test
    fun `does not delete anything when there is no existing draft`() = runTest {
        coEvery { pendingSubmissionCommentDao.findByPageId("page-1") } returns null

        useCase(CreatePendingTextCommentUseCase.Params(pageId = "page-1", comment = "New comment", attemptId = null))

        coVerify { pendingSubmissionCommentDao.deleteAll(emptyList()) }
        coVerify(exactly = 1) { pendingSubmissionCommentDao.insert(any()) }
    }
}
