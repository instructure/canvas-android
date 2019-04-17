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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult

sealed class SubmissionDetailsEvent {
    object RefreshRequested : SubmissionDetailsEvent()
    data class SubmissionClicked(val submissionAttempt: Long, val attachmentId: Int = 0) : SubmissionDetailsEvent()
    data class DataLoaded(val assignment: DataResult<Assignment>, val rootSubmission: DataResult<Submission>) :
        SubmissionDetailsEvent()
}

sealed class SubmissionDetailsEffect {
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
    val rootSubmission: DataResult<Submission>? = null
)

sealed class SubmissionDetailsContentType {
    data class QuizContent(
        val courseId: Long,
        val assignmentId: Long,
        val studentId: Long,
        val url: String,
        val pendingReview: Boolean
    ) : SubmissionDetailsContentType()

    data class MediaContent(
        val uri: Uri,
        val contentType: String?,
        val thumbnailUrl: String?,
        val displayName: String?
    ) : SubmissionDetailsContentType()

    data class NoSubmissionContent(val canvasContext: CanvasContext, val assignment: Assignment) : SubmissionDetailsContentType()
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
