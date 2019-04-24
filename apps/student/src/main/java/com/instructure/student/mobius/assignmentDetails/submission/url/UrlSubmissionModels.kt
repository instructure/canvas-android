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

sealed class UrlSubmissionEvent {
    data class UrlChanged(val url: String) : UrlSubmissionEvent()
    data class SubmitClicked(val url: String) : UrlSubmissionEvent()
}

sealed class UrlSubmissionEffect {
    object ShowMalformedUrl : UrlSubmissionEffect()
    data class ShowPreviewUrl(val url: String) : UrlSubmissionEffect()
    data class SubmitUrl(val url: String, val courseId: Long, val assignmentId: Long) : UrlSubmissionEffect()
    data class InitializeUrl(val url: String?) : UrlSubmissionEffect()
}

data class UrlSubmissionModel(
    val courseId: Long,
    val assignmentId: Long,
    val initialUrl: String? = null,
    val failureMessage: String? = null,
    val isSubmittable: Boolean = false
)
