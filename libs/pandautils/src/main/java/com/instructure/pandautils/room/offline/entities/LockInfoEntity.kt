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
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.LockedModule

@Entity
data class LockInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modulePrerequisiteNames: List<String>?,
    val unlockAt: String?,
    val assignmentId: Long?,
    val moduleId: Long?,
    val pageId: Long?
) {
    constructor(lockInfo: LockInfo, assignmentId: Long? = null, moduleId: Long? = null, pageId: Long?) : this(
        modulePrerequisiteNames = lockInfo.modulePrerequisiteNames,
        unlockAt = lockInfo.unlockAt,
        assignmentId = assignmentId,
        moduleId = moduleId,
        pageId = pageId
    )

    fun toApiModel(
        lockedModule: LockedModule? = null
    ) = LockInfo(
        modulePrerequisiteNames = ArrayList(modulePrerequisiteNames.orEmpty()),
        contextModule = lockedModule,
        unlockAt = unlockAt
    )
}
