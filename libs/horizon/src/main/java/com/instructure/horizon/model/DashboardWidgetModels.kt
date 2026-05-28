/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class TimeSpentWidgetData(
    val lastModifiedDate: Date?,
    val data: List<TimeSpentDataEntry>,
)

data class TimeSpentDataEntry(
    val date: Date? = null,

    @SerializedName("user_id")
    val userId: Long? = null,

    @SerializedName("user_uuid")
    val userUUID: String? = null,

    @SerializedName("user_name")
    val userName: String? = null,

    @SerializedName("user_email")
    val userEmail: String? = null,

    @SerializedName("user_avatar_image_url")
    val userAvatarUrl: String? = null,

    @SerializedName("course_id")
    val courseId: Long? = null,

    @SerializedName("course_name")
    val courseName: String? = null,

    @SerializedName("minutes_per_day")
    val minutesPerDay: Int? = null,
)

data class MyProgressWidgetData(
    val data: List<MyProgressWidgetDataEntry>,
    val lastModifiedDate: Date?,
)

data class MyProgressWidgetDataEntry(
    @SerializedName("course_id")
    val courseId: Long?,

    @SerializedName("course_name")
    val courseName: String?,

    @SerializedName("user_id")
    val userId: Long?,

    @SerializedName("user_uuid")
    val userUUID: String?,

    @SerializedName("user_name")
    val userName: String?,

    @SerializedName("user_avatar_image_url")
    val userAvatarUrl: String?,

    @SerializedName("user_email")
    val userEmail: String?,

    @SerializedName("module_count_completed")
    val moduleCountCompleted: Int?,

    @SerializedName("module_count_started")
    val moduleCountStarted: Int?,

    @SerializedName("module_count_locked")
    val moduleCountLocked: Int?,

    @SerializedName("module_count_total")
    val moduleCountTotal: Int?,
)
