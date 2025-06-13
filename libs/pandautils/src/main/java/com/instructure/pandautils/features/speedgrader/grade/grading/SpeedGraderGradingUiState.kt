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
package com.instructure.pandautils.features.speedgrader.grade.grading

import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.type.LatePolicyStatusType
import com.instructure.pandautils.R
import java.util.Date

data class SpeedGraderGradingUiState(
    val loading: Boolean = true,
    val pointsPossible: Double? = null,
    val enteredGrade: String? = null,
    val enteredScore: Float? = null,
    val grade: String? = null,
    val score: Double? = null,
    val pointsDeducted: Double? = null,
    val gradingType: GradingType? = null,
    val dueDate: Date? = null,
    val daysLate: Int? = null,
    val letterGrades: List<GradingSchemeRow> = emptyList(),
    val gradingStatuses: List<LatePolicyStatusType> = emptyList(),
    val gradingStatus: LatePolicyStatusType = LatePolicyStatusType.none,
    val onScoreChange: (Float?) -> Unit,
    val onPercentageChange: (Float?) -> Unit
)

fun LatePolicyStatusType.stringRes() {
    when (this) {
        LatePolicyStatusType.none -> R.string.gradingStatus_none
        LatePolicyStatusType.late -> R.string.gradingStatus_late
        LatePolicyStatusType.missing -> R.string.gradingStatus_missing
        LatePolicyStatusType.extended -> R.string.gradingStatus_extended
        else -> R.string.gradingStatus_none
    }
}