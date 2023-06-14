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
import com.instructure.canvasapi2.models.AssignmentOverride
import java.util.*

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
data class AssignmentOverrideEntity(
    @PrimaryKey
    val id: Long,
    val assignmentId: Long,
    val title: String?,
    val dueAt: Date?,
    val isAllDay: Boolean,
    val allDayDate: String?,
    val unlockAt: Date?,
    val lockAt: Date?,
    val courseSectionId: Long,
    val groupId: Long
) {

    constructor(assignmentOverride: AssignmentOverride) : this(
        assignmentOverride.id,
        assignmentOverride.assignmentId,
        assignmentOverride.title,
        assignmentOverride.dueAt,
        assignmentOverride.isAllDay,
        assignmentOverride.allDayDate,
        assignmentOverride.unlockAt,
        assignmentOverride.lockAt,
        assignmentOverride.courseSectionId,
        assignmentOverride.groupId
    )

    fun toApiModel() = AssignmentOverride(
        id,
        assignmentId,
        title,
        dueAt,
        isAllDay,
        allDayDate,
        unlockAt,
        lockAt,
        courseSectionId
    )
}