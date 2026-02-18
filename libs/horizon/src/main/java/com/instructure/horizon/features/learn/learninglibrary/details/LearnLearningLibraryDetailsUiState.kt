/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.learn.learninglibrary.details

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnLearningLibraryDetailsUiState(
    val loadingState: LoadingState = LoadingState(),
    val collectionName: String = "",
    val items: List<LearnLearningLibraryCollectionItemState> = emptyList(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val updateSearchQuery: (TextFieldValue) -> Unit = {},
    val itemsToDisplays: Int = 1,
    val increaseItemsToDisplay: () -> Unit = {},
    val selectedStatusFilter: LearnLearningLibraryDetailsStatusFilter = LearnLearningLibraryDetailsStatusFilter.All,
    val updateSelectedStatusFilter: (LearnLearningLibraryDetailsStatusFilter) -> Unit = {},
    val selectedTypeFilter: LearnLearningLibraryDetailsTypeFilter = LearnLearningLibraryDetailsTypeFilter.All,
    val updateTypeFilter: (LearnLearningLibraryDetailsTypeFilter) -> Unit = {},
    val onBookmarkClicked: (itemId: String) -> Unit = {},
    val onEnrollClicked: (itemId: String) -> Unit = {},
)

enum class LearnLearningLibraryDetailsStatusFilter(@StringRes val labelRes: Int) {
    All(R.string.learnLearningLibraryDetailsStatusFilterAllLabel),
    Completed(R.string.learnLearningLibraryDetailsStatusFilterCompletedLabel),
    Bookmarked(R.string.learnLearningLibraryDetailsStatusFilterBookmarkedLabel)
}

enum class LearnLearningLibraryDetailsTypeFilter(@StringRes val labelRes: Int) {
    All(R.string.LearnLearningLibraryDetailsTypeFilterAllLabel),
    Assessments(R.string.LearnLearningLibraryDetailsTypeFilterAssessmentsLabel),
    Assignments(R.string.LearnLearningLibraryDetailsTypeFilterAssignmentsLabel),
    ExternalLinks(R.string.LearnLearningLibraryDetailsTypeFilterExternalLinksLabel),
    ExternalTools(R.string.LearnLearningLibraryDetailsTypeFilterExternalToolsLabel),
    Files(R.string.LearnLearningLibraryDetailsTypeFilterFilesLabel),
    Pages(R.string.LearnLearningLibraryDetailsTypeFilterPagesLabel)
}