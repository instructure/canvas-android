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
        var hasNextPage = true
        var nextCursor: String? = null
        val allComments = mutableListOf<SubmissionCommentsQuery.Node>()
        var data: SubmissionCommentsQuery.Data? = null

        while (hasNextPage) {
            val nextCursorParam = if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
            val query = SubmissionCommentsQuery(userId.toString(), assignmentId.toString(), QLClientConfig.GRAPHQL_PAGE_SIZE, nextCursorParam)
            data = QLClientConfig.enqueueQuery(query).data
            val comments = data?.submission?.commentsConnection?.edges?.mapNotNull { edge ->
                edge?.node
            } ?: emptyList()
            allComments.addAll(comments)

            hasNextPage = data?.submission?.commentsConnection?.pageInfo?.hasNextPage ?: false
            nextCursor = data?.submission?.commentsConnection?.pageInfo?.endCursor
        }
        if (data == null) {
            throw Exception("No data returned from SubmissionCommentsQuery")
        }
        return SubmissionCommentsResponseWrapper(data, allComments)
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
