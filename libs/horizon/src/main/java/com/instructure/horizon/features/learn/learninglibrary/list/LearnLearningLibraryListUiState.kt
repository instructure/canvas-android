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
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnLearningLibraryListUiState(
    val loadingState: LoadingState = LoadingState(),
    val collections: List<LearnLearningLibraryCollectionState> = emptyList(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val itemsToDisplays: Int = 1,
    val increaseItemsToDisplay: () -> Unit = {},
    val updateSearchQuery: (TextFieldValue) -> Unit = {},
    val onBookmarkClicked: (itemId: String) -> Unit = {},
    val onEnrollClicked: (itemId: String) -> Unit = {},
)