/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.MasteryPathAssignment

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = AssignmentSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentSetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MasteryPathAssignmentEntity(
    @PrimaryKey
    val id: Long,
    val assignmentId: Long,
    val createdAt: String?,
    val updatedAt: String?,
    val overrideId: Long,
    val assignmentSetId: Long,
    val position: Int
) {

    constructor(masteryPathAssignment: MasteryPathAssignment) : this(
        id = masteryPathAssignment.id,
        assignmentId = masteryPathAssignment.assignmentId,
        createdAt = masteryPathAssignment.createdAt,
        updatedAt = masteryPathAssignment.updatedAt,
        overrideId = masteryPathAssignment.overrideId,
        assignmentSetId = masteryPathAssignment.assignmentSetId,
        position = masteryPathAssignment.position
    )

    fun toApiModel(assignment: Assignment?): MasteryPathAssignment {
        return MasteryPathAssignment(
            id = id,
            assignmentId = assignmentId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            overrideId = overrideId,
            assignmentSetId = assignmentSetId,
            position = position,
            model = assignment
        )
    }
}