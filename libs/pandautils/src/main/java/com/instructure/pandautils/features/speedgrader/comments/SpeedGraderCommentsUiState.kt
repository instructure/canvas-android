package com.instructure.pandautils.features.speedgrader.comments


data class SpeedGraderCommentsUiState(
    val comments: List<SpeedGraderComment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
)

data class SpeedGraderComment(
    val id: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    val createdAt: String = "",
    val isOwnComment: Boolean = false,
    val attachments: List<SpeedGraderCommentAttachment> = emptyList()
)

data class SpeedGraderCommentAttachment(
    val id: String = "",
    val displayName: String = "",
    val contentType: String = "",
    val size: String = "",
    val url: String = "",
    val thumbnailUrl: String? = null,
    val createdAt: String = "",
)
