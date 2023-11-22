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
import com.instructure.canvasapi2.models.ModuleCompletionRequirement

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
data class ModuleCompletionRequirementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: String?,
    val minScore: Double,
    val maxScore: Double,
    val completed: Boolean?,
    val moduleId: Long,
    val courseId: Long
) {
    constructor(moduleCompletionRequirement: ModuleCompletionRequirement, moduleId: Long, courseId: Long, moduleItemId: Long? = null) : this(
        // In some api calls we don't receive the id of this entity, but it corresponds to the id of the module item so in that case we can use that
        id = if (moduleCompletionRequirement.id == 0L) moduleItemId ?: 0 else moduleCompletionRequirement.id,
        type = moduleCompletionRequirement.type,
        minScore = moduleCompletionRequirement.minScore,
        maxScore = moduleCompletionRequirement.maxScore,
        completed = moduleCompletionRequirement.completed,
        moduleId = moduleId,
        courseId = courseId
    )

    fun toApiModel() = ModuleCompletionRequirement(
        id = id,
        type = type,
        minScore = minScore,
        maxScore = maxScore,
        completed = completed ?: false
    )
}
