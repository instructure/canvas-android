package com.instructure.pandautils.room.appdatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.MediaComment

@Entity
data class MediaCommentEntity(
    @PrimaryKey
    val mediaId: String,
    var displayName: String? = null,
    var url: String? = null,
    var mediaType: String? = null,
    var contentType: String? = null
) {
    constructor(mediaComment: MediaComment) : this(
        mediaComment.mediaId!!,
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
