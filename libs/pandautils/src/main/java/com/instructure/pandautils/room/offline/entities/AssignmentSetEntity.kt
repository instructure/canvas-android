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
import com.instructure.canvasapi2.models.AssignmentSet
import com.instructure.canvasapi2.models.MasteryPathAssignment

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = MasteryPathEntity::class,
            parentColumns = ["id"],
            childColumns = ["masteryPathId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AssignmentSetEntity(
    @PrimaryKey
    val id: Long,
    val scoringRangeId: Long,
    val createdAt: String?,
    val updatedAt: String?,
    val position: Int,
    val masteryPathId: Long
) {

    constructor(assignmentSet: AssignmentSet, masteryPathId: Long) : this(
        id = assignmentSet.id,
        scoringRangeId = assignmentSet.scoringRangeId,
        createdAt = assignmentSet.createdAt,
        updatedAt = assignmentSet.updatedAt,
        position = assignmentSet.position,
        masteryPathId = masteryPathId
    )

    fun toApiModel(assignments: List<MasteryPathAssignment>): AssignmentSet {
        return AssignmentSet(
            id = id,
            scoringRangeId = scoringRangeId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            position = position,
            assignments = assignments.toTypedArray()
        )
    }
}