package com.instructure.canvasapi2.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Author(
    @PrimaryKey val id: Long,
    val displayName: String? = null,
    val avatarImageUrl: String? = null,
    val htmlUrl: String? = null,
    val pronouns: String? = null
) {
    constructor(author: com.instructure.canvasapi2.models.Author): this(
        author.id,
        author.displayName,
        author.avatarImageUrl,
        author.htmlUrl,
        author.pronouns
    )
}
