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
 * Stores assignment/quiz content for offline access.
 * [description] contains the parsed HTML with local file references replacing remote URLs.
 * [submissionTypes] is a comma-separated list of submission type API strings.
 * [url] is the assignment URL (used by quizzes/assessments to launch the quiz).
 * [ltiToolUrl] is from [com.instructure.canvasapi2.models.ExternalToolAttributes.url], used for LTI button.
 * Submission history is not stored; offline view shows description only.
 */
@Entity(
    tableName = "horizon_assignment_details",
    indices = [Index("courseId")]
)
data class HorizonAssignmentDetailsEntity(
    @PrimaryKey val assignmentId: Long,
    val courseId: Long,
    val name: String?,
    val description: String?,
    val pointsPossible: Double,
    val allowedAttempts: Long,
    val dueAt: String?,
    val submissionTypes: String,
    val gradingType: String?,
    val lockedForUser: Boolean,
    val lockExplanation: String?,
    val quizId: Long,
    val url: String?,
    val ltiToolUrl: String?,
)
