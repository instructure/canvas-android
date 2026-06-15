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
package com.instructure.horizon.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val ANNOUNCEMENT_TYPE_COURSE = "course"
const val ANNOUNCEMENT_TYPE_ACCOUNT = "account"

@Entity(
    tableName = "horizon_announcement",
    indices = [Index("courseId")],
)
data class HorizonAnnouncementEntity(
    @PrimaryKey val key: String,
    val id: Long,
    val type: String,
    val courseId: Long?,
    val courseName: String?,
    val title: String,
    val message: String,
    val authorDisplayName: String?,
    val postedAtMs: Long?,
    val isUnread: Boolean,
    val isClosed: Boolean,
)

fun announcementKey(type: String, id: Long, courseId: Long?): String {
    return if (type == ANNOUNCEMENT_TYPE_COURSE) {
        "$type:${courseId ?: -1L}:$id"
    } else {
        "$type:$id"
    }
}
