package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.pandautils.room.offline.entities.SubmissionEntity

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
data class MediaCommentEntity(
    @PrimaryKey
    val mediaId: String,
    val submissionId: Long,
    val attemptId: Long,
    val displayName: String? = null,
    val url: String? = null,
    val mediaType: String? = null,
    val contentType: String? = null
) {
    constructor(mediaComment: MediaComment, submissionId: Long, attemptId: Long) : this(
        mediaComment.mediaId!!,
        submissionId,
        attemptId,
        mediaComment.displayName,
        mediaComment.url,
        mediaComment.mediaType?.name,
        mediaComment.contentType
    )

    fun toApiModel() = MediaComment(
        mediaId,
        displayName,
        url,
        mediaType?.let { MediaComment.MediaType.valueOf(it) },
        contentType
    )
}
