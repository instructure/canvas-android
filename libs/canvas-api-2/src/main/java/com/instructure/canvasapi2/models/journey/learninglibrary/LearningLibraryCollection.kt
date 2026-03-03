package com.instructure.canvasapi2.models.journey.learninglibrary

import java.util.Date

data class LearningLibraryCollection(
    val id: String,
    val name: String,
    val publicName: String?,
    val description: String?,
    val rootAccountUuid: String,
    val accountId: String,
    val deletedAt: Date?,
    val createdAt: Date,
    val updatedAt: Date
)