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
package com.instructure.student.mobius.assignmentDetails.submission.picker

import android.net.Uri
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject

sealed class PickerSubmissionUploadEvent {
    object SubmitClicked : PickerSubmissionUploadEvent()
    object CameraClicked : PickerSubmissionUploadEvent()
    object GalleryClicked : PickerSubmissionUploadEvent()
    object SelectFileClicked : PickerSubmissionUploadEvent()
    data class OnFileSelected(val uri: Uri) : PickerSubmissionUploadEvent()
    data class OnFileRemoved(val fileIndex: Int) : PickerSubmissionUploadEvent()
    data class OnFileAdded(val file: FileSubmitObject?) : PickerSubmissionUploadEvent()
}

sealed class PickerSubmissionUploadEffect {
    object LaunchCamera : PickerSubmissionUploadEffect()
    object LaunchGallery : PickerSubmissionUploadEffect()
    object LaunchSelectFile : PickerSubmissionUploadEffect()
    data class HandleSubmit(val model: PickerSubmissionUploadModel) : PickerSubmissionUploadEffect()
    data class LoadFileContents(val uri: Uri, val allowedExtensions: List<String>) :
        PickerSubmissionUploadEffect()
    data class RemoveTempFile(val path: String) : PickerSubmissionUploadEffect()
}

data class PickerSubmissionUploadModel(
    val canvasContext: CanvasContext,
    val assignmentId: Long,
    val assignmentName: String,
    val assignmentGroupCategoryId: Long,
    val allowedExtensions: List<String>,
    val mode: PickerSubmissionMode,
    val mediaFileUri: Uri? = null,
    val files: List<FileSubmitObject> = emptyList(),
    val isLoadingFile: Boolean = false,
    val attemptId: Long? = null
)

enum class PickerSubmissionMode {
    MediaSubmission,
    FileSubmission,
    CommentAttachment;

    val isMediaSubmission get() = this == MediaSubmission
    val isForComment get() = this == CommentAttachment
}
