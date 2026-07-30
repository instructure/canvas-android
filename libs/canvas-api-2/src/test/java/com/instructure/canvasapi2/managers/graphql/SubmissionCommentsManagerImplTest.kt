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
package com.instructure.canvasapi2.managers.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.MarkSubmissionCommentsReadMutation
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.enqueueMutation
import com.instructure.canvasapi2.enqueueQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

private const val QL_CLIENT_CONFIG_CLASS = "com.instructure.canvasapi2.QLClientConfigKt"

class SubmissionCommentsManagerImplTest {

    private val apolloClient: ApolloClient = mockk()
    private lateinit var manager: SubmissionCommentsManagerImpl

    @Before
    fun setup() {
        mockkStatic(QL_CLIENT_CONFIG_CLASS)
        manager = SubmissionCommentsManagerImpl(apolloClient)
    }

    @After
    fun tearDown() {
        unmockkStatic(QL_CLIENT_CONFIG_CLASS)
    }

    @Test
    fun `getSubmissionComments returns every comment of a single page`() = runTest {
        val response = queryResponse(
            data(commentNodes = listOf(commentNode("1"), commentNode("2")))
        )
        coEvery { apolloClient.enqueueQuery(any<SubmissionCommentsQuery>(), any(), any()) } returns response

        val result = manager.getSubmissionComments(userId = 7L, assignmentId = 1L, forceNetwork = true)

        assertEquals(2, result.comments.size)
        assertEquals(listOf("1", "2"), result.comments.map { it._id })
        coVerify(exactly = 1) { apolloClient.enqueueQuery(any<SubmissionCommentsQuery>(), any(), any()) }
    }

    @Test
    fun `getSubmissionComments paginates comments within a history`() = runTest {
        val firstPage = queryResponse(
            data(
                commentNodes = listOf(commentNode("1")),
                commentsHasNext = true,
                commentsEndCursor = "cursor-1"
            )
        )
        val secondPage = queryResponse(
            data(commentNodes = listOf(commentNode("2")))
        )
        coEvery {
            apolloClient.enqueueQuery(any<SubmissionCommentsQuery>(), any(), any())
        } returnsMany listOf(firstPage, secondPage)

        val result = manager.getSubmissionComments(userId = 7L, assignmentId = 1L, forceNetwork = true)

        assertEquals(listOf("1", "2"), result.comments.map { it._id })
        coVerify(exactly = 2) { apolloClient.enqueueQuery(any<SubmissionCommentsQuery>(), any(), any()) }
    }

    @Test
    fun `getSubmissionComments throws when no data is returned`() = runTest {
        coEvery {
            apolloClient.enqueueQuery(any<SubmissionCommentsQuery>(), any(), any())
        } returns queryResponse(null)

        var thrown = false
        try {
            manager.getSubmissionComments(userId = 7L, assignmentId = 1L, forceNetwork = true)
        } catch (e: Exception) {
            thrown = true
        }
        assertTrue(thrown)
    }

    @Test
    fun `createSubmissionComment sends the given variables and returns the data`() = runTest {
        val mutationSlot = slot<CreateSubmissionCommentMutation>()
        val data = CreateSubmissionCommentMutation.Data(createSubmissionComment = null)
        coEvery {
            apolloClient.enqueueMutation(capture(mutationSlot), any())
        } returns mutationResponse(data)

        val result = manager.createSubmissionComment(
            submissionId = 500L,
            comment = "Hello",
            attempt = 2,
            isGroupComment = false
        )

        assertEquals(data, result)
        assertEquals("500", mutationSlot.captured.submissionId)
        assertEquals("Hello", mutationSlot.captured.comment)
        assertEquals(Optional.Present(2), mutationSlot.captured.attempt)
        assertEquals(Optional.Present(false), mutationSlot.captured.groupComment)
    }

    @Test
    fun `markSubmissionCommentsRead sends the comment ids as strings and returns the data`() = runTest {
        val mutationSlot = slot<MarkSubmissionCommentsReadMutation>()
        val data = MarkSubmissionCommentsReadMutation.Data(markSubmissionCommentsRead = null)
        coEvery {
            apolloClient.enqueueMutation(capture(mutationSlot), any())
        } returns mutationResponse(data)

        val result = manager.markSubmissionCommentsRead(submissionId = 500L, submissionCommentIds = listOf(1L, 2L))

        assertEquals(data, result)
        assertEquals("500", mutationSlot.captured.submissionId)
        assertEquals(listOf("1", "2"), mutationSlot.captured.submissionCommentIds)
    }

    private fun commentNode(id: String) = SubmissionCommentsQuery.Node1(
        _id = id,
        author = null,
        mediaObject = null,
        authorVisibleName = null,
        comment = "Comment $id",
        mediaCommentId = null,
        createdAt = Date(),
        canReply = true,
        draft = false,
        attempt = 1,
        read = true,
        attachments = emptyList(),
    )

    private fun data(
        commentNodes: List<SubmissionCommentsQuery.Node1>,
        commentsHasNext: Boolean = false,
        commentsEndCursor: String? = null,
        historyHasNext: Boolean = false,
        historyEndCursor: String? = null,
    ) = SubmissionCommentsQuery.Data(
        submission = SubmissionCommentsQuery.Submission(
            _id = "500",
            userId = "7",
            assignment = SubmissionCommentsQuery.Assignment(anonymousGrading = false),
            submissionHistoriesConnection = SubmissionCommentsQuery.SubmissionHistoriesConnection(
                edges = listOf(
                    SubmissionCommentsQuery.Edge(
                        node = SubmissionCommentsQuery.Node(
                            attempt = 1,
                            commentsConnection = SubmissionCommentsQuery.CommentsConnection(
                                edges = commentNodes.map { SubmissionCommentsQuery.Edge1(node = it) },
                                pageInfo = SubmissionCommentsQuery.PageInfo(
                                    hasNextPage = commentsHasNext,
                                    endCursor = commentsEndCursor
                                )
                            )
                        )
                    )
                ),
                pageInfo = SubmissionCommentsQuery.PageInfo1(
                    hasNextPage = historyHasNext,
                    endCursor = historyEndCursor
                )
            )
        )
    )

    private fun queryResponse(data: SubmissionCommentsQuery.Data?): ApolloResponse<SubmissionCommentsQuery.Data> =
        ApolloResponse.Builder(
            operation = SubmissionCommentsQuery(
                userId = "7",
                assignmentId = "1",
                historyCursor = Optional.absent(),
                commentCursor = Optional.absent()
            ),
            requestUuid = UUID.randomUUID()
        ).data(data).build()

    private fun <T : com.apollographql.apollo.api.Mutation.Data> mutationResponse(
        data: T
    ): ApolloResponse<T> = ApolloResponse.Builder(
        operation = mockk<com.apollographql.apollo.api.Mutation<T>>(relaxed = true),
        requestUuid = UUID.randomUUID()
    ).data(data).build()
}
