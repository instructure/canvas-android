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
 * Flattened entity for [com.instructure.canvasapi2.models.ModuleItem] belonging to a course module.
 * [courseId] is stored for efficient per-course deletion.
 * [completionRequirementType], [completionRequirementMinScore], [completionRequirementCompleted]
 * are from [com.instructure.canvasapi2.models.ModuleCompletionRequirement].
 * [pointsPossible], [dueAt], [lockedForUser], [lockExplanation], [lockAt], [unlockAt]
 * are from [com.instructure.canvasapi2.models.ModuleContentDetails].
 */
@Entity(
    tableName = "horizon_course_module_items",
    indices = [Index("moduleId"), Index("courseId")]
)
data class HorizonCourseModuleItemEntity(
    @PrimaryKey val itemId: Long,
    val moduleId: Long,
    val courseId: Long,
    val title: String?,
    val position: Int,
    val type: String?,
    val htmlUrl: String?,
    val url: String?,
    // ModuleCompletionRequirement flattened
    val completionRequirementType: String?,
    val completionRequirementMinScore: Double,
    val completionRequirementCompleted: Boolean,
    // ModuleContentDetails flattened
    val pointsPossible: String?,
    val dueAt: String?,
    val lockedForUser: Boolean,
    val lockExplanation: String?,
    val lockAt: String?,
    val unlockAt: String?,
    // Other
    val quizLti: Boolean,
    val estimatedDuration: String?,
    val pageUrl: String?,
)
