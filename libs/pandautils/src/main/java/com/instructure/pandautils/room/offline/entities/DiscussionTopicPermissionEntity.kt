/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
import com.instructure.canvasapi2.models.DiscussionTopicPermission

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = DiscussionTopicHeaderEntity::class,
            parentColumns = ["id"],
            childColumns = ["discussionTopicHeaderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DiscussionTopicPermissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val discussionTopicHeaderId: Long,
    val attach: Boolean,
    val update: Boolean,
    val delete: Boolean,
    val reply: Boolean
) {
    constructor(permission: DiscussionTopicPermission, discussionTopicHeaderId: Long) : this(
        0,
        discussionTopicHeaderId,
        permission.attach,
        permission.update,
        permission.delete,
        permission.reply
    )

    fun toApiModel(): DiscussionTopicPermission {
        return DiscussionTopicPermission(
            attach,
            update,
            delete,
            reply
        )
    }
}