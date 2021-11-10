/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models.canvadocs

import com.google.gson.annotations.SerializedName

data class CanvaDocSessionRequestBody(
    @SerializedName("submission_id")
    val submussionId: String,
    @SerializedName("submission_attempt")
    val submissionAttempt: String
)

data class CanvaDocSessionResponseBody(
    @SerializedName("annotation_context_launch_id")
    val annotationContextLaunchId: String? = null,
    @SerializedName("canvadocs_session_url")
    val canvadocsSessionUrl: String? = null
)