package com.instructure.canvasapi2.models.journey.learninglibrary

data class LearningLibraryPageInfo(
    val nextCursor: String?,
    val previousCursor: String?,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)