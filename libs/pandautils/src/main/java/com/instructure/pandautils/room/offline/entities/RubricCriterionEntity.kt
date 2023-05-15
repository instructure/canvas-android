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
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.RubricCriterion

@Entity
data class RubricCriterionEntity(
    @PrimaryKey
    val id: String,
    val assignmentId: Long,
    val description: String?,
    val longDescription: String?,
    val points: Double,
    val criterionUseRange: Boolean,
    val ignoreForScoring: Boolean,
) {
    constructor(rubricCriterion: RubricCriterion, assignmentId: Long) : this(
        rubricCriterion.id.orEmpty(),
        assignmentId,
        rubricCriterion.description,
        rubricCriterion.longDescription,
        rubricCriterion.points,
        rubricCriterion.criterionUseRange,
        rubricCriterion.ignoreForScoring,
    )

    fun toApiModel() = RubricCriterion(
        id = id,
        description = description,
        longDescription = longDescription,
        points = points,
        ratings = arrayListOf(),
        criterionUseRange = criterionUseRange,
        ignoreForScoring = ignoreForScoring
    )
}