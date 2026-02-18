package com.instructure.horizon.features.learn.learninglibrary.common

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.R

enum class LearnLearningLibraryTypeFilter(@StringRes val labelRes: Int) {
    All(R.string.LearnLearningLibraryTypeFilterAllLabel),
    Assessments(R.string.LearnLearningLibraryTypeFilterAssessmentsLabel),
    Assignments(R.string.LearnLearningLibraryTypeFilterAssignmentsLabel),
    ExternalLinks(R.string.LearnLearningLibraryTypeFilterExternalLinksLabel),
    ExternalTools(R.string.LearnLearningLibraryTypeFilterExternalToolsLabel),
    Files(R.string.LearnLearningLibraryTypeFilterFilesLabel),
    Pages(R.string.LearnLearningLibraryTypeFilterPagesLabel)
    ;

    fun toCollectionItemType(): CollectionItemType? {
        return when(this) {
            All -> null
            Assignments -> CollectionItemType.ASSIGNMENT
            ExternalLinks -> CollectionItemType.EXTERNAL_URL
            ExternalTools -> CollectionItemType.EXTERNAL_TOOL
            Files -> CollectionItemType.FILE
            Pages -> CollectionItemType.PAGE
            else -> null
        }
    }
}