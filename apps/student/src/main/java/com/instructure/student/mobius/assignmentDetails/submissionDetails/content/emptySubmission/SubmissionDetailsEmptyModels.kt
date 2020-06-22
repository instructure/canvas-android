/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission

import android.net.Uri
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEffect
import java.io.File

sealed class SubmissionDetailsEmptyContentEvent {
    object SubmitAssignmentClicked : SubmissionDetailsEmptyContentEvent()
    object AudioRecordingClicked : SubmissionDetailsEmptyContentEvent()
    object VideoRecordingClicked : SubmissionDetailsEmptyContentEvent()
    object ChooseMediaClicked : SubmissionDetailsEmptyContentEvent()
    object OnVideoRecordingError : SubmissionDetailsEmptyContentEvent()
    object OnMediaPickingError : SubmissionDetailsEmptyContentEvent()
    object SendVideoRecording : SubmissionDetailsEmptyContentEvent()
    data class SendMediaFile(val uri: Uri) : SubmissionDetailsEmptyContentEvent()
    data class StoreVideoUri(val uri: Uri?) : SubmissionDetailsEmptyContentEvent()
    data class SendAudioRecordingClicked(val file: File?) : SubmissionDetailsEmptyContentEvent()
    object SubmissionStarted : SubmissionDetailsEmptyContentEvent()
}

sealed class SubmissionDetailsEmptyContentEffect {
    object ShowAudioRecordingView : SubmissionDetailsEmptyContentEffect()
    object ShowVideoRecordingView : SubmissionDetailsEmptyContentEffect()
    object ShowMediaPickerView : SubmissionDetailsEmptyContentEffect()
    object ShowAudioRecordingError : SubmissionDetailsEmptyContentEffect()
    object ShowVideoRecordingError : SubmissionDetailsEmptyContentEffect()
    object ShowMediaPickingError : SubmissionDetailsEmptyContentEffect()
    object SubmissionStarted : SubmissionDetailsEmptyContentEffect()
    data class UploadVideoSubmission(val uri: Uri, val course: Course, val assignment: Assignment) : SubmissionDetailsEmptyContentEffect()
    data class UploadAudioSubmission(val file: File, val course: Course, val assignment: Assignment) : SubmissionDetailsEmptyContentEffect()
    data class UploadMediaFileSubmission(val uri: Uri, val course: Course, val assignment: Assignment) : SubmissionDetailsEmptyContentEffect()
    data class ShowSubmitDialogView(val assignment: Assignment, val course: Course, val isStudioEnabled: Boolean, val studioLTITool: LTITool? = null) : SubmissionDetailsEmptyContentEffect()
    data class ShowQuizStartView(val quiz: Quiz, val course: Course) : SubmissionDetailsEmptyContentEffect()
    data class ShowDiscussionDetailView(val discussionTopicHeaderId: Long, val course: Course) : SubmissionDetailsEmptyContentEffect()
    data class ShowCreateSubmissionView(val submissionType: Assignment.SubmissionType, val course: Course, val assignment: Assignment, val ltiUrl: String? = null) : SubmissionDetailsEmptyContentEffect()
}

data class SubmissionDetailsEmptyContentModel(
    val assignment: Assignment,
    val course: Course,
    val isStudioEnabled: Boolean,
    val quiz: Quiz? = null,
    val studioLTITool: LTITool? = null,
    val videoFileUri: Uri? = null,
    val isObserver: Boolean = false
)