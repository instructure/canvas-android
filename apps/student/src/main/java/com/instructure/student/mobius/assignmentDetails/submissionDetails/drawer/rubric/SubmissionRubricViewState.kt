/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric

import com.instructure.student.mobius.assignmentDetails.ui.gradeCell.GradeCellViewState

data class SubmissionRubricViewState(
    val listData: List<RubricListData>
)

sealed class RubricListData {
    object Empty : RubricListData()
    data class Grade(val state: GradeCellViewState) : RubricListData()
    data class Criterion(
        val description: String,
        val criterionId: String,
        val showLongDescriptionButton: Boolean,
        val ratingDescription: String?,
        val ratings: List<RatingData>,
        val comment: String?
    ) : RubricListData()
}

data class RatingData(
    val points: String,
    val description: String?,
    val isSelected: Boolean,
    val useSmallText: Boolean = false
)
