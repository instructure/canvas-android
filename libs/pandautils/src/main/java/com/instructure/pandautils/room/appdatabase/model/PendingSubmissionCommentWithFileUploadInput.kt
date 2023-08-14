package com.instructure.pandautils.room.appdatabase.model

import androidx.room.Embedded
import androidx.room.Relation
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.FileUploadWorkerData
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import java.util.*

data class PendingSubmissionCommentWithFileUploadInput(
    @Embedded
    val pendingSubmissionCommentEntity: PendingSubmissionCommentEntity,

    @Relation(
        parentColumn = "workerId",
        entityColumn = "workerId"
    )
    val fileUploadInput: FileUploadInputEntity?
) {
    fun toApiModel(): PendingSubmissionComment {
        return PendingSubmissionComment(
            pendingSubmissionCommentEntity.pageId,
            pendingSubmissionCommentEntity.comment
        ).apply {
            date = pendingSubmissionCommentEntity.date
            id = pendingSubmissionCommentEntity.id
            status = CommentSendStatus.valueOf(pendingSubmissionCommentEntity.status)
            workerId = if (pendingSubmissionCommentEntity.workerId != null && pendingSubmissionCommentEntity.workerId != "null") {
                UUID.fromString(pendingSubmissionCommentEntity.workerId)
            } else {
                null
            }
            workerInputData = FileUploadWorkerData(
                fileUploadInput?.filePaths ?: emptyList(),
                fileUploadInput?.courseId ?: -1L,
                fileUploadInput?.assignmentId ?: -1L,
                fileUploadInput?.userId ?: -1L
            )
            attemptId = pendingSubmissionCommentEntity.attemptId
        }
    }
}