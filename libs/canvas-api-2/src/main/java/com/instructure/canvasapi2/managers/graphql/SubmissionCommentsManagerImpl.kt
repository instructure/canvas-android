/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper

class SubmissionCommentsManagerImpl : SubmissionCommentsManager {

    override suspend fun getSubmissionComments(userId: Long, assignmentId: Long): SubmissionCommentsResponseWrapper {
        val allComments = mutableListOf<SubmissionCommentsQuery.Node1>()
        var historyCursor: String? = null
        var hasMoreHistories = true
        var initialData: SubmissionCommentsQuery.Data? = null

        while (hasMoreHistories) {
            val historyCursorOpt = if (historyCursor != null) Optional.present(historyCursor) else Optional.absent()

            val query = SubmissionCommentsQuery(
                userId = userId.toString(),
                assignmentId = assignmentId.toString(),
                historyCursor = historyCursorOpt,
                commentCursor = Optional.absent()
            )

            val historyData = QLClientConfig.enqueueQuery(query).data
            if (initialData == null) initialData = historyData

            val historyEdges = historyData
                ?.submission
                ?.submissionHistoriesConnection
                ?.edges
                .orEmpty()

            for (historyEdge in historyEdges) {
                val historyNode = historyEdge?.node ?: continue
                val attempt = historyNode.attempt

                val commentEdges = historyNode.commentsConnection?.edges.orEmpty().mapNotNull { it?.node }
                allComments.addAll(commentEdges)

                var commentPageInfo = historyNode.commentsConnection?.pageInfo
                var commentCursor = commentPageInfo?.endCursor
                var hasMoreComments = commentPageInfo?.hasNextPage == true

                while (hasMoreComments) {
                    val commentQuery = SubmissionCommentsQuery(
                        userId = userId.toString(),
                        assignmentId = assignmentId.toString(),
                        historyCursor = historyCursorOpt,
                        commentCursor = Optional.present(commentCursor)
                    )

                    val commentData = QLClientConfig.enqueueQuery(commentQuery).data

                    val matchedHistory = commentData
                        ?.submission
                        ?.submissionHistoriesConnection
                        ?.edges
                        ?.mapNotNull { it?.node }
                        ?.find { it.attempt == attempt }

                    val newComments = matchedHistory
                        ?.commentsConnection
                        ?.edges
                        ?.mapNotNull { it?.node }
                        .orEmpty()

                    allComments.addAll(newComments)

                    commentPageInfo = matchedHistory?.commentsConnection?.pageInfo
                    hasMoreComments = commentPageInfo?.hasNextPage == true
                    commentCursor = commentPageInfo?.endCursor
                }
            }

            val pageInfo = historyData
                ?.submission
                ?.submissionHistoriesConnection
                ?.pageInfo

            hasMoreHistories = pageInfo?.hasNextPage == true
            historyCursor = pageInfo?.endCursor
        }

        if (initialData == null) {
            throw Exception("No data returned from SubmissionCommentsQuery")
        }

        return SubmissionCommentsResponseWrapper(initialData, allComments)
    }

    override suspend fun createSubmissionComment(
        submissionId: Long,
        comment: String,
        attempt: Int?,
        isGroupComment: Boolean
    ): CreateSubmissionCommentMutation.Data {
        val mutation = CreateSubmissionCommentMutation(
            submissionId.toString(),
            comment,
            Optional.Present(attempt),
            Optional.Present(isGroupComment)
        )
        val result = QLClientConfig.enqueueMutation(mutation)
        return result.dataAssertNoErrors
    }
}
