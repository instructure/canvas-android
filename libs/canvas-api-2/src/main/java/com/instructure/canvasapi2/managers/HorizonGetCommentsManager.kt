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
package com.instructure.canvasapi2.managers

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.HorizonGetSubmissionCommentsQuery
import com.instructure.canvasapi2.QLClientConfig
import java.util.Date

private const val SUBMISSION_COMMENTS_PAGE_SIZE = 5

class HorizonGetCommentsManager {

    suspend fun getComments(
        assignmentId: Long,
        userId: Long,
        attempt: Int,
        nextPage: Boolean = false,
        forceNetwork: Boolean,
        endCursor: String? = null,
        startCursor: String? = null
    ): CommentsData {
        val first = if (nextPage) SUBMISSION_COMMENTS_PAGE_SIZE else null
        val last = if (nextPage) null else SUBMISSION_COMMENTS_PAGE_SIZE

        val query = HorizonGetSubmissionCommentsQuery(
            assignmentId.toString(),
            userId.toString(),
            startCursor = Optional.presentIfNotNull(startCursor),
            endCursor = Optional.presentIfNotNull(endCursor),
            first = Optional.presentIfNotNull(first),
            last = Optional.presentIfNotNull(last),
            attempt = Optional.present(attempt)
        )
        val result = QLClientConfig.enqueueQuery(query, forceNetwork)

        val newEndCursor = result.data?.submission?.onSubmission?.commentsConnection?.pageInfo?.endCursor
        val hasNextPage = result.data?.submission?.onSubmission?.commentsConnection?.pageInfo?.hasNextPage ?: false
        val newStartCursor = result.data?.submission?.onSubmission?.commentsConnection?.pageInfo?.startCursor
        val hasPreviousPage = result.data?.submission?.onSubmission?.commentsConnection?.pageInfo?.hasPreviousPage ?: false

        val resultData = result.dataAssertNoErrors
        val comments = resultData.submission?.onSubmission?.commentsConnection?.edges?.mapNotNull { edge ->
            edge?.node?.let { commentNode ->
                Comment(
                    authorId = commentNode.author?._id?.toLong() ?: -1,
                    authorName = commentNode.author?.shortName ?: "",
                    commentText = commentNode.comment ?: "",
                    createdAt = commentNode.createdAt,
                    attachments = commentNode.attachments?.map { attachment ->
                        CommentAttachment(fileName = attachment.displayName ?: "")
                    } ?: emptyList(),
                    read = commentNode.read
                )
            }
        } ?: emptyList()
        return CommentsData(
            comments = comments,
            hasNextPage = hasNextPage,
            hasPreviousPage = hasPreviousPage,
            endCursor = newEndCursor,
            startCursor = newStartCursor
        )
    }
}

data class CommentsData(
    val comments: List<Comment>,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val endCursor: String? = null,
    val startCursor: String? = null
)

data class Comment(
    val authorId: Long,
    val authorName: String,
    val commentText: String,
    val createdAt: Date,
    val attachments: List<CommentAttachment>,
    val read: Boolean = true,
)

data class CommentAttachment(
    val fileName: String
)