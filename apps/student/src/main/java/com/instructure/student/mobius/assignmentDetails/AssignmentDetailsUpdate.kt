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
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.mapToAttachment
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
            Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(getSubmitClickedEffect(model.assignmentResult, model, submissionTypes))
        }
        AssignmentDetailsEvent.ViewSubmissionClicked -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowSubmissionView(model.assignmentId, model.course, model.isObserver)))
        }
        AssignmentDetailsEvent.DiscussionAttachmentClicked -> {
            // They can't click on an attachment if there aren't any present
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowDiscussionAttachment(model.assignmentResult!!.dataOrThrow.discussionTopicHeader?.attachments?.first()?.mapToAttachment()!!, model.course)))
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
        AssignmentDetailsEvent.VideoRecordingClicked -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowVideoRecordingView))
        }
        AssignmentDetailsEvent.ChooseMediaClicked -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowMediaPickerView))
        }
        AssignmentDetailsEvent.OnMediaPickingError -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowMediaPickingError))
        }
        is AssignmentDetailsEvent.SendAudioRecordingClicked -> {
            if(event.file == null) {
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.ShowAudioRecordingError))
            } else {
                val assignment = model.assignmentResult!!.dataOrThrow
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.UploadAudioSubmission(event.file, model.course, assignment)))
            }
        }
        is AssignmentDetailsEvent.SendVideoRecording -> {
            if (model.videoFileUri == null) {
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.ShowVideoRecordingError))
            } else {
                val assignment = model.assignmentResult!!.dataOrThrow
                Next.dispatch<AssignmentDetailsModel, AssignmentDetailsEffect>(setOf(AssignmentDetailsEffect.UploadVideoSubmission(model.videoFileUri, model.course, assignment)))
            }
        }
        is AssignmentDetailsEvent.SendMediaFile -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.UploadMediaFileSubmission(event.uri, model.course, model.assignmentResult!!.dataOrThrow)))
        }
        is AssignmentDetailsEvent.OnVideoRecordingError -> {
            Next.dispatch(setOf(AssignmentDetailsEffect.ShowVideoRecordingError))
        }
        is AssignmentDetailsEvent.SubmissionStatusUpdated -> {
            val newModel = model.copy(
                databaseSubmission = event.submission
            )
            // Null submission emitted to this event means that the submission was successful and was deleted, so we need to load
            if (event.submission == null) {
                Next.next<AssignmentDetailsModel, AssignmentDetailsEffect>(
                    newModel,
                    setOf(
                        AssignmentDetailsEffect.LoadData(
                            model.assignmentId,
                            model.course.id,
                            true
                        )
                    )
                )
            } else {
                Next.next(newModel)
            }
        }
        is AssignmentDetailsEvent.DataLoaded -> {
            val dbSubmission = dbSubmissionIfNewest(event.submission, event.assignmentResult?.dataOrNull?.submission)
            val newModel = model.copy(
                isLoading = false,
                assignmentResult = event.assignmentResult,
                isStudioEnabled = event.isStudioEnabled,
                studioLTIToolResult = event.studioLTIToolResult,
                ltiTool = event.ltiToolResult,
                quizResult = event.quizResult,
                databaseSubmission = dbSubmission,
                isObserver = event.isObserver
            )

            if (newModel.shouldRouteToSubmissionDetails) {
                Next.next<AssignmentDetailsModel, AssignmentDetailsEffect>(
                    newModel.copy(shouldRouteToSubmissionDetails = false), setOf(AssignmentDetailsEffect.ShowSubmissionView(model.assignmentId, model.course, model.isObserver))
                )
            } else {
                Next.next<AssignmentDetailsModel, AssignmentDetailsEffect>(newModel)
            }
        }
        is AssignmentDetailsEvent.InternalRouteRequested -> {
            val effect = AssignmentDetailsEffect.RouteInternally(
                url = event.url,
                course = model.course,
                assignment = model.assignmentResult!!.dataOrThrow
            )
            Next.dispatch(setOf(effect))
        }
        is AssignmentDetailsEvent.StoreVideoUri -> {
            Next.next(model.copy(videoFileUri = event.uri))
        }
        AssignmentDetailsEvent.AddBookmarkClicked -> Next.dispatch(setOf(AssignmentDetailsEffect.ShowBookmarkDialog))
    }

    private fun getSubmitClickedEffect(assignmentResult: DataResult<Assignment>, model: AssignmentDetailsModel, submissionTypes: List<Assignment.SubmissionType>) : Set<AssignmentDetailsEffect> {
        return setOf(when {
            assignmentResult.dataOrNull!!.turnInType == Assignment.TurnInType.QUIZ -> {
                AssignmentDetailsEffect.ShowQuizStartView(model.quizResult!!.dataOrThrow, model.course)
            }
            assignmentResult.dataOrNull!!.turnInType == Assignment.TurnInType.DISCUSSION -> {
                AssignmentDetailsEffect.ShowDiscussionDetailView(
                    assignmentResult.dataOrThrow.discussionTopicHeader!!.id,
                    model.course
                )
            }
            submissionTypes.size == 1 && !(submissionTypes.contains(Assignment.SubmissionType.ONLINE_UPLOAD) && model.isStudioEnabled) -> {
                AssignmentDetailsEffect.ShowCreateSubmissionView(
                    submissionTypes.first(),
                    model.course,
                    assignmentResult.dataOrThrow,
                    assignmentResult.dataOrThrow.url
                )
            }
            else -> {
                AssignmentDetailsEffect.ShowSubmitDialogView(
                    assignmentResult.dataOrThrow,
                    model.course,
                    model.isStudioEnabled,
                    model.studioLTIToolResult?.dataOrNull
                )
            }
        })
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
