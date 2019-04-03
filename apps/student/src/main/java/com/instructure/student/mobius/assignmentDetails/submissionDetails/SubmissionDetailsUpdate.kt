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
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.student.mobius.common.ui.UpdateInit
import com.instructure.student.util.Const
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SubmissionDetailsUpdate : UpdateInit<SubmissionDetailsModel, SubmissionDetailsEvent, SubmissionDetailsEffect>() {
    override fun performInit(model: SubmissionDetailsModel): First<SubmissionDetailsModel, SubmissionDetailsEffect> {
        return First.first(
                model.copy(
                        isLoading = true
                ),
                setOf(SubmissionDetailsEffect.LoadData(model.canvasContext.id, model.assignmentId))
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
                if (event.submissionAttemptId == model.selectedSubmissionAttemptId) {
                    Next.noChange<SubmissionDetailsModel, SubmissionDetailsEffect>()
                } else {
                    val submissionType = getSubmissionContentType(
                        model.rootSubmission?.dataOrNull?.submissionHistory?.find { it?.id == event.submissionAttemptId },
                        model.assignment?.dataOrNull,
                        model.canvasContext,
                        model.assignmentId
                    )
                    Next.next<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        model.copy(
                            selectedSubmissionAttemptId = event.submissionAttemptId
                        ), setOf(SubmissionDetailsEffect.ShowSubmissionContentType(submissionType))
                    )
                }
            }
            is SubmissionDetailsEvent.DataLoaded -> {
                val submissionType = getSubmissionContentType(
                        event.rootSubmission.dataOrNull,
                        event.assignment.dataOrNull,
                        model.canvasContext,
                        model.assignmentId)
                Next.next(
                        model.copy(
                                isLoading = false,
                                assignment = event.assignment,
                                rootSubmission = event.rootSubmission,
                                selectedSubmissionAttemptId = event.rootSubmission.dataOrNull?.attempt
                        ), setOf(SubmissionDetailsEffect.ShowSubmissionContentType(submissionType))
                )
            }
        }
    }

    private fun getSubmissionContentType(submission: Submission?, assignment: Assignment?, canvasContext: CanvasContext, assignmentId: Long): SubmissionDetailsContentType {
        return when {
            Assignment.SubmissionType.NONE.apiString in assignment?.submissionTypesRaw ?: emptyList() -> SubmissionDetailsContentType.NoneContent
            Assignment.SubmissionType.ON_PAPER.apiString in assignment?.submissionTypesRaw ?: emptyList() -> SubmissionDetailsContentType.OnPaperContent
            submission?.submissionType == null -> SubmissionDetailsContentType.NoSubmissionContent
            AssignmentUtils2.getAssignmentState(assignment, submission) in listOf(AssignmentUtils2.ASSIGNMENT_STATE_MISSING, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING) -> SubmissionDetailsContentType.NoSubmissionContent
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
                } ?: SubmissionDetailsContentType.UnsupportedContent

                // File uploads
                Assignment.SubmissionType.ONLINE_UPLOAD -> getAttachmentContent(submission.attachments[0])

                // URL Submission
                Assignment.SubmissionType.ONLINE_URL -> SubmissionDetailsContentType.UrlContent(submission.url!!, submission.attachments.firstOrNull()?.url)

                // Quiz Submission
                Assignment.SubmissionType.ONLINE_QUIZ -> SubmissionDetailsContentType.QuizContent(
                        canvasContext.id,
                        assignmentId,
                        submission.userId,
                        submission.previewUrl ?: "",
                        QuizSubmission.parseWorkflowState(submission.workflowState!!) == QuizSubmission.WorkflowState.PENDING_REVIEW
                )

                // Discussion Submission
                Assignment.SubmissionType.DISCUSSION_TOPIC -> SubmissionDetailsContentType.DiscussionContent(submission.previewUrl)
                else -> SubmissionDetailsContentType.UnsupportedContent
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
            type.startsWith("image") -> SubmissionDetailsContentType.ImageContent(attachment.url ?: "", attachment.contentType!!)
            else -> SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        }
    }
}
