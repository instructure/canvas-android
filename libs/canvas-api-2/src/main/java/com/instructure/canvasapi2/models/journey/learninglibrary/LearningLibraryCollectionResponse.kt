package com.instructure.canvasapi2.models.journey.learninglibrary

data class LearningLibraryCollectionResponse(
    val learningLibraryCollections: List<LearningLibraryCollection>,
    val pageInfo: LearningLibraryPageInfo
)