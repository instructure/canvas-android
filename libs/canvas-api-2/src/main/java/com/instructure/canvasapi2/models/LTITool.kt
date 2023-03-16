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
 *
 */

package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LTITool(
    override var id: Long = 0,
    var name: String? = null,
    var url: String? = null,
    var assignmentId: Long = 0L,
    var courseId: Long = 0L,
    @SerializedName("course_navigation")
    var courseNavigation: CourseNavigation? = null,
    @SerializedName("icon_url")
    var iconUrl: String? = null,
    @SerializedName("context_id")
    var contextId: Long? = null,
    @SerializedName("context_name")
    var contextName: String? = null
) : CanvasModel<LTITool>() {
    override val comparisonString get() = name
}

@Parcelize
data class CourseNavigation(
    val text: String? = null,
    val url: String? = null
) : Parcelable
