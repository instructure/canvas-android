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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarkPendingCommentFailedUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val useCase = MarkPendingCommentFailedUseCase(pendingSubmissionCommentDao)

    @Test
    fun `marks the pending row ERROR by id`() = runTest {
        val pending = PendingSubmissionCommentEntity(id = 5L, pageId = "page-1", status = CommentSendStatus.SENDING.toString())
        coEvery { pendingSubmissionCommentDao.findById(5L) } returns pending

        useCase(MarkPendingCommentFailedUseCase.Params(commentId = 5L))

        assertEquals(CommentSendStatus.ERROR.toString(), pending.status)
        coVerify { pendingSubmissionCommentDao.update(pending) }
    }

    @Test
    fun `does nothing when no pending row matches the id`() = runTest {
        coEvery { pendingSubmissionCommentDao.findById(6L) } returns null

        useCase(MarkPendingCommentFailedUseCase.Params(commentId = 6L))

        coVerify(exactly = 0) { pendingSubmissionCommentDao.update(any()) }
    }
}
