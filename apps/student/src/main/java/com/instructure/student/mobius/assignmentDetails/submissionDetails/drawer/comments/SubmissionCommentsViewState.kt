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

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.room.studentdb.entities.CreatePendingSubmissionCommentEntity
import java.util.Date

data class SubmissionCommentsViewState(
    val enableFilesButton: Boolean = true,
    val commentStates: List<CommentItemState> = listOf(CommentItemState.Empty)
)

sealed class CommentItemState {
    object Empty : CommentItemState()

    data class CommentItem(
        val id: Long,
        val authorName: String,
        val authorPronouns: String?,
        val avatarUrl: String,
        val sortDate: Date,
        val dateText: String,
        val message: String,
        val isAudience: Boolean,
        val media: MediaComment? = null,
        val attachments: List<Attachment> = emptyList(),
        val tint: Int
    ) : CommentItemState()

    data class PendingCommentItem(
        val authorName: String,
        val authorPronouns: String?,
        val avatarUrl: String,
        val sortDate: Date,
        val pendingComment: CreatePendingSubmissionCommentEntity
    ) : CommentItemState()

    data class SubmissionItem(
        val authorName: String,
        val authorPronouns: String?,
        val avatarUrl: String,
        val sortDate: Date,
        val dateText: String,
        val submission: Submission,
        val tint: Int
    ) : CommentItemState()
}
