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
package com.instructure.horizon.features.learn.mycontent

import androidx.annotation.DrawableRes
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnMyContentUiState(
    val searchQuery: TextFieldValue = TextFieldValue(),
    val updateSearchQuery: (TextFieldValue) -> Unit = {},
    val sortByOption: LearnLearningLibrarySortOption = LearnLearningLibrarySortOption.MostRecent,
    val inProgressState: LearnMyContentInProgressUiState = LearnMyContentInProgressUiState(),
    val completedState: LearnMyContentCompletedUiState = LearnMyContentCompletedUiState(),
    val savedState: LearnMyContentSavedUiState = LearnMyContentSavedUiState(),
)

enum class LearnMyContentTab {
    InProgress,
    Completed,
    Saved,
}

data class LearnMyContentInProgressUiState(
    val loadingState: LoadingState = LoadingState(),
    val contentCards: List<LearnContentCardState> = emptyList(),
    val visibleItemCount: Int = 10,
    val increaseVisibleItemCount: () -> Unit = {},
)

data class LearnMyContentCompletedUiState(
    val loadingState: LoadingState = LoadingState(),
    val contentCards: List<LearnContentCardState> = emptyList(),
    val visibleItemCount: Int = 10,
    val increaseVisibleItemCount: () -> Unit = {},
)

data class LearnMyContentSavedUiState(
    val loadingState: LoadingState = LoadingState(),
    val contentCards: List<LearnLearningLibraryCollectionItemState> = emptyList(),
    val totalItemCount: Int = 10,
    val showMoreButton: Boolean = false,
    val isMoreLoading: Boolean = false,
    val increaseTotalItemCount: () -> Unit = {},
)

data class LearnContentCardState(
    val imageUrl: String? = null,
    val name: String = "",
    val progress: Double? = null,
    val route: String = "",
    val buttonLabel: String? = null,
    val cardChips: List<LearnContentCardChipState> = emptyList(),
)

data class LearnContentCardChipState(
    val label: String = "",
    @DrawableRes val iconRes: Int? = null,
    val color: StatusChipColor = StatusChipColor.Grey,
)