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

import android.webkit.URLUtil
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.data.repository.submissioncomments.SubmissionCommentsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class GetSubmissionCommentsUseCaseTest {

    private val repository: SubmissionCommentsRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val useCase = GetSubmissionCommentsUseCase(repository, apiPrefs)

    @Before
    fun setup() {
        // Constructing an Attachment eagerly evaluates its `isLocalFile` (URLUtil.isNetworkUrl),
        // which is an un-mocked Android framework call under plain JVM unit tests.
        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun author(id: String) = SubmissionCommentsQuery.Author(
        _id = id,
        name = "Author $id",
        email = null,
        avatarUrl = "avatar$id",
        pronouns = null,
    )

    private fun node(
        id: String,
        authorId: String?,
        authorVisibleName: String? = null,
        attempt: Int = 1,
        read: Boolean = true,
        attachments: List<SubmissionCommentsQuery.Attachment> = emptyList(),
    ) = SubmissionCommentsQuery.Node1(
        _id = id,
        author = authorId?.let { author(it) },
        mediaObject = null,
        authorVisibleName = authorVisibleName,
        comment = "Comment $id",
        mediaCommentId = null,
        createdAt = Date(),
        canReply = true,
        draft = false,
        attempt = attempt,
        read = read,
        attachments = attachments,
    )

    private fun attachment(id: String, url: String?) = SubmissionCommentsQuery.Attachment(
        _id = id,
        id = "gql-$id",
        contentType = "application/pdf",
        createdAt = Date(),
        displayName = "file-$id.pdf",
        title = "file-$id.pdf",
        size = "1024",
        thumbnailUrl = null,
        url = url,
    )

    private fun wrapper(comments: List<SubmissionCommentsQuery.Node1>, submissionId: String? = "500") =
        mockk<SubmissionCommentsResponseWrapper> {
            every { data.submission } returns mockk { every { _id } returns submissionId!! }
            every { this@mockk.comments } returns comments
        }

    @Test
    fun `own comment is flagged as own and never anonymous or unread`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns
            wrapper(listOf(node(id = "1", authorId = "7", read = false)))

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = 1L))

        val comment = result.comments.single()
        assertTrue(comment.isOwnComment)
        assertFalse(comment.isAnonymous)
        assertFalse(comment.isUnread)
        assertEquals(500L, result.submissionId)
    }

    @Test
    fun `other user unread comment is flagged unread and not own`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns
            wrapper(listOf(node(id = "2", authorId = "99", read = false)))

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = 1L))

        val comment = result.comments.single()
        assertFalse(comment.isOwnComment)
        assertFalse(comment.isAnonymous)
        assertTrue(comment.isUnread)
        assertEquals("Author 99", comment.authorName)
    }

    @Test
    fun `hidden author with visible name maps to anonymous with no avatar`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns
            wrapper(listOf(node(id = "3", authorId = null, authorVisibleName = "Student 11234")))

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = 1L))

        val comment = result.comments.single()
        assertTrue(comment.isAnonymous)
        assertFalse(comment.isOwnComment)
        assertEquals("Student 11234", comment.authorName)
        assertEquals(null, comment.authorAvatarUrl)
    }

    @Test
    fun `comments are filtered to the selected attempt`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns wrapper(
            listOf(
                node(id = "a", authorId = "99", attempt = 1),
                node(id = "b", authorId = "99", attempt = 2),
            )
        )

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = 2L))

        assertEquals(1, result.comments.size)
        assertEquals("b", result.comments.single().id)
    }

    @Test
    fun `attempt zero comments are shown on the first attempt`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns
            wrapper(listOf(node(id = "legacy", authorId = "99", attempt = 0)))

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = 1L))

        assertEquals(1, result.comments.size)
    }

    @Test
    fun `attachment url is replaced with the verifier url from the rest submission`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns
            wrapper(listOf(node(id = "1", authorId = "99", attachments = listOf(attachment(id = "42", url = "https://canvas/files/42/unverified")))))
        coEvery { repository.getSingleSubmission(any(), any(), any()) } returns Submission(
            submissionComments = listOf(
                SubmissionComment(attachments = arrayListOf(Attachment(id = 42L, url = "https://canvas/files/42?verifier=abc")))
            )
        )

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = 1L))

        assertEquals("https://canvas/files/42?verifier=abc", result.comments.single().attachments.single().url)
    }

    @Test
    fun `attachment falls back to the graphql url when no verifier url is available`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns
            wrapper(listOf(node(id = "1", authorId = "99", attachments = listOf(attachment(id = "42", url = "https://canvas/files/42/unverified")))))
        coEvery { repository.getSingleSubmission(any(), any(), any()) } returns null

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = 1L))

        assertEquals("https://canvas/files/42/unverified", result.comments.single().attachments.single().url)
    }

    @Test
    fun `null selected attempt shows all comments`() = runTest {
        every { apiPrefs.user } returns User(id = 7L)
        coEvery { repository.getSubmissionComments(any(), any(), any()) } returns wrapper(
            listOf(
                node(id = "a", authorId = "99", attempt = 1),
                node(id = "b", authorId = "99", attempt = 2),
            )
        )

        val result = useCase(GetSubmissionCommentsUseCase.Params(courseId = 10L, studentId = 7L, assignmentId = 1L, selectedAttemptId = null))

        assertEquals(2, result.comments.size)
    }
}
