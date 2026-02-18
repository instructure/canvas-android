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
package com.instructure.horizon.features.learn.learninglibrary.bookmark

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnLearningLibraryBookmarkUiState(
    val loadingState: LoadingState = LoadingState(),
    val items: List<LearnLearningLibraryCollectionItemState> = emptyList(),
    val showMoreButton: Boolean = false,
    val onShowMoreClicked: () -> Unit = {},
    val isMoreButtonLoading: Boolean = false,
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val updateSearchQuery: (TextFieldValue) -> Unit = {},
    val typeFilter: LearnLearningLibraryTypeFilter? = null,
    val updateTypeFilter: (LearnLearningLibraryTypeFilter?) -> Unit = {},
    val onBookmarkClicked: (itemId: String) -> Unit = {},
    val onEnrollClicked: (itemId: String) -> Unit = {},
)