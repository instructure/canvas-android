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
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject

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
data class ModuleObjectEntity(
    @PrimaryKey
    val id: Long,
    val position: Int,
    val name: String?,
    val unlockAt: String? = null,
    val sequentialProgress: Boolean = false,
    val prerequisiteIds: LongArray? = null,
    val state: String? = null,
    val completedAt: String? = null,
    val published: Boolean? = null,
    val itemCount: Int = 0,
    val itemsUrl: String = "",
    val courseId: Long
) {
    constructor(module: ModuleObject, courseId: Long) : this(
        id = module.id,
        position = module.position,
        name = module.name,
        unlockAt = module.unlockAt,
        sequentialProgress = module.sequentialProgress,
        prerequisiteIds = module.prerequisiteIds,
        state = module.state,
        completedAt = module.completedAt,
        published = module.published,
        itemCount = module.itemCount,
        itemsUrl = module.itemsUrl,
        courseId = courseId
    )

    fun toApiModel(items: List<ModuleItem>) = ModuleObject(
        id = id,
        position = position,
        name = name,
        unlockAt = unlockAt,
        sequentialProgress = sequentialProgress,
        prerequisiteIds = prerequisiteIds,
        state = state,
        completedAt = completedAt,
        published = published,
        itemCount = itemCount,
        itemsUrl = itemsUrl,
        items = items
    )
}