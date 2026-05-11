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
 * Flattened entity for the full depaginated learning library browse list
 * ([com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem]).
 */
@Entity(tableName = "horizon_learn_browse_items")
data class HorizonLearnBrowseItemEntity(
    @PrimaryKey val id: String,
    val libraryId: String,
    val itemType: String,
    val displayOrder: Double,
    // CanvasCourseInfo flattened
    val canvasCourseId: String?,
    val canvasUrl: String?,
    val courseName: String?,
    val courseImageUrl: String?,
    val moduleCount: Double?,
    val moduleItemCount: Double?,
    val estimatedDurationMinutes: Double?,
    // LearningLibraryModuleInfo flattened
    val moduleId: String?,
    val moduleItemId: String?,
    val moduleItemType: String?,
    val resourceId: String?,
    // Other
    val programId: String?,
    val programCourseId: String?,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val isBookmarked: Boolean,
    val completionPercentage: Double?,
    val isEnrolledInCanvas: Boolean?,
    val canvasEnrollmentId: String?,
)
