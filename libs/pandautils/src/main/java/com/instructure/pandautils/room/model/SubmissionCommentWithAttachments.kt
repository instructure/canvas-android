package com.instructure.pandautils.room.model

import androidx.room.Embedded
import androidx.room.Relation
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.pandautils.room.entities.AttachmentEntity
import com.instructure.pandautils.room.entities.AuthorEntity
import com.instructure.pandautils.room.entities.MediaCommentEntity
import com.instructure.pandautils.room.entities.SubmissionCommentEntity

data class SubmissionCommentWithAttachments(
    @Embedded
    val submissionComment: SubmissionCommentEntity,
    @Relation(
        parentColumn = "mediaCommentId",
        entityColumn = "mediaId"
    )
    val mediaComment: MediaCommentEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "submissionCommentId"
    )
    val attachments: List<AttachmentEntity>?,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: AuthorEntity?
) {
    fun toApiModel(): SubmissionComment {
        return SubmissionComment(
            submissionComment.id,
            submissionComment.authorId,
            submissionComment.authorName,
            submissionComment.authorPronouns,
            submissionComment.comment,
            submissionComment.createdAt,
            mediaComment?.toApiModel(),
            attachments?.let { it.map { it.toApiModel() } }?.let { ArrayList(it) } ?: ArrayList(),
            author?.toApiModel()
        )
    }
}