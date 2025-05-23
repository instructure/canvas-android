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

import android.os.Bundle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsRepository
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.instructure.student.mobius.common.FlowSource
import com.instructure.student.mobius.common.LiveDataSource
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_SUBMISSION_DETAILS)
@PageView(url = "{canvasContext}/assignments/{assignmentId}/submissions")
@AndroidEntryPoint
class SubmissionDetailsRepositoryFragment : SubmissionDetailsFragment() {

    @Inject
    lateinit var submissionDetailsRepository: SubmissionDetailsRepository

    @Inject
    lateinit var studentDb: StudentDb

    override fun getRepository() = submissionDetailsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun getExternalEventSources() = listOf(
        FlowSource.getSource<SubmissionDetailsSharedEvent, SubmissionDetailsEvent> {
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
        LiveDataSource.of<CreateSubmissionEntity, SubmissionDetailsEvent>(
            studentDb.submissionDao()
                .findSubmissionByAssignmentIdLiveData(assignmentId, ApiPrefs.user?.id ?: -1)
        ) { submission ->
            if (submission?.progress?.toDouble() == 100.0) {
                // A submission for this assignment was finished - we'll want to reload data
                SubmissionDetailsEvent.SubmissionUploadFinished
            } else {
                // Submission is either currently being uploaded, or there is no submission being uploaded - do nothing
                null
            }
        },
    )

    companion object {
        fun makeRoute(
            course: CanvasContext,
            assignmentId: Long,
            isObserver: Boolean = false,
            initialSelectedSubmissionAttempt: Long? = null
        ): Route {
            val bundle = course.makeBundle {
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putBoolean(Const.IS_OBSERVER, isObserver)
                initialSelectedSubmissionAttempt?.let { putLong(Const.SUBMISSION_ATTEMPT, it) }
            }
            return Route(null, SubmissionDetailsRepositoryFragment::class.java, course, bundle)
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course &&
                    (route.arguments.containsKey(Const.ASSIGNMENT_ID) ||
                            route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID))
        }

        fun newInstance(route: Route): SubmissionDetailsRepositoryFragment? {
            if (!validRoute(route)) return null

            // If routed from a URL, set the bundle's assignment ID from the url value
            if (route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID)) {
                val assignmentId = route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLong() ?: -1
                route.arguments.putLong(Const.ASSIGNMENT_ID, assignmentId)
            }

            if (route.paramsHash.containsKey(Const.SUBMISSION_ATTEMPT)) {
                val submissionAttempt = route.paramsHash[Const.SUBMISSION_ATTEMPT]?.toLong() ?: -1
                route.arguments.putLong(Const.SUBMISSION_ATTEMPT, submissionAttempt)
            }

            return SubmissionDetailsRepositoryFragment().withArgs(route.arguments)
        }
    }
}
