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
 * Flattened entity for [com.instructure.canvasapi2.models.Assignment] with submission data
 * needed to reconstruct [com.instructure.horizon.model.AssignmentStatus] offline.
 * [courseId] is stored for efficient per-course deletion.
 * Submission fields are prefixed with "submission" and are null when no submission exists.
 */
@Entity(
    tableName = "horizon_course_assignments",
    indices = [Index("groupId"), Index("courseId")]
)
data class HorizonCourseAssignmentEntity(
    @PrimaryKey val assignmentId: Long,
    val groupId: Long,
    val courseId: Long,
    val name: String?,
    val pointsPossible: Double,
    val dueAt: String?,
    // Submission fields flattened
    val submissionGrade: String?,
    val submissionWorkflowState: String?,
    val submissionExcused: Boolean,
    val submissionMissing: Boolean,
    val submissionLate: Boolean,
    val submissionPostedAtMs: Long?,
    val submissionCustomGradeStatusId: Long?,
    val submissionCommentsCount: Int,
)
