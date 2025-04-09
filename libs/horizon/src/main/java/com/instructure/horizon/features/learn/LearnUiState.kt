/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.Course
import com.instructure.horizon.R
import com.instructure.horizon.util.ScreenState

data class LearnUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val course: Course? = null,
    val availableTabs: List<LearnTab> = LearnTab.entries,
)

enum class LearnTab(@StringRes val titleRes: Int, ) {
    MyProgress(titleRes = R.string.myProgress),
    Overview(titleRes = R.string.overview),
    Scores(titleRes = R.string.scores),
    Notes(titleRes = R.string.notes);
}