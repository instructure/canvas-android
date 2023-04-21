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
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = DiscussionParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class DiscussionTopicHeaderEntity(
    @PrimaryKey
    val id: Long,
    var discussionType: String?,
    var title: String?,
    var message: String?,
    var htmlUrl: String?,
    var postedDate: Date?,
    var delayedPostDate: Date?,
    var lastReplyDate: Date?,
    var requireInitialPost: Boolean,
    var discussionSubentryCount: Int,
    var readState: String?,
    var unreadCount: Int,
    var position: Int,
    var assignmentId: Long?,
    var locked: Boolean,
    var lockedForUser: Boolean,
    var lockExplanation: String?,
    var pinned: Boolean,
    var authorId: Long?,
    var podcastUrl: String?,
    var groupCategoryId: String?,
    var announcement: Boolean,
    //TODO var groupTopicChildren: List<GroupTopicChild>,
    // TODO var lockInfo: LockInfo?,
    var published: Boolean,
    var allowRating: Boolean,
    var onlyGradersCanRate: Boolean,
    var sortByRating: Boolean,
    var subscribed: Boolean,
    var lockAt: Date?,
    var userCanSeePosts: Boolean,
    var specificSections: String?,
    var anonymousState: String?
)