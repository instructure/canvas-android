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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.canvasapi2.utils.DataResult
import java.util.*

sealed class SubmissionCommentsEvent {
    // User Events
    object AddMediaCommentClicked : SubmissionCommentsEvent()
    object AddAudioCommentClicked : SubmissionCommentsEvent()
    object AddVideoCommentClicked : SubmissionCommentsEvent()
    data class MediaCommentClicked(val comment: SubmissionComment) : SubmissionCommentsEvent()
    data class AttachmentClicked(val attachment: Attachment) : SubmissionCommentsEvent()
    data class SubmissionClicked(val submission: Submission) : SubmissionCommentsEvent()
    data class SubmissionFileClicked(val submissionAttachment: Attachment, val submission: Submission) : SubmissionCommentsEvent()
    data class AddTextComment(val commentText: String) : SubmissionCommentsEvent()
    data class retryAddComment(val pendingComment: PendingCommentWrapper) : SubmissionCommentsEvent()

    // External Events
    data class DataLoaded(val submissionComments: DataResult<List<SubmissionCommentWrapper>>, val draftComment: String?) : SubmissionCommentsEvent()
    data class pendingCommentUpdated(val pendingComment: PendingCommentWrapper) : SubmissionCommentsEvent()
}

sealed class SubmissionCommentsEffect {
    data class ShowMediaComment(val comment: SubmissionComment) : SubmissionCommentsEffect()
    data class ShowAttachment(val attachment: Attachment) : SubmissionCommentsEffect()
    data class ShowSubmission(val submission: Submission) : SubmissionCommentsEffect()
    data class ShowSubmissionFile(val submissionAttachment: Attachment, val submission: Submission) : SubmissionCommentsEffect()
    data class UploadComment(val pendingComment: PendingCommentWrapper, val courseId: Long, val assignmentId: Long, val groupMessage: Boolean) : SubmissionCommentsEffect() // todo do we need a separate effect for media?

    data class LoadData(val courseId: Long, val assignmentId: Long, val forceNetwork: Boolean) : SubmissionCommentsEffect()
}

data class SubmissionCommentsModel(
    val courseId: Long,
    val assignmentId: Long,
    val isLoading: Boolean = false,
    val assignment: DataResult<Assignment>? = null,
    val submissionComments: DataResult<List<SubmissionCommentWrapper>>? = null
)

// region Submission Comment Wrapper

sealed class SubmissionCommentWrapper {
    abstract val id: Long
    abstract val date: Date
}

class CommentWrapper(val comment: SubmissionComment) : SubmissionCommentWrapper() {
    override val date: Date get() = comment.createdAt ?: Date(0)
    override val id: Long get() = comment.id
}

class PendingCommentWrapper(val pendingComment: PendingSubmissionComment) : SubmissionCommentWrapper() {
    override val id: Long get() = pendingComment.id
    override val date: Date get() = pendingComment.date
}

class SubmissionWrapper(val submission: Submission) : SubmissionCommentWrapper() {
    override val id: Long get() = submission.hashCode().toLong()
    override val date: Date get() = submission.submittedAt ?: Date(0)
}
// endregion
