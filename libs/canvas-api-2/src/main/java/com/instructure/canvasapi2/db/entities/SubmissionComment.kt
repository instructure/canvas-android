package com.instructure.canvasapi2.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class SubmissionComment(
    @PrimaryKey val id: Long = 0,
    val authorId: Long = 0,
    val authorName: String? = null,
    val authorPronouns: String? = null,
    val comment: String? = null,
    val createdAt: Date? = null,
    val mediaCommentId: String? = null
) {
    constructor(submissionComment: com.instructure.canvasapi2.models.SubmissionComment): this(
        submissionComment.id,
        submissionComment.authorId,
        submissionComment.authorName,
        submissionComment.authorPronouns,
        submissionComment.comment,
        submissionComment.createdAt,
        submissionComment.mediaComment?.mediaId
    )
}
