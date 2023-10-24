/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RubricSettings(
    val id: Long? = 0,
    @SerializedName("context_id")
    val contextId: Long = 0,
    @SerializedName("context_type")
    val contextType: String? = null,
    @SerializedName("points_possible")
    val pointsPossible: Double = 0.0,
    val title: String = "",
    @SerializedName("reusable")
    val isReusable: Boolean = false,
    @SerializedName("public")
    val isPublic: Boolean = false,
    @SerializedName("read_only")
    val isReadOnly: Boolean = false,
    @SerializedName("free_form_criterion_comments")
    val freeFormCriterionComments: Boolean = false,
    @SerializedName("hide_score_total")
    val hideScoreTotal: Boolean = false,
    @SerializedName("hide_points")
    val hidePoints: Boolean = false
) : Parcelable
