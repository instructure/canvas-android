package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Author

@Entity
data class AuthorEntity(
    @PrimaryKey val id: Long,
    val displayName: String? = null,
    val avatarImageUrl: String? = null,
    val htmlUrl: String? = null,
    val pronouns: String? = null
) {
    constructor(author: Author) : this(
        author.id,
        author.displayName,
        author.avatarImageUrl,
        author.htmlUrl,
        author.pronouns
    )

    fun toApiModel(): Author {
        return Author(
            id,
            displayName,
            avatarImageUrl,
            htmlUrl,
            pronouns
        )
    }
}
