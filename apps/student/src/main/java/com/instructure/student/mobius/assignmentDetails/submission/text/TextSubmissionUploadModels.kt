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

import com.instructure.canvasapi2.models.CanvasContext

sealed class TextSubmissionUploadEvent {
    data class TextChanged(val text: String) : TextSubmissionUploadEvent()
    data class SubmitClicked(val text: String) : TextSubmissionUploadEvent()
}

sealed class TextSubmissionUploadEffect {
    data class SubmitText(val text: String, val canvasContext: CanvasContext, val assignmentId: Long, val assignmentName: String?) : TextSubmissionUploadEffect()
    data class InitializeText(val text: String) : TextSubmissionUploadEffect()
}

data class TextSubmissionUploadModel(
        val canvasContext: CanvasContext,
        val assignmentId: Long,
        val assignmentName: String?,
        val initialText: String? = null,
        val failureMessage: String? = null,
        val isSubmittable: Boolean = false
)
