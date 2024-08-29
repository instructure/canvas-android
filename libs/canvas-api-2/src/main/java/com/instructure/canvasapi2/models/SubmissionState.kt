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
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.SubmissionStateTypeAdapter
import kotlinx.parcelize.Parcelize

/**
 * If this class is expanded with other fields, the type adapter should be updated as well.
 */
@JsonAdapter(SubmissionStateTypeAdapter::class)
@Parcelize
data class SubmissionState(
    @SerializedName("submitted")
    val submitted: Boolean = false,
    @SerializedName("missing")
    val missing: Boolean = false,
    @SerializedName("late")
    val late: Boolean = false,
    @SerializedName("excused")
    val excused: Boolean = false,
    @SerializedName("graded")
    val graded: Boolean = false,
    @SerializedName("needs_grading")
    val needsGrading: Boolean = false,
    @SerializedName("with_feedback")
    val withFeedback: Boolean = false,
    @SerializedName("redo_request")
    val redoRequest: Boolean = false
): Parcelable