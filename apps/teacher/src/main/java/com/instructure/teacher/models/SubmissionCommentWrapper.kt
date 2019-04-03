/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.models

import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import java.util.*

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
