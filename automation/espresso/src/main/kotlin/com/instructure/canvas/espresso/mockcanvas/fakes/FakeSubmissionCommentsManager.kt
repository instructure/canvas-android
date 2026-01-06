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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper
import java.util.Date

class FakeSubmissionCommentsManager : SubmissionCommentsManager {
    override suspend fun getSubmissionComments(
        userId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): SubmissionCommentsResponseWrapper {
        val data = MockCanvas.data

        val submission = data.submissions[assignmentId]?.find { it.userId == userId }

        val graphqlComments = submission?.submissionComments?.map { comment ->
            SubmissionCommentsQuery.Node1(
                _id = comment.id.toString(),
                author = SubmissionCommentsQuery.Author(
                    _id = comment.authorId.toString(),
                    name = comment.authorName,
                    email = null,
                    avatarUrl = null,
                    pronouns = comment.authorPronouns
                ),
                mediaObject = null,
                comment = comment.comment,
                mediaCommentId = null,
                createdAt = comment.createdAt ?: Date(),
                canReply = true,
                draft = false,
                attempt = comment.attempt?.toInt() ?: 1,
                read = true,
                attachments = comment.attachments.map { attachment ->
                    SubmissionCommentsQuery.Attachment(
                        _id = attachment.id.toString(),
                        id = attachment.id.toString(),
                        contentType = attachment.contentType,
                        createdAt = attachment.createdAt,
                        displayName = attachment.displayName,
                        title = attachment.filename,
                        size = attachment.size.toString(),
                        thumbnailUrl = attachment.thumbnailUrl,
                        url = attachment.url
                    )
                }
            )
        } ?: emptyList()

        return SubmissionCommentsResponseWrapper(
            comments = graphqlComments,
            data = SubmissionCommentsQuery.Data(
                submission = null
            )
        )
    }

    override suspend fun createSubmissionComment(
        submissionId: Long,
        comment: String,
        attempt: Int?,
        isGroupComment: Boolean
    ): CreateSubmissionCommentMutation.Data {
        val data = MockCanvas.data

        val submission = data.submissions.values.flatten().find { it.id == submissionId }

        if (submission != null) {
            val newComment = SubmissionComment(
                id = data.newItemId(),
                authorId = data.currentUser?.id ?: 0L,
                authorName = data.currentUser?.shortName ?: "Teacher",
                authorPronouns = data.currentUser?.pronouns,
                attempt = attempt?.toLong() ?: submission.attempt,
                comment = comment,
                createdAt = Date(),
                attachments = arrayListOf()
            )

            submission.submissionComments = submission.submissionComments + newComment
        }

        return CreateSubmissionCommentMutation.Data(null)
    }
}