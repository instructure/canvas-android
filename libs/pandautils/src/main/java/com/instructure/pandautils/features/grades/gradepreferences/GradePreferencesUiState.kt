/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades.gradepreferences

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.R


data class GradePreferencesUiState(
    val show: Boolean = false,
    val courseName: String = "",
    val gradingPeriods: List<GradingPeriod> = emptyList(),
    val defaultGradingPeriod: GradingPeriod? = null,
    val selectedGradingPeriod: GradingPeriod? = null,
    val sortBy: SortBy = SortBy.DUE_DATE
) {
    val isDefault: Boolean
        get() = selectedGradingPeriod == defaultGradingPeriod
}

enum class SortBy(@StringRes val titleRes: Int) {
    DUE_DATE(R.string.sortByDueDate),
    GROUP(R.string.sortByGroup)
}
