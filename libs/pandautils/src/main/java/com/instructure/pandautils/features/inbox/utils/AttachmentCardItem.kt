package com.instructure.pandautils.features.inbox.utils

import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.Attachment

data class AttachmentCardItem (
    val attachment: Attachment,
    val status: AttachmentStatus, // TODO: Currently this is not used for proper state handling, but if the upload process will be refactored it can be useful
    val readOnly: Boolean
)

enum class AttachmentStatus {
    UPLOADING,
    UPLOADED,
    FAILED

    ;

    companion object {
        fun fromWorkInfoState(state: WorkInfo.State): AttachmentStatus {
            return when (state) {
                WorkInfo.State.SUCCEEDED -> AttachmentStatus.UPLOADED
                WorkInfo.State.FAILED -> AttachmentStatus.FAILED
                WorkInfo.State.ENQUEUED -> AttachmentStatus.UPLOADING
                WorkInfo.State.RUNNING -> AttachmentStatus.UPLOADING
                WorkInfo.State.BLOCKED -> AttachmentStatus.FAILED
                WorkInfo.State.CANCELLED -> AttachmentStatus.FAILED
            }
        }
    }
}