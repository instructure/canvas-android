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
package com.instructure.horizon.features.learn.program.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnProgramListUiState(
    val loadingState: LoadingState = LoadingState(),
    val filteredPrograms: List<LearnProgramState> = emptyList(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val updateSearchQuery: (TextFieldValue) -> Unit = {},
    val selectedFilterValue: LearnProgramFilterOption = LearnProgramFilterOption.All,
    val updateFilterValue: (LearnProgramFilterOption) -> Unit = {},
    val visibleItemCount: Int = 10,
    val increaseVisibleItemCount: () -> Unit = {},
)

data class LearnProgramState(
    val programName: String = "",
    val programId: String = "",
    val programProgress: Double = 0.0,
    val programChips: List<LearnProgramChipState> = emptyList(),
)

data class LearnProgramChipState(
    val label: String = "",
    @DrawableRes val iconRes: Int? = null,
)

enum class LearnProgramFilterOption(@StringRes val labelRes: Int) {
    All(R.string.learnProgramListFilterAll),
    NotStarted(R.string.learnProgramListFilterNotStarted),
    InProgress(R.string.learnProgramListFilterInProgress),
    Completed(R.string.learnProgramListFilterCompleted);

    companion object {
        internal fun Double.getProgressOption(): LearnProgramFilterOption {
            return when (this) {
                0.0 -> NotStarted
                in 0.0..<100.0 -> InProgress
                else -> Completed
            }
        }
    }
}