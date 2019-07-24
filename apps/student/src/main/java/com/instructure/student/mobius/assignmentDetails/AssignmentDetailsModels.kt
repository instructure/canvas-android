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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.Submission

sealed class AssignmentDetailsEvent {
    object SubmitAssignmentClicked : AssignmentDetailsEvent()
    object ViewSubmissionClicked : AssignmentDetailsEvent()
    object ViewUploadStatusClicked : AssignmentDetailsEvent()
    object PullToRefresh : AssignmentDetailsEvent()
    data class SubmissionTypeClicked(val submissionType: Assignment.SubmissionType) : AssignmentDetailsEvent()
    data class DataLoaded(
        val assignmentResult: DataResult<Assignment>?,
        val isStudioEnabled: Boolean,
        val studioLTITool: DataResult<LTITool>?,
        val ltiTool: DataResult<LTITool>?,
        val submission: Submission?
    ) : AssignmentDetailsEvent()
    data class SubmissionStatusUpdated(val submission: Submission?) : AssignmentDetailsEvent()
    data class InternalRouteRequested(val url: String) : AssignmentDetailsEvent()
}

sealed class AssignmentDetailsEffect {
    data class ShowSubmitDialogView(val assignment: Assignment, val course: Course, val isStudioEnabled: Boolean, val studioLTITool: LTITool? = null) : AssignmentDetailsEffect()
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
    val isStudioEnabled: Boolean = false,
    val studioLTIToolResult: DataResult<LTITool>? = null,
    val ltiTool: DataResult<LTITool>? = null,
    val databaseSubmission: Submission? = null
)
