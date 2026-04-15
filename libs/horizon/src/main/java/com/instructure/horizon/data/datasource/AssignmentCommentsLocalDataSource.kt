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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.managers.graphql.horizon.Comment
import com.instructure.canvasapi2.managers.graphql.horizon.CommentAttachment
import com.instructure.canvasapi2.managers.graphql.horizon.CommentsData
import com.instructure.horizon.database.dao.HorizonAssignmentCommentDao
import com.instructure.horizon.database.entity.HorizonAssignmentCommentAttachmentEntity
import com.instructure.horizon.database.entity.HorizonAssignmentCommentEntity
import java.util.Date
import javax.inject.Inject

class AssignmentCommentsLocalDataSource @Inject constructor(
    private val commentDao: HorizonAssignmentCommentDao,
) {

    suspend fun getComments(assignmentId: Long, attempt: Int): CommentsData {
        val commentEntities = commentDao.getComments(assignmentId, attempt)
        val commentIds = commentEntities.map { it.id }
        val attachmentEntities = if (commentIds.isNotEmpty()) {
            commentDao.getAttachments(commentIds)
        } else {
            emptyList()
        }
        val attachmentsByCommentId = attachmentEntities.groupBy { it.commentId }
        val comments = commentEntities.map { entity ->
            entity.toComment(attachmentsByCommentId[entity.id] ?: emptyList())
        }
        return CommentsData(
            comments = comments,
            hasNextPage = false,
            hasPreviousPage = false,
        )
    }

    suspend fun saveComments(assignmentId: Long, attempt: Int, commentsData: CommentsData) {
        val commentWithAttachments = commentsData.comments.map { comment ->
            val commentEntity = HorizonAssignmentCommentEntity(
                assignmentId = assignmentId,
                attempt = attempt,
                authorId = comment.authorId,
                authorName = comment.authorName,
                commentText = comment.commentText,
                createdAtMs = comment.createdAt.time,
                read = comment.read,
            )
            val attachmentEntities = comment.attachments.map { attachment ->
                HorizonAssignmentCommentAttachmentEntity(
                    attachmentId = attachment.attachmentId,
                    commentId = 0,
                    fileName = attachment.fileName,
                    fileUrl = attachment.fileUrl,
                    fileType = attachment.fileType,
                )
            }
            commentEntity to attachmentEntities
        }
        commentDao.replaceCommentsForAttempt(assignmentId, attempt, commentWithAttachments)
    }

    suspend fun getUnreadCommentCount(assignmentId: Long): Int {
        return commentDao.getUnreadCommentCount(assignmentId)
    }

    private fun HorizonAssignmentCommentEntity.toComment(
        attachments: List<HorizonAssignmentCommentAttachmentEntity>,
    ): Comment {
        return Comment(
            authorId = authorId,
            authorName = authorName,
            commentText = commentText,
            createdAt = Date(createdAtMs),
            attachments = attachments.map { it.toCommentAttachment() },
            read = read,
        )
    }

    private fun HorizonAssignmentCommentAttachmentEntity.toCommentAttachment(): CommentAttachment {
        return CommentAttachment(
            attachmentId = attachmentId,
            fileName = fileName,
            fileUrl = fileUrl,
            fileType = fileType,
        )
    }
}