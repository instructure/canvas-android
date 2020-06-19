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

package com.instructure.student.mobius.assignmentDetails.submissionDetails

import android.net.Uri
import android.webkit.MimeTypeMap
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.student.mobius.common.ui.UpdateInit
import com.instructure.student.util.Const
import com.spotify.mobius.First
import com.spotify.mobius.Next
import java.util.Collections.emptyList

class SubmissionDetailsUpdate : UpdateInit<SubmissionDetailsModel, SubmissionDetailsEvent, SubmissionDetailsEffect>() {
    override fun performInit(model: SubmissionDetailsModel): First<SubmissionDetailsModel, SubmissionDetailsEffect> {
        return First.first(
            model.copy(
                isLoading = true
            ),
            setOf(SubmissionDetailsEffect.LoadData(model.canvasContext.id, model.assignmentId, model.isObserver))
        )
    }

    override fun update(model: SubmissionDetailsModel, event: SubmissionDetailsEvent): Next<SubmissionDetailsModel, SubmissionDetailsEffect> {
        return when (event) {
            SubmissionDetailsEvent.RefreshRequested -> {
                Next.next(
                    model.copy(isLoading = true),
                    setOf(SubmissionDetailsEffect.LoadData(model.canvasContext.id, model.assignmentId)))
            }
            is SubmissionDetailsEvent.SubmissionClicked -> {
                if (event.submissionAttempt == model.selectedSubmissionAttempt) {
                    Next.noChange<SubmissionDetailsModel, SubmissionDetailsEffect>()
                } else {
                    val submissionType = getSubmissionContentType(
                        model.rootSubmissionResult?.dataOrNull?.submissionHistory?.find { it?.attempt == event.submissionAttempt },
                        model.assignmentResult?.dataOrNull,
                        model.canvasContext,
                        model.isStudioEnabled,
                        null,
                        quiz = model.quizResult?.dataOrNull,
                        studioLTITool = model.studioLTIToolResult?.dataOrNull,
                        isObserver = model.isObserver
                    )
                    Next.next<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        model.copy(
                            selectedSubmissionAttempt = event.submissionAttempt
                        ), setOf(SubmissionDetailsEffect.ShowSubmissionContentType(submissionType))
                    )
                }
            }
            is SubmissionDetailsEvent.DataLoaded -> {
                val submissionType = getSubmissionContentType(
                    event.rootSubmissionResult.dataOrNull,
                    event.assignment.dataOrNull,
                    model.canvasContext,
                    event.isStudioEnabled,
                    event.ltiUrlResult.dataOrNull,
                    event.quizResult?.dataOrNull,
                    event.studioLTIToolResult?.dataOrNull,
                    event.isObserver
                )
                Next.next(
                    model.copy(
                        isLoading = false,
                        assignmentResult = event.assignment,
                        rootSubmissionResult = event.rootSubmissionResult,
                        selectedSubmissionAttempt = event.rootSubmissionResult.dataOrNull?.attempt,
                        quizResult = event.quizResult
                    ), setOf(SubmissionDetailsEffect.ShowSubmissionContentType(submissionType))
                )
            }
            is SubmissionDetailsEvent.AttachmentClicked -> {
                if (model.selectedAttachmentId == event.file.id) {
                    Next.noChange()
                } else {
                    val content = getAttachmentContent(event.file)
                    Next.next<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        model.copy(selectedAttachmentId = event.file.id),
                        setOf(SubmissionDetailsEffect.ShowSubmissionContentType(content))
                    )
                }
            }
            is SubmissionDetailsEvent.SubmissionAndAttachmentClicked -> {
                if (event.submissionAttempt == model.selectedSubmissionAttempt
                    && model.selectedAttachmentId == event.attachment.id) {
                    Next.noChange<SubmissionDetailsModel, SubmissionDetailsEffect>()
                } else {
                    val content = getAttachmentContent(event.attachment)
                    Next.next<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        model.copy(
                            selectedSubmissionAttempt = event.submissionAttempt,
                            selectedAttachmentId = event.attachment.id
                        ),
                        setOf(
                            SubmissionDetailsEffect.ShowSubmissionContentType(content)
                        )
                    )
                }
            }
            is SubmissionDetailsEvent.AudioRecordingClicked -> {
                Next.dispatch(setOf(SubmissionDetailsEffect.ShowAudioRecordingView))
            }
            is SubmissionDetailsEvent.VideoRecordingClicked -> {
                Next.dispatch(setOf(SubmissionDetailsEffect.ShowVideoRecordingView))
            }
            is SubmissionDetailsEvent.VideoRecordingReplayClicked -> {
                if(event.file != null) {
                    Next.dispatch<SubmissionDetailsModel, SubmissionDetailsEffect>(setOf(SubmissionDetailsEffect.ShowVideoRecordingPlayback(event.file)))
                } else {
                    Next.dispatch<SubmissionDetailsModel, SubmissionDetailsEffect>(setOf(SubmissionDetailsEffect.ShowVideoRecordingPlaybackError))
                }
            }
            is SubmissionDetailsEvent.StopMediaRecordingClicked -> {
                Next.dispatch(setOf(SubmissionDetailsEffect.MediaCommentDialogClosed))
            }
            is SubmissionDetailsEvent.SendMediaCommentClicked -> {
                if(event.file != null) {
                    Next.dispatch<SubmissionDetailsModel, SubmissionDetailsEffect>(setOf(SubmissionDetailsEffect.UploadMediaComment(event.file)))
                } else {
                    Next.dispatch<SubmissionDetailsModel, SubmissionDetailsEffect>(setOf(SubmissionDetailsEffect.ShowMediaCommentError))
                }
            }
            SubmissionDetailsEvent.SubmissionUploadFinished ->  Next.next(
                model.copy(isLoading = true),
                setOf(SubmissionDetailsEffect.LoadData(model.canvasContext.id, model.assignmentId)))
        }
    }

    private fun getSubmissionContentType(
        submission: Submission?,
        assignment: Assignment?,
        canvasContext: CanvasContext,
        isStudioEnabled: Boolean?,
        ltiUrl: LTITool?,
        quiz: Quiz?,
        studioLTITool: LTITool?,
        isObserver: Boolean = false
    ): SubmissionDetailsContentType {
        return when {
            Assignment.SubmissionType.NONE.apiString in assignment?.submissionTypesRaw ?: emptyList() -> SubmissionDetailsContentType.NoneContent
            Assignment.SubmissionType.ON_PAPER.apiString in assignment?.submissionTypesRaw ?: emptyList() -> SubmissionDetailsContentType.OnPaperContent
            Assignment.SubmissionType.EXTERNAL_TOOL.apiString in assignment?.submissionTypesRaw ?: emptyList() -> {
                if (assignment?.isAllowedToSubmit == true)
                    SubmissionDetailsContentType.ExternalToolContent(canvasContext, ltiUrl?.url ?: "")
                else SubmissionDetailsContentType.LockedContent
            }
            submission?.submissionType == null -> SubmissionDetailsContentType.NoSubmissionContent(canvasContext, assignment!!, isStudioEnabled!!, quiz, studioLTITool, isObserver)
            submission.workflowState != "submitted" && AssignmentUtils2.getAssignmentState(assignment, submission) in listOf(AssignmentUtils2.ASSIGNMENT_STATE_MISSING, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING) -> SubmissionDetailsContentType.NoSubmissionContent(canvasContext, assignment!!, isStudioEnabled!!, quiz)
            else -> when (Assignment.getSubmissionTypeFromAPIString(submission.submissionType)) {

                // LTI submission
                Assignment.SubmissionType.BASIC_LTI_LAUNCH -> SubmissionDetailsContentType.ExternalToolContent(
                        canvasContext,
                        submission.previewUrl.validOrNull() ?: assignment?.url?.validOrNull() ?: assignment?.htmlUrl ?: ""
                )

                // Text submission
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> SubmissionDetailsContentType.TextContent(submission.body ?: "")

                // Media submission
                Assignment.SubmissionType.MEDIA_RECORDING -> submission.mediaComment?.let {
                    SubmissionDetailsContentType.MediaContent(
                            uri = Uri.parse(it.url),
                            contentType = it.contentType ?: "",
                            displayName = it.displayName,
                            thumbnailUrl = null
                    )
                } ?: SubmissionDetailsContentType.UnsupportedContent(assignment?.id ?: -1)

                // File uploads
                Assignment.SubmissionType.ONLINE_UPLOAD -> getAttachmentContent(submission.attachments[0])

                // URL Submission
                Assignment.SubmissionType.ONLINE_URL -> SubmissionDetailsContentType.UrlContent(submission.url!!, submission.attachments.firstOrNull()?.url)

                // Quiz Submission
                Assignment.SubmissionType.ONLINE_QUIZ -> SubmissionDetailsContentType.QuizContent(
                    ApiPrefs.fullDomain + "/courses/${canvasContext.id}/quizzes/${assignment!!.quizId}/history?version=${submission.attempt}&headless=1"
                )

                // Discussion Submission
                Assignment.SubmissionType.DISCUSSION_TOPIC -> SubmissionDetailsContentType.DiscussionContent(submission.previewUrl)
                else -> SubmissionDetailsContentType.UnsupportedContent(assignment?.id ?: -1)
            }
        }
    }

    private fun getAttachmentContent(attachment: Attachment): SubmissionDetailsContentType {
        var type = attachment.contentType ?: return SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        if (type == "*/*") {
            val fileExtension = attachment.filename?.substringAfterLast(".") ?: ""
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
                    ?: MimeTypeMap.getFileExtensionFromUrl(attachment.url)
                            ?: type
        }
        return when {
            type == "application/pdf" || (attachment.previewUrl?.contains(Const.CANVADOC) ?: false) -> {
                if (attachment.previewUrl?.contains(Const.CANVADOC) == true) {
                    SubmissionDetailsContentType.PdfContent(attachment.previewUrl!!)
                } else {
                    SubmissionDetailsContentType.PdfContent(attachment.url ?: "")
                }
            }
            type.startsWith("audio") || type.startsWith("video") -> with(attachment) {
                SubmissionDetailsContentType.MediaContent(
                    uri = Uri.parse(url),
                    thumbnailUrl = thumbnailUrl,
                    contentType = contentType,
                    displayName = displayName
                )
            }
            type.startsWith("image") -> SubmissionDetailsContentType.ImageContent(
                title = attachment.displayName ?: attachment.filename ?: "",
                url = attachment.url ?: "",
                contentType = attachment.contentType!!
            )
            else -> SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        }
    }
}
