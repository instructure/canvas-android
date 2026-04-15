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

/**
 * Stores a single submission attempt for offline access.
 * Primary key is (assignmentId, attempt) because all history entries for the same
 * assignment share the same Canvas submission ID — only [attempt] differs.
 * [submissionId] is the Canvas submission ID, kept as a regular field.
 * File attachments are stored in [HorizonSubmissionAttachmentEntity].
 */
@Entity(
    tableName = "horizon_submissions",
    primaryKeys = ["assignmentId", "attempt"],
    indices = [Index("assignmentId")]
)
data class HorizonSubmissionEntity(
    val assignmentId: Long,
    val attempt: Long,
    val submissionId: Long,
    val grade: String?,
    val score: Double,
    val submittedAtMs: Long?,
    val workflowState: String?,
    val submissionType: String?,
    val body: String?,
    val url: String?,
    val late: Boolean,
    val excused: Boolean,
    val missing: Boolean,
    val customGradeStatusId: Long?,
    val userId: Long,
)

/**
 * Stores file attachments for a submission attempt.
 * Linked via [assignmentId] + [attempt] to [HorizonSubmissionEntity].
 * No FK constraint because [replaceForAssignment] handles cleanup via explicit DELETE.
 */
@Entity(
    tableName = "horizon_submission_attachments",
    primaryKeys = ["id"],
    indices = [Index("assignmentId", "attempt")]
)
data class HorizonSubmissionAttachmentEntity(
    val id: Long,
    val assignmentId: Long,
    val attempt: Long,
    val displayName: String?,
    val url: String?,
    val contentType: String?,
    val thumbnailUrl: String?,
)
