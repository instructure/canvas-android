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

import android.net.Uri
import android.os.Bundle
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.Submission
import java.io.File

sealed class AssignmentDetailsEvent {
    object SubmitAssignmentClicked : AssignmentDetailsEvent()
    object ViewSubmissionClicked : AssignmentDetailsEvent()
    object ViewUploadStatusClicked : AssignmentDetailsEvent()
    object AudioRecordingClicked : AssignmentDetailsEvent()
    object VideoRecordingClicked : AssignmentDetailsEvent()
    object ChooseMediaClicked : AssignmentDetailsEvent()
    object OnVideoRecordingError : AssignmentDetailsEvent()
    object OnMediaPickingError : AssignmentDetailsEvent()
    object DiscussionAttachmentClicked : AssignmentDetailsEvent()
    object AddBookmarkClicked : AssignmentDetailsEvent()
    object PullToRefresh : AssignmentDetailsEvent()
    object SendVideoRecording : AssignmentDetailsEvent()
    data class SendMediaFile(val uri: Uri) : AssignmentDetailsEvent()
    data class StoreVideoUri(val uri: Uri?) : AssignmentDetailsEvent()
    data class SendAudioRecordingClicked(val file: File?) : AssignmentDetailsEvent()
    data class DataLoaded(
        val assignmentResult: DataResult<Assignment>?,
        val isStudioEnabled: Boolean,
        val studioLTIToolResult: DataResult<LTITool>?,
        val ltiToolResult: DataResult<LTITool>?,
        val submission: Submission?,
        val quizResult: DataResult<Quiz>?,
        val isObserver: Boolean = false
    ) : AssignmentDetailsEvent()
    data class SubmissionStatusUpdated(val submission: Submission?) : AssignmentDetailsEvent()
    data class InternalRouteRequested(val url: String) : AssignmentDetailsEvent()
}

/**
 * NOTE: If you make any submission changes here, make sure to make the same changes
 * in the empty submission details page as well, which also has a submit button.
 */
sealed class AssignmentDetailsEffect {
    object ShowAudioRecordingView : AssignmentDetailsEffect()
    object ShowVideoRecordingView : AssignmentDetailsEffect()
    object ShowMediaPickerView : AssignmentDetailsEffect()
    object ShowAudioRecordingError : AssignmentDetailsEffect()
    object ShowVideoRecordingError : AssignmentDetailsEffect()
    object ShowMediaPickingError : AssignmentDetailsEffect()
    object ShowBookmarkDialog : AssignmentDetailsEffect()
    data class UploadVideoSubmission(val uri: Uri, val course: Course, val assignment: Assignment) : AssignmentDetailsEffect()
    data class UploadAudioSubmission(val file: File, val course: Course, val assignment: Assignment) : AssignmentDetailsEffect()
    data class UploadMediaFileSubmission(val uri: Uri, val course: Course, val assignment: Assignment) : AssignmentDetailsEffect()
    data class ShowSubmitDialogView(val assignment: Assignment, val course: Course, val isStudioEnabled: Boolean, val studioLTITool: LTITool? = null) : AssignmentDetailsEffect()
    data class ShowQuizStartView(val quiz: Quiz, val course: Course) : AssignmentDetailsEffect()
    data class ShowDiscussionDetailView(val discussionTopicHeaderId: Long, val course: Course) : AssignmentDetailsEffect()
    data class ShowDiscussionAttachment(val discussionAttachment: Attachment, val course: Course) : AssignmentDetailsEffect()
    data class ShowSubmissionView(val assignmentId: Long, val course: Course, val isObserver: Boolean = false) : AssignmentDetailsEffect()
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
    val quizResult: DataResult<Quiz>? = null,
    val ltiTool: DataResult<LTITool>? = null,
    val databaseSubmission: Submission? = null,
    val videoFileUri: Uri? = null,
    var shouldRouteToSubmissionDetails: Boolean = false,
    val isObserver: Boolean = false
)
