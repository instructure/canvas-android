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

@Entity(tableName = "horizon_dashboard_enrollments")
data class HorizonDashboardEnrollmentEntity(
    @PrimaryKey val enrollmentId: Long,
    val enrollmentState: String,
    val courseId: Long,
    val courseName: String,
    val courseImageUrl: String?,
    val courseSyllabus: String?,
    val institutionName: String?,
    val completionPercentage: Double,
)
