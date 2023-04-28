package com.instructure.pandautils.room.appdatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.SubmissionComment
import java.util.*

@Entity
data class SubmissionCommentEntity(
    @PrimaryKey val id: Long = 0,
    val authorId: Long = 0,
    val authorName: String? = null,
    val authorPronouns: String? = null,
    val comment: String? = null,
    val createdAt: Date? = null,
    val mediaCommentId: String? = null,
    val attemptId: Long? = null,
    val submissionId: Long? = null
) {
    constructor(submissionComment: SubmissionComment): this(
        submissionComment.id,
        submissionComment.authorId,
        submissionComment.authorName,
        submissionComment.authorPronouns,
        submissionComment.comment,
        submissionComment.createdAt,
        submissionComment.mediaComment?.mediaId,
        submissionComment.attempt
    )
}
