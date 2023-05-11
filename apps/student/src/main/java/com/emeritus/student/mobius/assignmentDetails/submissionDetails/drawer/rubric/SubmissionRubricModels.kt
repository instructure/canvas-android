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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission

sealed class SubmissionRubricEvent {
    data class LongDescriptionClicked(val criterionId: String) : SubmissionRubricEvent()
    data class RatingClicked(val criterionId: String, val ratingId: String) : SubmissionRubricEvent()
}

sealed class SubmissionRubricEffect {
    data class ShowLongDescription(
        val description: String,
        val longDescription: String
    ) : SubmissionRubricEffect()
}

data class SubmissionRubricModel(
    val assignment: Assignment,
    val submission: Submission,
    val selectedRatingMap: Map<String, String> = emptyMap()
)
