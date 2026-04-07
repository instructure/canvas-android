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

/**
 * Flattened entity for both [com.instructure.canvasapi2.models.journey.mycontent.ProgramEnrollmentItem]
 * and [com.instructure.canvasapi2.models.journey.mycontent.CourseEnrollmentItem].
 * [queryKey] identifies which fetch bucket this item belongs to (e.g. "IN_PROGRESS" or "COMPLETED").
 */
@Entity(
    tableName = "horizon_learn_items",
    indices = [Index("queryKey")]
)
data class HorizonLearnItemEntity(
    @PrimaryKey val id: String,
    val queryKey: String,
    val itemType: String,
    val name: String,
    val position: Int,
    val enrolledAtMs: Long?,
    val completionPercentage: Double?,
    // ProgramEnrollmentItem-specific fields
    val startDateMs: Long?,
    val endDateMs: Long?,
    val enrollmentStatus: String?,
    val description: String?,
    val variant: String?,
    val estimatedDurationMinutes: Int?,
    val courseCount: Int?,
    // CourseEnrollmentItem-specific fields
    val startAtMs: Long?,
    val endAtMs: Long?,
    val requirementCount: Int?,
    val requirementCompletedCount: Int?,
    val completedAtMs: Long?,
    val grade: Double?,
    val imageUrl: String?,
    val workflowState: String?,
    val lastActivityAtMs: Long?,
)
