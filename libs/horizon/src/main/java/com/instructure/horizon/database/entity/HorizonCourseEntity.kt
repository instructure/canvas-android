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
 * Unified entity for course data, used by both the My Content list screen and the Course/Program
 * Details screens. Fields from the list screen (enrollment metadata) and from the details screens
 * (syllabus, module durations) are merged here to ensure data consistency when offline.
 */
@Entity(tableName = "horizon_courses")
data class HorizonCourseEntity(
    @PrimaryKey val courseId: Long,
    val name: String,
    val progress: Double,
    val imageUrl: String?,
    val startAtMs: Long?,
    val endAtMs: Long?,
    val requirementCount: Int?,
    val requirementCompletedCount: Int?,
    val completedAtMs: Long?,
    val grade: Double?,
    val workflowState: String?,
    val lastActivityAtMs: Long?,
    val enrolledAtMs: Long?,
    val courseSyllabus: String?,
    val moduleItemsDurations: String,
)
