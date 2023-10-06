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
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.ModuleContentDetails

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ModuleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ModuleContentDetailsEntity(
    @PrimaryKey
    val id: Long,
    val pointsPossible: String?,
    val dueAt: String?,
    val unlockAt: String?,
    val lockAt: String?,
    val lockedForUser: Boolean,
    val lockExplanation: String?
) {
    constructor(moduleContentDetails: ModuleContentDetails, moduleItemId: Long) : this(
        id = moduleItemId, // This will always be a 1on1 relationship with the moduleItem so we use the same id
        pointsPossible = moduleContentDetails.pointsPossible,
        dueAt = moduleContentDetails.dueAt,
        unlockAt = moduleContentDetails.unlockAt,
        lockAt = moduleContentDetails.lockAt,
        lockedForUser = moduleContentDetails.lockedForUser,
        lockExplanation = moduleContentDetails.lockExplanation,
    )

    fun toApiModel(lockInfo: LockInfo?) = ModuleContentDetails(
        pointsPossible = pointsPossible,
        dueAt = dueAt,
        unlockAt = unlockAt,
        lockAt = lockAt,
        lockedForUser = lockedForUser,
        lockExplanation = lockExplanation,
        lockInfo = lockInfo
    )
}