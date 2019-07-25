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
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.student.Submission
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next
import org.threeten.bp.OffsetDateTime

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
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionTypes.first(), model.course, model.assignmentResult.dataOrThrow, model.assignmentResult.dataOrThrow.url)))
            } else {
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.ShowSubmitDialogView(model.assignmentResult.dataOrThrow, model.course, model.isArcEnabled)))
            }
        }
        AssignmentDetailsEvent.ViewSubmissionClicked -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowSubmissionView(model.assignmentId, model.course)))
        }
        AssignmentDetailsEvent.ViewUploadStatusClicked -> {
            // Force non null, we should only have a click if there is a submission ID
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowUploadStatusView(model.databaseSubmission!!)))
        }
        AssignmentDetailsEvent.PullToRefresh -> {
            Next.next(model.copy(isLoading = true), setOf(AssignmentDetailsEffect.LoadData(model.assignmentId, model.course.id, true)))
        }
        AssignmentDetailsEvent.AudioRecordingClicked -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowAudioRecordingView))
        }
        is AssignmentDetailsEvent.SendAudioRecordingClicked -> {
            if(event.file == null) {
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.ShowAudioRecordingError))
            } else {
                val assignment = model.assignmentResult!!.dataOrThrow
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.UploadAudioMediaSubmission(event.file, model.course, assignment)))
            }
        }
        is AssignmentDetailsEvent.SubmissionStatusUpdated -> {
            Next.next(
                model.copy(
                    databaseSubmission = event.submission
                )
            )
        }
        is AssignmentDetailsEvent.DataLoaded -> {
            val dbSubmission = dbSubmissionIfNewest(event.submission, event.assignmentResult?.dataOrNull?.submission)
            Next.next(model.copy(
                isLoading = false,
                assignmentResult = event.assignmentResult,
                isArcEnabled = event.isArcEnabled,
                ltiTool = event.ltiTool,
                databaseSubmission = dbSubmission
            ))
        }
        is AssignmentDetailsEvent.SubmissionTypeClicked -> {
            // If a user is trying to submit something to an assignment and the assignment is null, something is terribly wrong.
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowCreateSubmissionView(event.submissionType, model.course, model.assignmentResult!!.dataOrThrow)))
        }
        is AssignmentDetailsEvent.InternalRouteRequested -> {
            val effect = AssignmentDetailsEffect.RouteInternally(
                url = event.url,
                course = model.course,
                assignment = model.assignmentResult!!.dataOrThrow
            )
            Next.dispatch(setOf(effect))
        }
    }

    private fun dbSubmissionIfNewest(dbSubmission: Submission?, apiSubmission: com.instructure.canvasapi2.models.Submission?): Submission? {
        return when {
            dbSubmission == null -> null
            apiSubmission == null -> dbSubmission
            apiSubmission.submittedAt == null -> dbSubmission
            dbSubmission.lastActivityDate == null -> null
            OffsetDateTime.parse(apiSubmission.submittedAt.toApiString()).isBefore(dbSubmission.lastActivityDate) -> dbSubmission
            else -> null
        }
    }
}
