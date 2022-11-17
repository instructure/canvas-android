package com.instructure.canvasapi2.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Attachment(
    @PrimaryKey val id: Long,
    val contentType: String? = null,
    val filename: String? = null,
    val displayName: String? = null,
    val url: String? = null,
    val thumbnailUrl: String? = null,
    val previewUrl: String? = null,
    val createdAt: Date? = null,
    val size: Long = 0,
    //Used for file upload result
    val workerId: String? = null,
    //Used for Submission comments
    val submissionCommentId: Long? = null
) {
    constructor(attachment: com.instructure.canvasapi2.models.Attachment, workerId: String? = null, submissionCommentId: Long? = null) : this(
        attachment.id,
        attachment.contentType,
        attachment.filename,
        attachment.displayName,
        attachment.url,
        attachment.thumbnailUrl,
        attachment.previewUrl,
        attachment.createdAt,
        attachment.size,
        workerId,
        submissionCommentId
    )
}