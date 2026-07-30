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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class SendTextCommentUseCaseTest {

    private val createSubmissionCommentUseCase: CreateSubmissionCommentUseCase = mockk(relaxed = true)
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val useCase = SendTextCommentUseCase(createSubmissionCommentUseCase, pendingSubmissionCommentDao, apiPrefs)

    @Test
    fun `on success deletes the pending row and returns the sent comment`() = runTest {
        every { apiPrefs.user } returns User(id = 7L, name = "Teacher", avatarUrl = "avatar.png")
        val createdAt = Date()
        coEvery { createSubmissionCommentUseCase(any()) } returns CreatedSubmissionComment(
            id = "42",
            createdAt = createdAt
        )

        val result = useCase(
            SendTextCommentUseCase.Params(pendingId = 10L, submissionId = 500L, comment = "Nice", attemptId = 2L)
        )

        coVerify {
            createSubmissionCommentUseCase(
                CreateSubmissionCommentUseCase.Params(submissionId = 500L, comment = "Nice", attempt = 2)
            )
        }
        coVerify { pendingSubmissionCommentDao.deleteById(10L) }
        assertEquals("42", result.id)
        assertEquals("Nice", result.content)
        assertEquals(createdAt, result.createdAt)
        assertEquals("Teacher", result.authorName)
        assertFalse(result.isPending)
    }

    @Test(expected = IllegalStateException::class)
    fun `throws when submission id is null`() = runTest {
        useCase(SendTextCommentUseCase.Params(pendingId = 10L, submissionId = null, comment = "Nice", attemptId = null))
    }

    @Test
    fun `on failure marks the pending row as error and rethrows`() = runTest {
        coEvery { createSubmissionCommentUseCase(any()) } throws RuntimeException("network error")
        val entity = PendingSubmissionCommentEntity(
            id = 10L,
            pageId = "page-1",
            comment = "Nice",
            status = CommentSendStatus.SENDING.toString()
        )
        coEvery { pendingSubmissionCommentDao.findById(10L) } returns entity

        try {
            useCase(
                SendTextCommentUseCase.Params(pendingId = 10L, submissionId = 500L, comment = "Nice", attemptId = null)
            )
            fail("Expected exception to be rethrown")
        } catch (e: RuntimeException) {
            assertEquals("network error", e.message)
        }

        coVerify { pendingSubmissionCommentDao.update(match { it.status == CommentSendStatus.ERROR.toString() }) }
    }
}
