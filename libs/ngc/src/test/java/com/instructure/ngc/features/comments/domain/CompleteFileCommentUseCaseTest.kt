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
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.AttachmentEntity
import com.instructure.pandautils.room.appdatabase.entities.AuthorEntity
import com.instructure.pandautils.room.appdatabase.entities.MediaCommentEntity
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.entities.SubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.SubmissionCommentWithAttachments
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

private const val WORKER_ID = "22222222-2222-2222-2222-222222222222"

@OptIn(ExperimentalCoroutinesApi::class)
class CompleteFileCommentUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val submissionCommentDao: SubmissionCommentDao = mockk(relaxed = true)
    private val authorDao: AuthorDao = mockk(relaxed = true)
    private val mediaCommentDao: MediaCommentDao = mockk(relaxed = true)
    private val attachmentDao: AttachmentDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val useCase = CompleteFileCommentUseCase(
        pendingSubmissionCommentDao,
        submissionCommentDao,
        authorDao,
        mediaCommentDao,
        attachmentDao,
        apiPrefs
    )

    @Test
    fun `deletes pending, maps the comment incl attachments and runs cleanup`() = runTest {
        val pending = PendingSubmissionCommentEntity(id = 1L, pageId = "page-1", workerId = WORKER_ID)
        coEvery { pendingSubmissionCommentDao.findByWorkerId(WORKER_ID) } returns pending

        val createdAt = Date()
        val author = AuthorEntity(id = 9L, displayName = "Teacher", avatarImageUrl = "avatar.png")
        val mediaComment = MediaCommentEntity(mediaId = "media-1")
        val attachment = AttachmentEntity(
            id = 55L,
            contentType = "image/png",
            filename = "a.png",
            displayName = "A.png",
            url = "http://example.com/a.png",
            thumbnailUrl = "http://example.com/thumb.png",
            createdAt = createdAt,
            size = 100L
        )
        val submissionComment = SubmissionCommentWithAttachments(
            submissionComment = SubmissionCommentEntity(id = 500L, comment = "Nice work", createdAt = createdAt),
            mediaComment = mediaComment,
            attachments = listOf(attachment),
            author = author
        )
        coEvery { submissionCommentDao.findById(500L) } returns submissionComment

        val result = useCase(CompleteFileCommentUseCase.Params(workerId = WORKER_ID, submissionCommentId = 500L))

        coVerify { pendingSubmissionCommentDao.delete(pending) }
        coVerify { authorDao.delete(author) }
        coVerify { mediaCommentDao.delete(mediaComment) }
        coVerify { attachmentDao.deleteAll(listOf(attachment)) }
        coVerify { submissionCommentDao.delete(submissionComment.submissionComment) }

        requireNotNull(result)
        assertEquals("500", result.id)
        assertEquals("Teacher", result.authorName)
        assertEquals("9", result.authorId)
        assertEquals("avatar.png", result.authorAvatarUrl)
        assertEquals("Nice work", result.content)
        assertEquals(createdAt, result.createdAt)
        assertTrue(result.isOwnComment)
        assertEquals(1, result.attachments.size)
        assertEquals("55", result.attachments[0].id)
        assertEquals("http://example.com/a.png", result.attachments[0].url)
        assertEquals("A.png", result.attachments[0].title)
        // Raw byte size is formatted human-readable, not shown as a plain byte count.
        assertEquals("100 B", result.attachments[0].size)
    }

    @Test
    fun `falls back to apiPrefs user when the cached comment has no author`() = runTest {
        val pending = PendingSubmissionCommentEntity(id = 1L, pageId = "page-1", workerId = WORKER_ID)
        coEvery { pendingSubmissionCommentDao.findByWorkerId(WORKER_ID) } returns pending
        every { apiPrefs.user } returns User(id = 3L, name = "Fallback", avatarUrl = "fallback.png")

        val submissionComment = SubmissionCommentWithAttachments(
            submissionComment = SubmissionCommentEntity(id = 501L, comment = "Hi"),
            mediaComment = null,
            attachments = null,
            author = null
        )
        coEvery { submissionCommentDao.findById(501L) } returns submissionComment

        val result = useCase(CompleteFileCommentUseCase.Params(workerId = WORKER_ID, submissionCommentId = 501L))

        requireNotNull(result)
        assertEquals("Fallback", result.authorName)
        assertEquals("3", result.authorId)
        assertEquals("fallback.png", result.authorAvatarUrl)
        assertTrue(result.attachments.isEmpty())
        coVerify(exactly = 0) { authorDao.delete(any()) }
        coVerify(exactly = 0) { mediaCommentDao.delete(any()) }
        coVerify(exactly = 0) { attachmentDao.deleteAll(any()) }
    }

    @Test
    fun `returns null and skips cleanup when the cached comment is missing`() = runTest {
        val pending = PendingSubmissionCommentEntity(id = 1L, pageId = "page-1", workerId = WORKER_ID)
        coEvery { pendingSubmissionCommentDao.findByWorkerId(WORKER_ID) } returns pending
        coEvery { submissionCommentDao.findById(500L) } returns null

        val result = useCase(CompleteFileCommentUseCase.Params(workerId = WORKER_ID, submissionCommentId = 500L))

        assertNull(result)
        coVerify { pendingSubmissionCommentDao.delete(pending) }
        coVerify(exactly = 0) { authorDao.delete(any()) }
        coVerify(exactly = 0) { submissionCommentDao.delete(any()) }
    }

    @Test
    fun `returns null without deleting when no pending row matches the worker`() = runTest {
        coEvery { pendingSubmissionCommentDao.findByWorkerId(WORKER_ID) } returns null

        val result = useCase(CompleteFileCommentUseCase.Params(workerId = WORKER_ID, submissionCommentId = 500L))

        assertNull(result)
        coVerify(exactly = 0) { pendingSubmissionCommentDao.delete(any()) }
        coVerify(exactly = 0) { submissionCommentDao.findById(any()) }
    }
}
