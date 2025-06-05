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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.adapters.BasicItemCallback
import com.instructure.pandautils.adapters.BasicRecyclerAdapter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import com.instructure.pandautils.room.studentdb.StudentDb

interface SubmissionCommentsAdapterCallback : BasicItemCallback {
    fun onCommentAttachmentClicked(attachment: Attachment)
    fun onSubmissionClicked(submission: Submission)
    fun onSubmissionAttachmentClicked(submission: Submission, attachment: Attachment)
    fun onRetryPendingComment(pendingCommentId: Long)
    fun onDeletePendingComment(pendingCommentId: Long)
}

class SubmissionCommentsAdapter(callback: SubmissionCommentsAdapterCallback,
                                private val studentDb: StudentDb
) :
    BasicRecyclerAdapter<CommentItemState, SubmissionCommentsAdapterCallback>(callback) {
    override fun registerBinders() {
        register(SubmissionCommentsEmptyBinder())
        register(SubmissionCommentBinder())
        register(SubmissionAsCommentBinder())
        register(PendingCommentBinder(::getDb))
    }

    private fun getDb() = studentDb
}

