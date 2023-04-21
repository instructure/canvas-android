/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AssignmentGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AssignmentEntity(
    @PrimaryKey
    val id: Long,
    val name: String?,
    val description: String?,
    val submissionTypesRaw: List<String>,
    val dueAt: String?,
    val pointsPossible: Double,
    val courseId: Long,
    val isGradeGroupsIndividually: Boolean,
    val gradingType: String?,
    val needsGradingCount: Long,
    val htmlUrl: String?,
    val url: String?,
    val quizId: Long,
    val isUseRubricForGrading: Boolean,
    val rubricSettingsId: Long,
    val allowedExtensions: List<String>,
    val submissionId: Long?,
    val assignmentGroupId: Long,
    val position: Int,
    val isPeerReviews: Boolean,
    // TODO val lockInfo: LockInfo?,
    val lockedForUser: Boolean,
    val lockAt: String?,
    val unlockAt: String?,
    val lockExplanation: String?,
    val discussionTopicHeaderId: Long?,
    val freeFormCriterionComments: Boolean,
    val published: Boolean,
    val groupCategoryId: Long,
    val userSubmitted: Boolean,
    val unpublishable: Boolean,
    val onlyVisibleToOverrides: Boolean,
    val anonymousPeerReviews: Boolean,
    val moderatedGrading: Boolean,
    val anonymousGrading: Boolean,
    val allowedAttempts: Long,
    val plannerOverrideId: Long?,
    val isStudioEnabled: Boolean,
    val inClosedGradingPeriod: Boolean,
    val annotatableAttachmentId: Long,
    val anonymousSubmissions: Boolean,
    val omitFromFinalGrade: Boolean
)