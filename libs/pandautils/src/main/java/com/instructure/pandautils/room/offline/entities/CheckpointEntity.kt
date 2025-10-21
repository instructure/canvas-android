/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import com.instructure.canvasapi2.managers.graphql.ModuleItemCheckpoint
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.utils.toDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ModuleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["moduleItemId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class CheckpointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assignmentId: Long? = null,
    val name: String?,
    val tag: String?,
    val pointsPossible: Double?,
    val dueAt: String?,
    val onlyVisibleToOverrides: Boolean,
    val lockAt: String?,
    val unlockAt: String?,
    val moduleItemId: Long? = null,
    val courseId: Long? = null
) {
    constructor(checkpoint: Checkpoint, assignmentId: Long) : this(
        assignmentId = assignmentId,
        name = checkpoint.name,
        tag = checkpoint.tag,
        pointsPossible = checkpoint.pointsPossible,
        dueAt = checkpoint.dueAt,
        onlyVisibleToOverrides = checkpoint.onlyVisibleToOverrides,
        lockAt = checkpoint.lockAt,
        unlockAt = checkpoint.unlockAt
    )

    fun toApiModel() = Checkpoint(
        name = name,
        tag = tag,
        pointsPossible = pointsPossible,
        dueAt = dueAt,
        overrides = null,
        onlyVisibleToOverrides = onlyVisibleToOverrides,
        lockAt = lockAt,
        unlockAt = unlockAt
    )

    fun toModuleItemCheckpoint(): ModuleItemCheckpoint {
        return ModuleItemCheckpoint(
            dueAt = dueAt?.toDate(),
            tag = tag.orEmpty(),
            pointsPossible = pointsPossible ?: 0.0
        )
    }
}
