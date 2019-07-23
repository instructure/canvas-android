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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.student.PendingSubmissionComment
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.mobius.common.DBSource
import com.instructure.student.mobius.common.ui.MobiusFragment

class SubmissionCommentsFragment :
        MobiusFragment<SubmissionCommentsModel, SubmissionCommentsEvent, SubmissionCommentsEffect, SubmissionCommentsView, SubmissionCommentsViewState>() {
    lateinit var data: SubmissionDetailsTabData.CommentData

    override fun makeEffectHandler() = SubmissionCommentsEffectHandler(requireContext())
    override fun makeUpdate() = SubmissionCommentsUpdate()
    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionCommentsView(inflater, parent)
    override fun makePresenter() = SubmissionCommentsPresenter

    override fun makeInitModel() = SubmissionCommentsModel(
        comments = data.submission.submissionComments,
        submissionHistory = data.submission.submissionHistory.filterNotNull(),
        assignment = data.assignment
    )

    override fun getExternalEventSources() = listOf(
        ChannelSource.getSource<SubmissionCommentsSharedEvent, SubmissionCommentsEvent> {
            when (it) {
                is SubmissionCommentsSharedEvent.SendMediaCommentClicked -> SubmissionCommentsEvent.SendMediaCommentClicked(it.file)
                is SubmissionCommentsSharedEvent.MediaCommentDialogClosed -> SubmissionCommentsEvent.MediaCommentDialogClosed
            }
        },
        ChannelSource.getSource<SubmissionComment, SubmissionCommentsEvent> {
            SubmissionCommentsEvent.SubmissionCommentAdded(it)
        },
        DBSource.ofList<PendingSubmissionComment, SubmissionCommentsEvent>(
            Db.getInstance(ContextKeeper.appContext)
                .pendingSubmissionCommentQueries
                .getCommentsByAccountAssignment(ApiPrefs.domain, data.assignment.id)
        ) { pendingComments ->
            val commentIds = pendingComments.map { it.id }
            SubmissionCommentsEvent.PendingSubmissionsUpdated(commentIds)
        }
    )
}
