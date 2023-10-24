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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_COMMENTS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NLongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.student.PendingSubmissionComment
import com.instructure.student.databinding.FragmentSubmissionCommentsBinding
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.mobius.common.DBSource
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_COMMENTS)
class SubmissionCommentsFragment :
        MobiusFragment<SubmissionCommentsModel, SubmissionCommentsEvent, SubmissionCommentsEffect, SubmissionCommentsView, SubmissionCommentsViewState, FragmentSubmissionCommentsBinding>() {

    private var submission by ParcelableArg<Submission>(key = Const.SUBMISSION)
    private var assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    private var attemptId by NLongArg(key = Const.SUBMISSION_ATTEMPT)
    private var assignmentEnhancementsEnabled by BooleanArg(key = Const.ASSIGNMENT_ENHANCEMENTS_ENABLED)

    override fun makeEffectHandler() = SubmissionCommentsEffectHandler(requireContext())
    override fun makeUpdate() = SubmissionCommentsUpdate()
    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionCommentsView(inflater, parent)
    override fun makePresenter() = SubmissionCommentsPresenter

    override fun makeInitModel() = SubmissionCommentsModel(
        attemptId = attemptId,
        comments = submission.submissionComments,
        submissionHistory = submission.submissionHistory.filterNotNull(),
        assignment = assignment,
        assignmentEnhancementsEnabled = assignmentEnhancementsEnabled
    )

    override fun getExternalEventSources() = listOf(
        ChannelSource.getSource<SubmissionCommentsSharedEvent, SubmissionCommentsEvent> {
            when (it) {
                is SubmissionCommentsSharedEvent.SendMediaCommentClicked -> SubmissionCommentsEvent.SendMediaCommentClicked(it.file)
                is SubmissionCommentsSharedEvent.MediaCommentDialogClosed -> SubmissionCommentsEvent.AddFilesDialogClosed
            }
        },
        ChannelSource.getSource<SubmissionComment, SubmissionCommentsEvent> {
            SubmissionCommentsEvent.SubmissionCommentAdded(it)
        },
        DBSource.ofList<PendingSubmissionComment, SubmissionCommentsEvent>(
            Db.getInstance(ContextKeeper.appContext)
                .pendingSubmissionCommentQueries
                .getCommentsByAccountAssignment(ApiPrefs.domain, assignment.id)
        ) { pendingComments ->
            val commentIds = pendingComments.map { it.id }
            SubmissionCommentsEvent.PendingSubmissionsUpdated(commentIds)
        }
    )

    companion object {
        fun newInstance(data: SubmissionDetailsTabData.CommentData) = SubmissionCommentsFragment().apply {
            submission = data.submission
            assignment = data.assignment
            attemptId = data.attemptId
            assignmentEnhancementsEnabled = data.assignmentEnhancementsEnabled
        }
    }
}
