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

import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.PendingSubmissionCommentWithFileUploadInput
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObservePendingSubmissionCommentsUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val useCase = ObservePendingSubmissionCommentsUseCase(pendingSubmissionCommentDao, apiPrefs)

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
    fun `emits draft text and maps pending comments excluding drafts`() = runTest {
        every { apiPrefs.user } returns User(id = 1L, name = "Teacher")
        every { pendingSubmissionCommentDao.findByPageIdFlow("page-1") } returns flowOf(
            listOf(
                withInput(1L, CommentSendStatus.DRAFT, "Draft text"),
                withInput(2L, CommentSendStatus.SENDING, "Sending text"),
                withInput(3L, CommentSendStatus.ERROR, "Failed text"),
            )
        )

        val result = useCase(ObservePendingSubmissionCommentsUseCase.Params("page-1")).first()

        assertEquals("Draft text", result.draftText)
        assertEquals(2, result.pendingComments.size)

        val sending = result.pendingComments.first { it.id == "2" }
        assertTrue(sending.isPending)
        assertFalse(sending.isFailed)
        assertEquals("Sending text", sending.content)

        val failed = result.pendingComments.first { it.id == "3" }
        assertTrue(failed.isFailed)
        assertFalse(failed.isPending)
    }

    @Test
    fun `emits null draft text when there is no draft`() = runTest {
        every { apiPrefs.user } returns User(id = 1L, name = "Teacher")
        every { pendingSubmissionCommentDao.findByPageIdFlow("page-1") } returns flowOf(
            listOf(withInput(2L, CommentSendStatus.SENDING, "Sending text"))
        )

        val result = useCase(ObservePendingSubmissionCommentsUseCase.Params("page-1")).first()

        assertNull(result.draftText)
        assertEquals(1, result.pendingComments.size)
    }

    @Test
    fun `emits empty state when there are no pending rows`() = runTest {
        every { pendingSubmissionCommentDao.findByPageIdFlow("page-1") } returns flowOf(null)

        val result = useCase(ObservePendingSubmissionCommentsUseCase.Params("page-1")).first()

        assertNull(result.draftText)
        assertTrue(result.pendingComments.isEmpty())
    }
}
