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
    object SubmissionUploadFinished : SubmissionDetailsEvent()
    data class VideoRecordingReplayClicked(val file: File?) : SubmissionDetailsEvent()
    data class SendMediaCommentClicked(val file: File?) : SubmissionDetailsEvent()
    data class AttachmentClicked(val file: Attachment) : SubmissionDetailsEvent()
    data class SubmissionClicked(val submissionAttempt: Long) : SubmissionDetailsEvent()
    data class SubmissionAndAttachmentClicked(val submissionAttempt: Long, val attachment: Attachment) : SubmissionDetailsEvent()
    data class DataLoaded(
        val assignment: DataResult<Assignment>,
        val rootSubmissionResult: DataResult<Submission>,
        val ltiUrlResult: DataResult<LTITool>?,
        val isStudioEnabled: Boolean,
        val quizResult: DataResult<Quiz>?,
        val studioLTIToolResult: DataResult<LTITool>?,
        val isObserver: Boolean = false,
        val assignmentEnhancementsEnabled: Boolean,
        val restrictQuantitativeData: Boolean = false
    ) : SubmissionDetailsEvent()
    data class SubmissionCommentsUpdated(val submissionComments: List<SubmissionComment>) : SubmissionDetailsEvent()
}

sealed class SubmissionDetailsEffect {
    object ShowAudioRecordingView : SubmissionDetailsEffect()
    object ShowVideoRecordingView : SubmissionDetailsEffect()
    object ShowVideoRecordingPlaybackError : SubmissionDetailsEffect()
    object ShowMediaCommentError : SubmissionDetailsEffect()
    object MediaCommentDialogClosed : SubmissionDetailsEffect()
    data class ShowVideoRecordingPlayback(val file: File) : SubmissionDetailsEffect()
    data class UploadMediaComment(val file: File) : SubmissionDetailsEffect()
    data class LoadData(val courseId: Long, val assignmentId: Long, val isObserver: Boolean = false) : SubmissionDetailsEffect()
    data class ShowSubmissionContentType(val submissionContentType: SubmissionDetailsContentType) :
        SubmissionDetailsEffect()
}

data class SubmissionDetailsModel(
    val isLoading: Boolean = false,
    val canvasContext: CanvasContext,
    val assignmentId: Long,
    val selectedSubmissionAttempt: Long? = null,
    val selectedAttachmentId: Long? = null,
    val assignmentResult: DataResult<Assignment>? = null,
    val rootSubmissionResult: DataResult<Submission>? = null,
    val isStudioEnabled: Boolean? = null,
    val quizResult: DataResult<Quiz>? = null,
    val studioLTIToolResult: DataResult<LTITool>? = null,
    val isObserver: Boolean = false,
    val ltiTool: DataResult<LTITool>? = null,
    val initialSelectedSubmissionAttempt: Long? = null,
    val submissionComments: List<SubmissionComment>? = null,
    val assignmentEnhancementsEnabled: Boolean = false,
    val restrictQuantitativeData: Boolean = false
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

    data class NoSubmissionContent(val canvasContext: CanvasContext, val assignment: Assignment, val isStudioEnabled: Boolean, val quiz: Quiz? = null, val studioLTITool: LTITool? = null, val isObserver: Boolean = false, val ltiTool: LTITool? = null) : SubmissionDetailsContentType()
    object NoneContent : SubmissionDetailsContentType()
    data class ExternalToolContent(val canvasContext: CanvasContext, val url: String, val title: String, val ltiType: LtiType = LtiType.EXTERNAL_TOOL) : SubmissionDetailsContentType()
    object OnPaperContent : SubmissionDetailsContentType()
    data class UnsupportedContent(val assignmentId: Long) : SubmissionDetailsContentType()
    data class OtherAttachmentContent(val attachment: Attachment) : SubmissionDetailsContentType()
    data class PdfContent(val url: String) : SubmissionDetailsContentType()
    data class TextContent(val text: String) : SubmissionDetailsContentType()
    data class ImageContent(val title: String, val url: String, val contentType: String) : SubmissionDetailsContentType()
    data class UrlContent(val url: String, val previewUrl: String?) : SubmissionDetailsContentType()
    data class DiscussionContent(val previewUrl: String?) : SubmissionDetailsContentType()
    object LockedContent : SubmissionDetailsContentType()
    data class StudentAnnotationContent(val subissionId: Long, val submissionAttempt: Long) : SubmissionDetailsContentType()
}
