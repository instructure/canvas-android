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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.GradingRule

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AssignmentGroupEntity(
    @PrimaryKey
    val id: Long,
    val name: String?,
    val position: Int,
    val groupWeight: Double,
    val rules: GradingRule?,
    val courseId: Long
) {
    constructor(assignmentGroup: AssignmentGroup, courseId: Long) : this(
        assignmentGroup.id,
        assignmentGroup.name,
        assignmentGroup.position,
        assignmentGroup.groupWeight,
        assignmentGroup.rules,
        courseId
    )

    fun toApiModel(assignments: List<Assignment> = emptyList()) = AssignmentGroup(
        id = id,
        name = name,
        position = position,
        groupWeight = groupWeight,
        assignments = assignments,
        rules = rules
    )
}