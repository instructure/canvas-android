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
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class AssignmentOverride(
        override val id: Long = 0,
        @SerializedName("assignment_id")
        val assignmentId: Long = 0,
        val title: String? = null,
        @SerializedName("due_at")
        val dueAt: Date? = null,
        @SerializedName("all_day")
        val isAllDay: Boolean = false,
        @SerializedName("all_day_date")
        val allDayDate: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: Date? = null,
        @SerializedName("lock_at")
        val lockAt: Date? = null,
        @SerializedName("course_section_id")
        val courseSectionId: Long = 0,
        @SerializedName("student_ids")
        val studentIds: List<Long> = arrayListOf(),
        @SerializedName("group_id")
        val groupId: Long = 0
) : CanvasModel<AssignmentOverride>() {
    override val comparisonDate get() = dueAt
    override val comparisonString get() = title
}