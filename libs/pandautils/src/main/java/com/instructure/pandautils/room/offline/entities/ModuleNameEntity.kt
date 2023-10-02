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
import com.instructure.canvasapi2.models.ModuleName

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = LockedModuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["lockedModuleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ModuleNameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String?,
    val lockedModuleId: Long
) {
    constructor(moduleName: ModuleName, lockedModuleId: Long) : this(
        name = moduleName.name,
        lockedModuleId = lockedModuleId
    )

    fun toApiModel() = ModuleName(
        name = name
    )
}
