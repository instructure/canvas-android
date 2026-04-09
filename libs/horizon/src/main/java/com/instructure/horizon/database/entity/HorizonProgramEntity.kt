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
import androidx.room.PrimaryKey

/**
 * Unified entity for program data, used by both the My Content list screen and the Program
 * Details screen. Merges fields from [ProgramEnrollmentItem] (list) and the
 * [Program] domain model (details) to ensure data consistency when offline.
 */
@Entity(tableName = "horizon_programs")
data class HorizonProgramEntity(
    @PrimaryKey val programId: String,
    val name: String,
    val description: String?,
    val startDateMs: Long?,
    val endDateMs: Long?,
    val variant: String,
    val estimatedDurationMinutes: Int?,
    val courseCount: Int?,
    val courseCompletionCount: Int?,
    val enrolledAtMs: Long?,
    val completionPercentage: Double?,
    val enrollmentStatus: String?,
)
