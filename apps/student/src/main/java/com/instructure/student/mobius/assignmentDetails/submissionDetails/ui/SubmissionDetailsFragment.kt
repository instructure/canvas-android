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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.Submission
import com.instructure.student.databinding.FragmentSubmissionDetailsBinding
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.submissionDetails.*
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.mobius.common.DBSource
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_DETAILS)
@PageView(url = "{canvasContext}/assignments/{assignmentId}/submissions")
abstract class SubmissionDetailsFragment : MobiusFragment<SubmissionDetailsModel, SubmissionDetailsEvent,
        SubmissionDetailsEffect, SubmissionDetailsView, SubmissionDetailsViewState, FragmentSubmissionDetailsBinding>() {

    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)

    @get:PageViewUrlParam(name = "assignmentId")
    val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    val isObserver by BooleanArg(key = Const.IS_OBSERVER, default = false)
    private val initialSelectedSubmissionAttempt by LongArg(key = Const.SUBMISSION_ATTEMPT)

    override fun makeEffectHandler() = SubmissionDetailsEffectHandler(getRepository())

    override fun makeUpdate() = SubmissionDetailsUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        SubmissionDetailsView(inflater, parent, canvasContext, childFragmentManager)

    override fun makePresenter() = SubmissionDetailsPresenter

    override fun makeInitModel() = SubmissionDetailsModel(
        canvasContext = canvasContext,
        assignmentId = assignmentId,
        isObserver = isObserver,
        initialSelectedSubmissionAttempt = initialSelectedSubmissionAttempt
    )

    override fun getExternalEventSources() = listOf(
        ChannelSource.getSource<SubmissionDetailsSharedEvent, SubmissionDetailsEvent> {
            when (it) {
                is SubmissionDetailsSharedEvent.FileSelected -> SubmissionDetailsEvent.AttachmentClicked(it.file)
                is SubmissionDetailsSharedEvent.AudioRecordingViewLaunched -> SubmissionDetailsEvent.AudioRecordingClicked
                is SubmissionDetailsSharedEvent.VideoRecordingViewLaunched -> SubmissionDetailsEvent.VideoRecordingClicked
                is SubmissionDetailsSharedEvent.SubmissionClicked -> {
                    SubmissionDetailsEvent.SubmissionClicked(it.submission.attempt)
                }
                is SubmissionDetailsSharedEvent.SubmissionAttachmentClicked -> {
                    SubmissionDetailsEvent.SubmissionAndAttachmentClicked(it.submission.attempt, it.attachment)
                }
                is SubmissionDetailsSharedEvent.SubmissionCommentsUpdated -> SubmissionDetailsEvent.SubmissionCommentsUpdated(it.submissionComments)
            }
        },
        DBSource.ofSingle<Submission, SubmissionDetailsEvent>(
            Db.getInstance(ContextKeeper.appContext)
                .submissionQueries
                .getSubmissionsByAssignmentId(assignmentId, ApiPrefs.user?.id ?: -1)
        ) { submission ->
            if (submission?.progress == 100.0) {
                // A submission for this assignment was finished - we'll want to reload data
                SubmissionDetailsEvent.SubmissionUploadFinished
            } else {
                // Submission is either currently being uploaded, or there is no submission being uploaded - do nothing
                null
            }
        }
    )

    abstract fun getRepository(): SubmissionDetailsRepository
}
