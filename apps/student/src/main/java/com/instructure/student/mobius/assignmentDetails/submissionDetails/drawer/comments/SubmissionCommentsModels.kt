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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments

import com.instructure.canvasapi2.models.*
import java.io.File

sealed class SubmissionCommentsEvent {
    object AddFilesClicked : SubmissionCommentsEvent()
    object AddAudioCommentClicked : SubmissionCommentsEvent()
    object AddVideoCommentClicked : SubmissionCommentsEvent()
    object AddFilesDialogClosed : SubmissionCommentsEvent()
    object UploadFilesClicked : SubmissionCommentsEvent()
    data class SendTextCommentClicked(val message: String) : SubmissionCommentsEvent()
    data class SendMediaCommentClicked(val file: File) : SubmissionCommentsEvent()
    data class SubmissionCommentAdded(val comment: SubmissionComment) : SubmissionCommentsEvent()
    data class PendingSubmissionsUpdated(val ids: List<Long>) : SubmissionCommentsEvent()
    data class RetryCommentUploadClicked(val commentId: Long) : SubmissionCommentsEvent()
    data class DeletePendingCommentClicked(val commentId: Long) : SubmissionCommentsEvent()
    data class SubmissionClicked(val submission: Submission) : SubmissionCommentsEvent()
    data class SubmissionAttachmentClicked(
        val submission: Submission,
        val attachment: Attachment
    ) : SubmissionCommentsEvent()
    data class CommentAttachmentClicked(val attachment: Attachment) : SubmissionCommentsEvent()
}

sealed class SubmissionCommentsEffect {
    object ShowAudioRecordingView : SubmissionCommentsEffect()
    object ShowVideoRecordingView : SubmissionCommentsEffect()
    object ShowMediaCommentDialog : SubmissionCommentsEffect()
    object ClearTextInput : SubmissionCommentsEffect()
    object ScrollToBottom : SubmissionCommentsEffect()
    data class SendTextComment(
        val message: String,
        val assignmentId: Long,
        val assignmentName: String,
        val courseId: Long,
        val isGroupMessage: Boolean,
        val attemptId: Long?
    ) : SubmissionCommentsEffect()

    data class ShowFilePicker(
        val canvasContext: CanvasContext,
        val assignment: Assignment,
        val attemptId: Long?
    ) : SubmissionCommentsEffect()

    data class UploadMediaComment constructor(
        val file: File,
        val assignmentId: Long,
        val assignmentName: String,
        val courseId: Long,
        val isGroupMessage: Boolean,
        val attemptId: Long?
    ) : SubmissionCommentsEffect()

    data class RetryCommentUpload(val commentId: Long) : SubmissionCommentsEffect()

    data class DeletePendingComment(val commentId: Long) : SubmissionCommentsEffect()

    data class BroadcastSubmissionSelected(val submission: Submission) : SubmissionCommentsEffect()

    data class BroadcastSubmissionAttachmentSelected(
        val submission: Submission,
        val attachment: Attachment
    ) : SubmissionCommentsEffect()

    data class OpenMedia(
        val canvasContext: CanvasContext,
        val contentType: String,
        val url: String,
        val fileName: String
    ) : SubmissionCommentsEffect()
}

data class SubmissionCommentsModel(
    val attemptId: Long?,
    val comments: List<SubmissionComment>,
    val submissionHistory: List<Submission>,
    val assignment: Assignment,
    val pendingCommentIds: List<Long> = emptyList(),
    val isFileButtonEnabled: Boolean = true,
    val showSendButton: Boolean = false,
    val assignmentEnhancementsEnabled: Boolean
)
