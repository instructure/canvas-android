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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui

import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.adapters.BasicItemBinder
import com.emeritus.student.R
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.views.CommentDirection
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.views.CommentSubmissionView
import kotlinx.android.synthetic.main.adapter_submission_comment.view.*

class SubmissionAsCommentBinder :
    BasicItemBinder<CommentItemState.SubmissionItem, SubmissionCommentsAdapterCallback>() {
    override val layoutResId = R.layout.adapter_submission_comment
    override val bindBehavior = Item { comment, callback, _ ->
        commentHolder.direction = CommentDirection.OUTGOING
        commentHolder.usernameText = Pronouns.span(comment.authorName, comment.authorPronouns)
        commentHolder.dateText = comment.dateText
        commentHolder.commentText = null
        commentHolder.setAvatar(comment.avatarUrl, comment.authorName)
        commentHolder.setExtraView(
            CommentSubmissionView(
                context = context,
                submission = comment.submission,
                tint = comment.tint,
                onSubmissionClicked = { submission -> callback.onSubmissionClicked(submission) },
                onAttachmentClicked = { submission, attachment ->
                    callback.onSubmissionAttachmentClicked(submission, attachment)
                }
            )
        )
    }
}
