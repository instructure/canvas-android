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
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePendingMediaCommentUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val useCase = CreatePendingMediaCommentUseCase(pendingSubmissionCommentDao)

    @Test
    fun `inserts a SENDING pending row with the file path and returns its id`() = runTest {
        val entitySlot = slot<PendingSubmissionCommentEntity>()
        coEvery { pendingSubmissionCommentDao.insert(capture(entitySlot)) } returns 7L

        val result = useCase(
            CreatePendingMediaCommentUseCase.Params(
                pageId = "page-1",
                filePath = "/tmp/recording.mp4",
                attemptId = 3L
            )
        )

        assertEquals(7L, result)
        assertEquals("page-1", entitySlot.captured.pageId)
        assertEquals("/tmp/recording.mp4", entitySlot.captured.filePath)
        assertEquals(CommentSendStatus.SENDING.toString(), entitySlot.captured.status)
        assertEquals(3L, entitySlot.captured.attemptId)
    }
}
