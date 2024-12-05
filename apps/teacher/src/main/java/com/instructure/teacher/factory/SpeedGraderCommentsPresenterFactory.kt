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
package com.instructure.teacher.factory

import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.pandautils.room.appdatabase.daos.*
import com.instructure.teacher.presenters.SpeedGraderCommentsPresenter
import com.instructure.teacher.viewinterface.SpeedGraderCommentsView
import com.instructure.pandautils.blueprint.PresenterFactory

class SpeedGraderCommentsPresenterFactory(
    val rawComments: java.util.ArrayList<SubmissionComment>,
    val submissionHistory: List<Submission>,
    val assignee: Assignee,
    val courseId: Long,
    val assignmentId: Long,
    val groupMessage: Boolean,
    val submissionCommentDao: SubmissionCommentDao,
    val attachmentDao: AttachmentDao,
    val authorDao: AuthorDao,
    val mediaCommentDao: MediaCommentDao,
    val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    val fileUploadInputDao: FileUploadInputDao,
    val selectedAttemptId: Long,
    val assignmentEnhancementsEnabled: Boolean
) : PresenterFactory<SpeedGraderCommentsView, SpeedGraderCommentsPresenter> {
    override fun create() = SpeedGraderCommentsPresenter(
        rawComments,
        submissionHistory,
        assignee,
        courseId,
        assignmentId,
        groupMessage,
        submissionCommentDao,
        attachmentDao,
        authorDao,
        mediaCommentDao,
        pendingSubmissionCommentDao,
        fileUploadInputDao,
        selectedAttemptId,
        assignmentEnhancementsEnabled
    )
}
