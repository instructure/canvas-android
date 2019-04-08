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
import com.instructure.canvasapi2.utils.DataResult

sealed class AssignmentDetailsEvent {
    object SubmitAssignmentClicked : AssignmentDetailsEvent()
    object ViewSubmissionClicked : AssignmentDetailsEvent()
    object ViewUploadStatusClicked : AssignmentDetailsEvent()
    object PullToRefresh : AssignmentDetailsEvent()
    data class SubmissionTypeClicked(val submissionType: Assignment.SubmissionType) : AssignmentDetailsEvent()
    data class DataLoaded(val assignmentResult: DataResult<Assignment>?) : AssignmentDetailsEvent()
    data class SubmissionStatusUpdated(val status: SubmissionUploadStatus) : AssignmentDetailsEvent()
}

sealed class AssignmentDetailsEffect {
    data class ShowSubmitDialogView(val assignment: Assignment, val course: Course) : AssignmentDetailsEffect()
    data class ShowSubmissionView(val assignmentId: Long, val course: Course) : AssignmentDetailsEffect()
    data class ShowUploadStatusView(val assignmentId: Long, val course: Course) : AssignmentDetailsEffect()
    data class ShowCreateSubmissionView(val submissionType: Assignment.SubmissionType, val courseId: Long, val assignment: Assignment) : AssignmentDetailsEffect()
    data class LoadData(val assignmentId: Long, val courseId: Long, val forceNetwork: Boolean) : AssignmentDetailsEffect()
    data class ObserveSubmissionStatus(val assignmentId: Long) : AssignmentDetailsEffect()
}

data class AssignmentDetailsModel(
    val assignmentId: Long,
    val course: Course, // Will always pull from cache for the course
    val isLoading: Boolean = false,
    val assignmentResult: DataResult<Assignment>? = null,
    val status: SubmissionUploadStatus = SubmissionUploadStatus.Empty
)

sealed class SubmissionUploadStatus {
    object Uploading : SubmissionUploadStatus()
    object Failure : SubmissionUploadStatus()
    object Finished : SubmissionUploadStatus()
    object Empty : SubmissionUploadStatus()
}
