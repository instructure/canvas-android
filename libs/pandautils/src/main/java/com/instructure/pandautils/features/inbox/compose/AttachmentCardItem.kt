package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.models.Attachment

data class AttachmentCardItem (
    val attachment: Attachment,
    val status: AttachmentStatus // TODO: Currently this is not used for proper state handling, but if the upload process will be refactored it can be useful
)

enum class AttachmentStatus {
    UPLOADING,
    UPLOADED,
    FAILED

}
