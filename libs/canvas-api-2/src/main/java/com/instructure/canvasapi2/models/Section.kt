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
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Section(
        override val id: Long = 0,
        override var name: String = "",
        @SerializedName("course_id")
        val courseId: Long = 0,
        @SerializedName("start_at")
        val startAt: String? = null,
        @SerializedName("end_at")
        val endAt: String? = null,
        val students: List<User>? = arrayListOf(),
        @SerializedName("total_students")
        val totalStudents: Int = 0,
        @SerializedName("restrict_enrollments_to_section_dates")
        val restrictEnrollmentsToSectionDates: Boolean = false
) : CanvasContext() {
    override val comparisonString get() = name
    override val type get() = CanvasContext.Type.SECTION

    val endDate get() = endAt.toDate()

    val startDate get() = startAt.toDate()
}
