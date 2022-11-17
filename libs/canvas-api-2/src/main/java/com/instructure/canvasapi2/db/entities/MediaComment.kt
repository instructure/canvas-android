package com.instructure.canvasapi2.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MediaComment(
    @PrimaryKey
    val mediaId: String,
    var displayName: String? = null,
    var url: String? = null,
    var mediaType: String? = null,
    var contentType: String? = null
) {
    constructor(mediaComment: com.instructure.canvasapi2.models.MediaComment): this(
        mediaComment.mediaId!!,
        mediaComment.displayName,
        mediaComment.url,
        mediaComment.mediaType?.name,
        mediaComment.contentType
    )
}
