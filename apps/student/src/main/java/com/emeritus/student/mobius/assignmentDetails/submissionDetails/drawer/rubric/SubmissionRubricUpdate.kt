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

import com.emeritus.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SubmissionRubricUpdate : UpdateInit<SubmissionRubricModel, SubmissionRubricEvent, SubmissionRubricEffect>() {
    override fun performInit(model: SubmissionRubricModel): First<SubmissionRubricModel, SubmissionRubricEffect> {
        return First.first(model)
    }

    override fun update(
        model: SubmissionRubricModel,
        event: SubmissionRubricEvent
    ): Next<SubmissionRubricModel, SubmissionRubricEffect> {
        return when (event) {
            is SubmissionRubricEvent.LongDescriptionClicked -> {
                val criterion = model.assignment.rubric!!.first { it.id == event.criterionId }
                val effect = SubmissionRubricEffect.ShowLongDescription(
                    criterion.description.orEmpty(),
                    criterion.longDescription.orEmpty()
                )
                Next.dispatch(setOf(effect))
            }
            is SubmissionRubricEvent.RatingClicked -> {
                val currentSelection = model.selectedRatingMap[event.criterionId]
                val selectionMap = if (event.ratingId == currentSelection) {
                    model.selectedRatingMap - event.criterionId
                } else {
                    model.selectedRatingMap + (event.criterionId to event.ratingId)
                }
                val newModel = model.copy(selectedRatingMap = selectionMap)
                Next.next(newModel)
            }
        }
    }
}
