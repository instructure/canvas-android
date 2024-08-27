package com.instructure.pandautils.features.inbox.util

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

}