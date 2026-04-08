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
 * Stores course data needed by the Program Details and Course Details screens offline.
 * [moduleItemsDurations] is stored as a comma-separated list of ISO 8601 duration strings.
 */
@Entity(tableName = "horizon_learn_courses")
data class HorizonLearnCourseEntity(
    @PrimaryKey val courseId: Long,
    val courseName: String,
    val progress: Double,
    val courseSyllabus: String?,
    val startDateMs: Long?,
    val endDateMs: Long?,
    val moduleItemsDurations: String,
)
