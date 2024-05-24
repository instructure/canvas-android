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
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_COMMENTS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NLongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.student.databinding.FragmentSubmissionCommentsBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsUpdate
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsViewState
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_COMMENTS)
abstract class BaseSubmissionCommentsFragment :
        MobiusFragment<SubmissionCommentsModel, SubmissionCommentsEvent, SubmissionCommentsEffect, SubmissionCommentsView, SubmissionCommentsViewState, FragmentSubmissionCommentsBinding>() {

    protected var submission by ParcelableArg<Submission>(key = Const.SUBMISSION)
    protected var assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    protected var attemptId by NLongArg(key = Const.SUBMISSION_ATTEMPT)
    protected var assignmentEnhancementsEnabled by BooleanArg(key = Const.ASSIGNMENT_ENHANCEMENTS_ENABLED)

    override fun makeUpdate() = SubmissionCommentsUpdate()
    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionCommentsView(inflater, parent)


    override fun makeInitModel() = SubmissionCommentsModel(
        attemptId = attemptId,
        comments = submission.submissionComments,
        submissionHistory = submission.submissionHistory.filterNotNull(),
        assignment = assignment,
        assignmentEnhancementsEnabled = assignmentEnhancementsEnabled
    )
}
