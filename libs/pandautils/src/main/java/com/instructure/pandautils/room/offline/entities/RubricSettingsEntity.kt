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
import com.instructure.canvasapi2.models.RubricSettings

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RubricSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val contextId: Long,
    val contextType: String?,
    val pointsPossible: Double,
    val title: String,
    val isReusable: Boolean,
    val isPublic: Boolean,
    val isReadOnly: Boolean,
    val freeFormCriterionComments: Boolean,
    val hideScoreTotal: Boolean,
    val hidePoints: Boolean,
    val assignmentId: Long
) {
    constructor(rubricSettings: RubricSettings, assignmentId: Long) : this(
        rubricSettings.id ?: 0L,
        rubricSettings.contextId,
        rubricSettings.contextType,
        rubricSettings.pointsPossible,
        rubricSettings.title,
        rubricSettings.isReusable,
        rubricSettings.isPublic,
        rubricSettings.isReadOnly,
        rubricSettings.freeFormCriterionComments,
        rubricSettings.hideScoreTotal,
        rubricSettings.hidePoints,
        assignmentId
    )

    fun toApiModel() = RubricSettings(
        id = id,
        contextId = contextId,
        contextType = contextType,
        pointsPossible = pointsPossible,
        title = title,
        isReusable = isReusable,
        isPublic = isPublic,
        isReadOnly = isReadOnly,
        freeFormCriterionComments = freeFormCriterionComments,
        hideScoreTotal = hideScoreTotal,
        hidePoints = hidePoints
    )
}