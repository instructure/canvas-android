package com.instructure.pandautils.features.speedgrader.comments


data class SpeedGraderCommentsUiState(
    val comments: List<SpeedGraderComment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
)

data class SpeedGraderComment(
    val id: String,
    val authorName: String,
    val authorId: String,
    val content: String,
    val createdAt: String,
    val attachments: List<SpeedGraderCommentAttachment> = emptyList()
)

data class SpeedGraderCommentAttachment(
    val displayName: String,
    val contentType: String
)
