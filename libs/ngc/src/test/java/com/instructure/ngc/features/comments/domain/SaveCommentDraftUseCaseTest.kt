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
class SaveCommentDraftUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val useCase = SaveCommentDraftUseCase(pendingSubmissionCommentDao)

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
    fun `deletes existing drafts and inserts a new draft when text is non-blank`() = runTest {
        val draft = withInput(1L, CommentSendStatus.DRAFT, "old draft")
        val pending = withInput(2L, CommentSendStatus.SENDING, "pending")
        coEvery { pendingSubmissionCommentDao.findByPageId("page-1") } returns listOf(draft, pending)
        val slot = slot<PendingSubmissionCommentEntity>()
        coEvery { pendingSubmissionCommentDao.insert(capture(slot)) } returns 1L

        useCase(SaveCommentDraftUseCase.Params(pageId = "page-1", text = "New draft", attemptId = 3L))

        coVerify { pendingSubmissionCommentDao.deleteAll(listOf(draft.pendingSubmissionCommentEntity)) }
        coVerify(exactly = 1) { pendingSubmissionCommentDao.insert(any()) }
        assertEquals("New draft", slot.captured.comment)
        assertEquals(CommentSendStatus.DRAFT.toString(), slot.captured.status)
        assertEquals(3L, slot.captured.attemptId)
    }

    @Test
    fun `does not insert a draft when text is blank`() = runTest {
        coEvery { pendingSubmissionCommentDao.findByPageId("page-1") } returns emptyList()

        useCase(SaveCommentDraftUseCase.Params(pageId = "page-1", text = "   ", attemptId = null))

        coVerify(exactly = 0) { pendingSubmissionCommentDao.insert(any()) }
    }
}
