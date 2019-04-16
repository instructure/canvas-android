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
package com.instructure.student.mobius.assignmentDetails

import com.instructure.canvasapi2.models.Assignment
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class AssignmentDetailsUpdate : UpdateInit<AssignmentDetailsModel, AssignmentDetailsEvent, AssignmentDetailsEffect>() {
    override fun performInit(model: AssignmentDetailsModel): First<AssignmentDetailsModel, AssignmentDetailsEffect> {
        return First.first(model.copy(isLoading = true), setOf(AssignmentDetailsEffect.LoadData(model.assignmentId, model.course.id, false)))
    }

    override fun update(
        model: AssignmentDetailsModel,
        event: AssignmentDetailsEvent
    ): Next<AssignmentDetailsModel, AssignmentDetailsEffect> = when (event) {
        AssignmentDetailsEvent.SubmitAssignmentClicked -> {
            // If a user is trying to submit something to an assignment and the assignment is null, something is terribly wrong.
            val submissionTypes = model.assignmentResult!!.dataOrThrow.getSubmissionTypes()
            if(submissionTypes.size == 1 && !(submissionTypes.contains(Assignment.SubmissionType.ONLINE_UPLOAD) && model.isArcEnabled)) {
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionTypes.first(), model.course.id, model.assignmentResult.dataOrThrow)))
            } else {
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.ShowSubmitDialogView(model.assignmentResult.dataOrThrow, model.course, model.isArcEnabled)))
            }
        }
        AssignmentDetailsEvent.ViewSubmissionClicked -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowSubmissionView(model.assignmentId, model.course)))
        }
        AssignmentDetailsEvent.ViewUploadStatusClicked -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowUploadStatusView(model.assignmentId, model.course)))
        }
        AssignmentDetailsEvent.PullToRefresh -> {
            Next.next(model.copy(isLoading = true), setOf(AssignmentDetailsEffect.LoadData(model.assignmentId, model.course.id, true)))
        }
        is AssignmentDetailsEvent.SubmissionStatusUpdated -> {
            Next.next(model.copy(status = event.status))
        }
        is AssignmentDetailsEvent.DataLoaded -> {
            Next.next(model.copy(
                    isLoading = false,
                    assignmentResult = event.assignmentResult,
                    isArcEnabled = event.isArcEnabled
            ))
        }
        is AssignmentDetailsEvent.SubmissionTypeClicked -> {
            // If a user is trying to submit something to an assignment and the assignment is null, something is terribly wrong.
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowCreateSubmissionView(event.submissionType, model.course.id, model.assignmentResult!!.dataOrThrow)))
        }
    }
}
