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
package com.instructure.horizon.features.learn.mycontent.common

import androidx.annotation.DrawableRes
import com.instructure.horizon.horizonui.molecules.StatusChipColor

data class LearnContentCardState(
    val imageUrl: String? = null,
    val name: String = "",
    val progress: Double? = null,
    val route: String = "",
    val buttonState: LearnContentCardButtonState? = null,
    val cardChips: List<LearnContentCardChipState> = emptyList(),
    val courseNames: List<String> = emptyList(),
    val isSynced: Boolean = true,
)

data class LearnContentCardButtonState(
    val label: String = "",
    val route: Any = "",
)

data class LearnContentCardChipState(
    val label: String = "",
    @DrawableRes val iconRes: Int? = null,
    val color: StatusChipColor = StatusChipColor.Grey,
)