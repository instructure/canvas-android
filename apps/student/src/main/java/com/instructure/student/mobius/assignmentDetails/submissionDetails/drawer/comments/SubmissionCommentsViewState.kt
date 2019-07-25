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

data class SubmissionCommentsViewState(
    val enableMediaButton: Boolean = true,
    val enableCommentInput: Boolean = true,
    val showSendButton: Boolean = false,
    val showProgressIndicator: Boolean = false,
    val commentStates: List<CommentItemState> = listOf(CommentItemState.Empty)
)

sealed class CommentItemState {
    object Empty : CommentItemState()

    data class CommentItem(
        val avatarUrl: String,
        val avatarInitials: String,
        val comment: String,
        val isAudience: Boolean
    ) : CommentItemState()

    data class PendingCommentItem(
        val avatarUrl: String,
        val avatarInitials: String,
        val comment: String,
        val status: String
    )

    data class SubmissionItem(
        val name: String
    ) : CommentItemState()
}
