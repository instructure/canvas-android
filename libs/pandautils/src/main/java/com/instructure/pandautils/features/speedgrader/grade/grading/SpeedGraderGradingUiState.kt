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
import java.util.Date

data class SpeedGraderGradingUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val retryAction: (() -> Unit)? = null,
    val gradingType: GradingType? = null,
    val submittedAt: Date? = null,
    val letterGrades: List<GradingSchemeRow> = emptyList(),
    val gradingStatuses: List<GradeStatus> = emptyList(),
    val gradeHidden: Boolean = false,
    val onScoreChange: (Float?, String?) -> Unit,
    val onExcuse: (String?) -> Unit,
    val onPercentageChange: (Float?, String?) -> Unit,
    val onStatusChange: (GradeStatus, String?) -> Unit,
    val additionalRepliesNeeded: Int? = null,
    val gradableAssignments: List<GradableAssignment> = emptyList(),
    val onLateDaysChange: (Float?) -> Unit
)

data class GradeStatus(
    val id: Long? = null,
    val statusId: String? = null,
    val name: String
)

data class GradableAssignment(
    val tag: String? = null,
    val enteredGrade: String? = null,
    val enteredScore: Float? = null,
    val grade: String? = null,
    val score: Double? = null,
    val pointsPossible: Double? = null,
    val excused: Boolean = false,
    val gradingStatus: String? = null,
    val daysLate: Int? = null,
)