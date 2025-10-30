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

package com.instructure.student.mobius.assignmentDetails.submission.url

import com.instructure.canvasapi2.models.CanvasContext

sealed class UrlSubmissionUploadEvent {
    data class UrlChanged(val url: String) : UrlSubmissionUploadEvent()
    data class SubmitClicked(val url: String) : UrlSubmissionUploadEvent()
}

sealed class UrlSubmissionUploadEffect {
    data class ShowUrlPreview(val url: String) : UrlSubmissionUploadEffect()
    data class SubmitUrl(val url: String, val course: CanvasContext, val assignmentId: Long, val assignmentName: String?, val attempt: Long) : UrlSubmissionUploadEffect()
    data class InitializeUrl(val url: String?) : UrlSubmissionUploadEffect()
}

enum class MalformedUrlError {
    CLEARTEXT, NONE
}

data class UrlSubmissionUploadModel(
        val course: CanvasContext,
        val assignmentId: Long,
        val assignmentName: String? = null,
        val initialUrl: String? = null,
        val isFailure: Boolean = false,
        val urlError: MalformedUrlError = MalformedUrlError.NONE,
        val isSubmittable: Boolean = false,
        val attempt: Long = 1L
)
