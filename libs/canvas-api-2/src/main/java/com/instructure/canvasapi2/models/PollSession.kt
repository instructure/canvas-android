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

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class PollSession(
        override val id: Long = 0,
        @SerializedName("poll_id")
        val pollId: Long = 0,
        @SerializedName("course_id")
        val courseId: Long = 0,
        @SerializedName("course_section_id")
        val courseSectionId: Long = 0,
        @SerializedName("is_published")
        var isPublished: Boolean = false,
        @SerializedName("has_public_results")
        val hasPublicResults: Boolean = false,
        val results: Map<Long, Int>? = null,
        @SerializedName("created_at")
        val createdAt: Date? = null,
        @SerializedName("poll_submissions")
        val pollSubmissions: List<PollSubmission>? = null,
        @SerializedName("has_submitted")
        val hasSubmitted: Boolean = false
) : CanvasComparable<PollSession>() {
    override val comparisonDate get() = createdAt
}
