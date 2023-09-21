package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Attachment
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SubmissionCommentEntity::class,
            parentColumns = ["id"],
            childColumns = ["submissionCommentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttachmentEntity(
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
    val submissionCommentId: Long? = null,
    val submissionId: Long? = null,
    val attempt: Long? = null
) {
    constructor(
        attachment: Attachment,
        workerId: String? = null,
        submissionCommentId: Long? = null,
        submissionId: Long? = null,
        attempt: Long? = null
    ) : this(
        id = attachment.id,
        contentType = attachment.contentType,
        filename = attachment.filename,
        displayName = attachment.displayName,
        url = attachment.url,
        thumbnailUrl = attachment.thumbnailUrl,
        previewUrl = attachment.previewUrl,
        createdAt = attachment.createdAt,
        size = attachment.size,
        workerId = workerId,
        submissionCommentId = submissionCommentId,
        submissionId = submissionId,
        attempt = attempt
    )

    fun toApiModel() = Attachment(
        id = id,
        contentType = contentType,
        filename = filename,
        displayName = displayName,
        url = url,
        thumbnailUrl = thumbnailUrl,
        previewUrl = previewUrl,
        createdAt = createdAt,
        size = size
    )
}
