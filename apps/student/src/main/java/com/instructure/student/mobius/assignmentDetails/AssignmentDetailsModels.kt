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
package com.instructure.student.mobius.assignmentDetails

import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.Submission

sealed class AssignmentDetailsEvent {
    object SubmitAssignmentClicked : AssignmentDetailsEvent()
    object ViewSubmissionClicked : AssignmentDetailsEvent()
    object ViewUploadStatusClicked : AssignmentDetailsEvent()
    object DiscussionAttachmentClicked : AssignmentDetailsEvent()
    object PullToRefresh : AssignmentDetailsEvent()
    data class SubmissionTypeClicked(val submissionType: Assignment.SubmissionType) : AssignmentDetailsEvent()
    data class DataLoaded(
        val assignmentResult: DataResult<Assignment>?,
        val isArcEnabled: Boolean,
        val ltiTool: DataResult<LTITool>?,
        val submission: Submission?,
        val quizResult: DataResult<Quiz>?
    ) : AssignmentDetailsEvent()
    data class SubmissionStatusUpdated(val submission: Submission?) : AssignmentDetailsEvent()
    data class InternalRouteRequested(val url: String) : AssignmentDetailsEvent()
}

sealed class AssignmentDetailsEffect {
    data class ShowSubmitDialogView(val assignment: Assignment, val course: Course, val isArcEnabled: Boolean) : AssignmentDetailsEffect()
    data class ShowQuizStartView(val quiz: Quiz, val course: Course) : AssignmentDetailsEffect()
    data class ShowDiscussionDetailView(val discussionTopicHeaderId: Long, val course: Course) : AssignmentDetailsEffect()
    data class ShowDiscussionAttachment(val discussionAttachment: Attachment, val course: Course) : AssignmentDetailsEffect()
    data class ShowSubmissionView(val assignmentId: Long, val course: Course) : AssignmentDetailsEffect()
    data class ShowUploadStatusView(val submission: Submission) : AssignmentDetailsEffect()
    data class ShowCreateSubmissionView(val submissionType: Assignment.SubmissionType, val course: Course, val assignment: Assignment, val ltiUrl: String? = null) : AssignmentDetailsEffect()
    data class LoadData(val assignmentId: Long, val courseId: Long, val forceNetwork: Boolean) : AssignmentDetailsEffect()
    data class RouteInternally(
        val url: String,
        val course: Course,
        val assignment: Assignment
    ) : AssignmentDetailsEffect()
}

data class AssignmentDetailsModel(
    val assignmentId: Long,
    val course: Course, // Will always pull from cache for the course
    val isLoading: Boolean = false,
    val assignmentResult: DataResult<Assignment>? = null,
    val quizResult: DataResult<Quiz>? = null,
    val isArcEnabled: Boolean = false,
    val ltiTool: DataResult<LTITool>? = null,
    val databaseSubmission: Submission? = null
)
