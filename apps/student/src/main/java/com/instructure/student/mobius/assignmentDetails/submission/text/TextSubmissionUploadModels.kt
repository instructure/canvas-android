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

package com.instructure.student.mobius.assignmentDetails.submission.text

import android.net.Uri
import com.instructure.canvasapi2.models.CanvasContext

sealed class TextSubmissionUploadEvent {
    data class TextChanged(val text: String) : TextSubmissionUploadEvent()
    data class SubmitClicked(val text: String) : TextSubmissionUploadEvent()
    data class ImageAdded(val uri: Uri) : TextSubmissionUploadEvent()
    data class SaveDraft(val text: String): TextSubmissionUploadEvent()
    object CameraImageTaken : TextSubmissionUploadEvent()
    object ImageFailed : TextSubmissionUploadEvent()
}

sealed class TextSubmissionUploadEffect {
    data class SubmitText(val text: String, val canvasContext: CanvasContext, val assignmentId: Long, val assignmentName: String?, val attempt: Long) : TextSubmissionUploadEffect()
    data class InitializeText(val text: String) : TextSubmissionUploadEffect()
    data class AddImage(val uri: Uri, val canvasContext: CanvasContext) : TextSubmissionUploadEffect()
    data class SaveDraft(val text: String, val canvasContext: CanvasContext, val assignmentId: Long, val assignmentName: String?) : TextSubmissionUploadEffect()
    object ProcessCameraImage : TextSubmissionUploadEffect()
    object ShowFailedImageMessage : TextSubmissionUploadEffect()
}

data class TextSubmissionUploadModel(
        val canvasContext: CanvasContext,
        val assignmentId: Long,
        val assignmentName: String?,
        val initialText: String? = null,
        val isFailure: Boolean = false,
        val isSubmittable: Boolean = false,
        val attempt: Long = 1L
)
