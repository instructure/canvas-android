package com.instructure.pandautils.room.appdatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import java.util.*

@Entity
data class PendingSubmissionCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var pageId: String,
    var comment: String? = null,
    var date: Date = Date(),
    var status: String = CommentSendStatus.DRAFT.toString(),
    var workerId: String? = null,
    var filePath: String? = null,
    var attemptId: Long? = null
) {
    constructor(pendingSubmissionComment: PendingSubmissionComment): this(
        pendingSubmissionComment.id,
        pendingSubmissionComment.pageId,
        pendingSubmissionComment.comment,
        pendingSubmissionComment.date,
        pendingSubmissionComment.status.toString(),
        pendingSubmissionComment.workerId.toString(),
        pendingSubmissionComment.filePath,
        pendingSubmissionComment.attemptId
    )

    fun toApiModel(): PendingSubmissionComment {
        return PendingSubmissionComment(
            pageId,
            comment
        ).apply {
            this.date = this@PendingSubmissionCommentEntity.date
            this.id = this@PendingSubmissionCommentEntity.id
            this.status = CommentSendStatus.valueOf(this@PendingSubmissionCommentEntity.status)
            this.workerId = if (this@PendingSubmissionCommentEntity.workerId != null && this@PendingSubmissionCommentEntity.workerId != "null") UUID.fromString(this@PendingSubmissionCommentEntity.workerId) else null
            this.filePath = this@PendingSubmissionCommentEntity.filePath ?: ""
            this.attemptId = this@PendingSubmissionCommentEntity.attemptId
        }
    }
}