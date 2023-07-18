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
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.ModuleCompletionRequirement

@Entity
data class ModuleCompletionRequirementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String?,
    val minScore: Double,
    val maxScore: Double,
    val completed: Boolean?,
    val moduleId: Long
) {
    constructor(moduleCompletionRequirement: ModuleCompletionRequirement, moduleId: Long) : this(
        type = moduleCompletionRequirement.type,
        minScore = moduleCompletionRequirement.minScore,
        maxScore = moduleCompletionRequirement.maxScore,
        completed = moduleCompletionRequirement.completed,
        moduleId = moduleId
    )

    fun toApiModel() = ModuleCompletionRequirement(
        id = id,
        type = type,
        minScore = minScore,
        maxScore = maxScore,
        completed = completed ?: false
    )
}
