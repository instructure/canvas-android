/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class GradeChange(
    override var id: Long = 0,
    @SerializedName("created_at")
    val createdAt: Date? = null,
    @SerializedName("event_type")
    val eventType: String? = null,
    @SerializedName("grade_before")
    val gradeBefore: String? = null,
    @SerializedName("grade_after")
    val gradeAfter: String? = null,
    @SerializedName("excused_before")
    val excusedBefore: Boolean = false,
    @SerializedName("excused_after")
    val excusedAfter: Boolean = false,
    @SerializedName("graded_anonymously")
    val gradedAnonymously: Boolean? = null,
    @SerializedName("version_number")
    val versionNumber: Int = 0,
    @SerializedName("request_id")
    val requestId: String? = null,
    val links: GradeChangeLinks? = null
) : CanvasModel<GradeChange>() {
    override val comparisonDate: Date?
        get() = createdAt
}

@Parcelize
data class GradeChangeLinks(
    val assignment: Long = 0,
    val course: Long = 0,
    val student: Long = 0,
    val grader: Long = 0,
    @SerializedName("page_view")
    val pageView: String? = null
) : CanvasModel<GradeChangeLinks>()