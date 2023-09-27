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
import com.instructure.canvasapi2.models.RubricCriterionAssessment

@Entity(
    primaryKeys = ["id", "assignmentId"],
    foreignKeys = [
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RubricCriterionAssessmentEntity(
    val id: String,
    val assignmentId: Long,
    val ratingId: String?,
    val points: Double?,
    val comments: String?
) {
    constructor(rubricCriterionAssessment: RubricCriterionAssessment, id: String, assignmentId: Long) : this(
        id = id,
        assignmentId = assignmentId,
        ratingId = rubricCriterionAssessment.ratingId,
        points = rubricCriterionAssessment.points,
        comments = rubricCriterionAssessment.comments
    )

    fun toApiModel() = RubricCriterionAssessment(
        ratingId = ratingId,
        points = points,
        comments = comments
    )
}