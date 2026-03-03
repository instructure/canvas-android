package com.instructure.horizon.features.learn.learninglibrary.common

import androidx.annotation.StringRes
import com.instructure.horizon.R

enum class LearnLearningLibraryStatusFilter(@StringRes val labelRes: Int) {
    All(R.string.learnLearningLibraryStatusFilterAllLabel),
    Completed(R.string.learnLearningLibraryStatusFilterCompletedLabel),
    Bookmarked(R.string.learnLearningLibraryStatusFilterBookmarkedLabel)
}