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
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import java.io.File

sealed class SubmissionDetailsEvent {
    object RefreshRequested : SubmissionDetailsEvent()
    object AudioRecordingClicked : SubmissionDetailsEvent()
    object VideoRecordingClicked : SubmissionDetailsEvent()
    object StopMediaRecordingClicked : SubmissionDetailsEvent()
    data class VideoRecordingReplayClicked(val file: File?) : SubmissionDetailsEvent()
    data class SendMediaCommentClicked(val file: File?) : SubmissionDetailsEvent()
    data class AttachmentClicked(val file: Attachment) : SubmissionDetailsEvent()
    data class SubmissionClicked(val submissionAttempt: Long) : SubmissionDetailsEvent()
    data class SubmissionAndAttachmentClicked(val submissionAttempt: Long, val attachment: Attachment) : SubmissionDetailsEvent()
    data class DataLoaded(val assignment: DataResult<Assignment>, val rootSubmission: DataResult<Submission>, val ltiUrl: DataResult<LTITool?>, val isArcEnabled: Boolean) :
        SubmissionDetailsEvent()
}

sealed class SubmissionDetailsEffect {
    object ShowAudioRecordingView : SubmissionDetailsEffect()
    object ShowVideoRecordingView : SubmissionDetailsEffect()
    object ShowVideoRecordingPlaybackError : SubmissionDetailsEffect()
    object ShowMediaCommentError : SubmissionDetailsEffect()
    object MediaCommentDialogClosed : SubmissionDetailsEffect()
    data class ShowVideoRecordingPlayback(val file: File) : SubmissionDetailsEffect()
    data class UploadMediaComment(val file: File) : SubmissionDetailsEffect()
    data class LoadData(val courseId: Long, val assignmentId: Long) : SubmissionDetailsEffect()
    data class ShowSubmissionContentType(val submissionContentType: SubmissionDetailsContentType) :
        SubmissionDetailsEffect()
}

data class SubmissionDetailsModel(
    val isLoading: Boolean = false,
    val canvasContext: CanvasContext,
    val assignmentId: Long,
    val selectedSubmissionAttempt: Long? = null,
    val selectedAttachmentId: Long? = null,
    val assignment: DataResult<Assignment>? = null,
    val rootSubmission: DataResult<Submission>? = null,
    val isArcEnabled: Boolean? = null
)

sealed class SubmissionDetailsContentType {
    data class QuizContent(
        val url: String
    ) : SubmissionDetailsContentType()

    data class MediaContent(
        val uri: Uri,
        val contentType: String?,
        val thumbnailUrl: String?,
        val displayName: String?
    ) : SubmissionDetailsContentType()

    data class NoSubmissionContent(val canvasContext: CanvasContext, val assignment: Assignment, val isArcEnabled: Boolean) : SubmissionDetailsContentType()
    object NoneContent : SubmissionDetailsContentType()
    data class ExternalToolContent(val canvasContext: CanvasContext, val url: String) : SubmissionDetailsContentType()
    object OnPaperContent : SubmissionDetailsContentType()
    object UnsupportedContent : SubmissionDetailsContentType()
    data class OtherAttachmentContent(val attachment: Attachment) : SubmissionDetailsContentType()
    data class PdfContent(val url: String) : SubmissionDetailsContentType()
    data class TextContent(val text: String) : SubmissionDetailsContentType()
    data class ImageContent(val url: String, val contentType: String) : SubmissionDetailsContentType()
    data class UrlContent(val url: String, val previewUrl: String?) : SubmissionDetailsContentType()
    data class DiscussionContent(val previewUrl: String?) : SubmissionDetailsContentType()
}
