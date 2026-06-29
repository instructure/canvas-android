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

@Entity(
    tableName = "horizon_notification_stream_item",
    indices = [Index("courseId")],
)
data class HorizonNotificationStreamItemEntity(
    @PrimaryKey val id: Long,
    val type: String,
    val notificationCategory: String,
    val title: String,
    val courseId: Long,
    val courseName: String?,
    val isRead: Boolean,
    val score: Double,
    val grade: String?,
    val updatedAtMs: Long?,
    val htmlUrl: String,
    val assignmentHtmlUrl: String?,
)
