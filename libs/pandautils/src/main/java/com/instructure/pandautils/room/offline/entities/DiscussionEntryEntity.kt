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
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant

@Entity
data class DiscussionEntryEntity(
    @PrimaryKey
    val id: Long,
    var unread: Boolean,
    var updatedAt: String?,
    val createdAt: String?,
    var authorId: Long?,
    var description: String?,
    val userId: Long,
    var parentId: Long,
    var message: String?,
    var deleted: Boolean,
    var totalChildren: Int,
    var unreadChildren: Int,
    val ratingCount: Int,
    var ratingSum: Int,
    val editorId: Long,
    var _hasRated: Boolean,
    var replyIds: List<Long>,
) {
    constructor(discussionEntry: DiscussionEntry, replyIds: List<Long> = emptyList()): this(
        discussionEntry.id,
        discussionEntry.unread,
        discussionEntry.updatedAt,
        discussionEntry.createdAt,
        discussionEntry.author?.id,
        discussionEntry.description,
        discussionEntry.userId,
        discussionEntry.parentId,
        discussionEntry.message,
        discussionEntry.deleted,
        discussionEntry.totalChildren,
        discussionEntry.unreadChildren,
        discussionEntry.ratingCount,
        discussionEntry.ratingSum,
        discussionEntry.editorId,
        discussionEntry._hasRated,
        replyIds,
    )

    fun toApiModel(author: DiscussionParticipant? = null, replyDiscussionEntries: MutableList<DiscussionEntry> = mutableListOf()): DiscussionEntry {
        return DiscussionEntry(
            id = id,
            unread = unread,
            updatedAt = updatedAt,
            createdAt = createdAt,
            author = author,
            description = description,
            userId = userId,
            parentId = parentId,
            message = message,
            deleted = deleted,
            totalChildren = totalChildren,
            unreadChildren = unreadChildren,
            ratingCount = ratingCount,
            ratingSum = ratingSum,
            editorId = editorId,
            _hasRated = _hasRated,
            replies = replyDiscussionEntries,
        )
    }
}