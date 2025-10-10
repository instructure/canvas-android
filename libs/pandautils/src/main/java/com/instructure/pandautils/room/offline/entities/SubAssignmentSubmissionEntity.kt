/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.SubAssignmentSubmission

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SubmissionEntity::class,
            parentColumns = ["id", "attempt"],
            childColumns = ["submissionId", "submissionAttempt"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SubAssignmentSubmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val submissionId: Long,
    val submissionAttempt: Long,
    val grade: String?,
    val score: Double,
    val late: Boolean,
    val excused: Boolean,
    val missing: Boolean,
    val latePolicyStatus: String?,
    val customGradeStatusId: Long?,
    val subAssignmentTag: String?,
    val enteredScore: Double,
    val enteredGrade: String?,
    val userId: Long,
    val isGradeMatchesCurrentSubmission: Boolean
) {
    constructor(subAssignmentSubmission: SubAssignmentSubmission, submissionId: Long, submissionAttempt: Long) : this(
        submissionId = submissionId,
        submissionAttempt = submissionAttempt,
        grade = subAssignmentSubmission.grade,
        score = subAssignmentSubmission.score,
        late = subAssignmentSubmission.late,
        excused = subAssignmentSubmission.excused,
        missing = subAssignmentSubmission.missing,
        latePolicyStatus = subAssignmentSubmission.latePolicyStatus,
        customGradeStatusId = subAssignmentSubmission.customGradeStatusId,
        subAssignmentTag = subAssignmentSubmission.subAssignmentTag,
        enteredScore = subAssignmentSubmission.enteredScore,
        enteredGrade = subAssignmentSubmission.enteredGrade,
        userId = subAssignmentSubmission.userId,
        isGradeMatchesCurrentSubmission = subAssignmentSubmission.isGradeMatchesCurrentSubmission
    )

    fun toApiModel() = SubAssignmentSubmission(
        grade = grade,
        score = score,
        late = late,
        excused = excused,
        missing = missing,
        latePolicyStatus = latePolicyStatus,
        customGradeStatusId = customGradeStatusId,
        subAssignmentTag = subAssignmentTag,
        enteredScore = enteredScore,
        enteredGrade = enteredGrade,
        userId = userId,
        isGradeMatchesCurrentSubmission = isGradeMatchesCurrentSubmission
    )
}
