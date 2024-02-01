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
import com.instructure.canvasapi2.models.LockedModule
import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleName

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = LockInfoEntity::class,
            parentColumns = ["id"],
            childColumns = ["lockInfoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LockedModuleEntity(
    @PrimaryKey
    val id: Long,
    val contextId: Long,
    val contextType: String?,
    val name: String?,
    val unlockAt: String?,
    val isRequireSequentialProgress: Boolean,
    val lockInfoId: Long?
) {
    constructor(lockedModule: LockedModule, lockInfoId: Long?) : this(
        id = lockedModule.id,
        contextId = lockedModule.contextId,
        contextType = lockedModule.contextType,
        name = lockedModule.name,
        unlockAt = lockedModule.unlockAt,
        isRequireSequentialProgress = lockedModule.isRequireSequentialProgress,
        lockInfoId = lockInfoId
    )

    fun toApiModel(
        prerequisites: List<ModuleName>? = null,
        completionRequirements: List<ModuleCompletionRequirement> = emptyList()
    ) = LockedModule(
        id = id,
        contextId = contextId,
        contextType = contextType,
        name = name,
        unlockAt = unlockAt,
        isRequireSequentialProgress = isRequireSequentialProgress,
        prerequisites = prerequisites,
        completionRequirements = completionRequirements
    )
}
