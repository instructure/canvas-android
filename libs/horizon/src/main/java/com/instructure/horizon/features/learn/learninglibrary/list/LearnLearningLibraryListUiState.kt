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
package com.instructure.horizon.features.learn.learninglibrary.list

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryStatusFilter
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnLearningLibraryListUiState(
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val updateSearchQuery: (TextFieldValue) -> Unit = {},
    val typeFilter: LearnLearningLibraryTypeFilter = LearnLearningLibraryTypeFilter.All,
    val updateTypeFilter: (LearnLearningLibraryTypeFilter) -> Unit = {},
    val statusFilter: LearnLearningLibraryStatusFilter = LearnLearningLibraryStatusFilter.All,
    val updateStatusFilter: (LearnLearningLibraryStatusFilter) -> Unit = {},
    val collectionState: LearnLearningLibraryListCollectionUiState = LearnLearningLibraryListCollectionUiState(),
    val itemState: LearnLearningLibraryListItemUiState = LearnLearningLibraryListItemUiState()
) {
    fun isEmptyFilter(): Boolean {
        return searchQuery.text.isEmpty()
            && typeFilter == LearnLearningLibraryTypeFilter.All
            && statusFilter == LearnLearningLibraryStatusFilter.All
    }
}

data class LearnLearningLibraryListCollectionUiState(
    val loadingState: LoadingState = LoadingState(),
    val collections: List<LearnLearningLibraryCollectionState> = emptyList(),
    val itemsToDisplays: Int = 1,
    val increaseItemsToDisplay: () -> Unit = {},
    val onBookmarkClicked: (itemId: String) -> Unit = {},
)

data class LearnLearningLibraryListItemUiState(
    val loadingState: LoadingState = LoadingState(),
    val items: List<LearnLearningLibraryCollectionItemState> = emptyList(),
    val onShowMoreClicked: () -> Unit = {},
    val showMoreButton: Boolean = false,
    val isMoreButtonLoading: Boolean = false,
    val onBookmarkClicked: (itemId: String) -> Unit = {},
)