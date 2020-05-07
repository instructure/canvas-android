/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
import java.util.*

data class Plannable(
    val id: Long,

    val title: String,

    @SerializedName("course_id")
    val courseId: Long?,

    @SerializedName("group_id")
    val groupId: Long?,

    @SerializedName("user_id")
    val userId: Long?,

    @SerializedName("points_possible")
    val pointsPossible: Double?,

    @SerializedName("due_at")
    val dueAt: Date?,

    // Used to determine if a quiz is an assignment or not
    @SerializedName("assignment_id")
    val assignmentId: Long?
) {}
