package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.pandautils.room.offline.entities.SubmissionEntity
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SubmissionEntity::class,
            parentColumns = ["id", "attempt"],
            childColumns = ["submissionId", "attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
    constructor(submissionComment: SubmissionComment, submissionId: Long, attemptId: Long) : this(
        id = submissionComment.id,
        authorId = submissionComment.authorId,
        authorName = submissionComment.authorName,
        authorPronouns = submissionComment.authorPronouns,
        comment = submissionComment.comment,
        createdAt = submissionComment.createdAt,
        mediaCommentId = submissionComment.mediaComment?.mediaId,
        attemptId = attemptId,
        submissionId = submissionId
    )
}
