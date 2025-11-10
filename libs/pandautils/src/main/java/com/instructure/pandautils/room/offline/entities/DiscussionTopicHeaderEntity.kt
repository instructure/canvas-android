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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.utils.orDefault
import java.util.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = DiscussionParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = DiscussionTopicPermissionEntity::class,
            parentColumns = ["id"],
            childColumns = ["permissionId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class DiscussionTopicHeaderEntity(
    @PrimaryKey
    val id: Long,
    val courseId: Long,
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
    var permissionId: Long?,
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
    var anonymousState: String?,
    var replyRequiredCount: Int?
) {
    constructor(discussionTopicHeader: DiscussionTopicHeader, courseId: Long, permissionId: Long? = null) : this(
        discussionTopicHeader.id,
        courseId,
        discussionTopicHeader.discussionType,
        discussionTopicHeader.title,
        discussionTopicHeader.message,
        discussionTopicHeader.htmlUrl,
        discussionTopicHeader.postedDate,
        discussionTopicHeader.delayedPostDate,
        discussionTopicHeader.lastReplyDate,
        discussionTopicHeader.requireInitialPost,
        discussionTopicHeader.discussionSubentryCount,
        discussionTopicHeader.readState,
        discussionTopicHeader.unreadCount,
        discussionTopicHeader.position,
        discussionTopicHeader.assignmentId,
        discussionTopicHeader.locked,
        discussionTopicHeader.lockedForUser,
        discussionTopicHeader.lockExplanation,
        discussionTopicHeader.pinned,
        discussionTopicHeader.author?.id,
        discussionTopicHeader.podcastUrl,
        discussionTopicHeader.groupCategoryId,
        discussionTopicHeader.announcement,
        permissionId,
        discussionTopicHeader.published,
        discussionTopicHeader.allowRating,
        discussionTopicHeader.onlyGradersCanRate,
        discussionTopicHeader.sortByRating,
        discussionTopicHeader.subscribed,
        discussionTopicHeader.lockAt,
        discussionTopicHeader.userCanSeePosts,
        discussionTopicHeader.specificSections,
        discussionTopicHeader.anonymousState,
        discussionTopicHeader.replyRequiredCount
    )

    fun toApiModel(
        author: DiscussionParticipant? = null,
        assignment: Assignment? = null,
        permissions: DiscussionTopicPermission? = null,
        attachments: List<RemoteFile>
    ) = DiscussionTopicHeader(
        id = id,
        discussionType = discussionType,
        title = title,
        message = message,
        htmlUrl = htmlUrl,
        postedDate = postedDate,
        delayedPostDate = delayedPostDate,
        lastReplyDate = lastReplyDate,
        requireInitialPost = requireInitialPost,
        discussionSubentryCount = discussionSubentryCount,
        readState = readState,
        unreadCount = unreadCount,
        position = position,
        assignmentId = assignmentId.orDefault(),
        locked = locked,
        lockedForUser = lockedForUser,
        lockExplanation = lockExplanation,
        pinned = pinned,
        author = author,
        podcastUrl = podcastUrl,
        groupCategoryId = groupCategoryId,
        announcement = announcement,
        groupTopicChildren = emptyList(),
        attachments = attachments.toMutableList(),
        //TODO
        permissions = permissions,
        assignment = assignment,
        //TODO
        lockInfo = null,
        published = published,
        allowRating = allowRating,
        onlyGradersCanRate = onlyGradersCanRate,
        sortByRating = sortByRating,
        subscribed = subscribed,
        lockAt = lockAt,
        userCanSeePosts = userCanSeePosts,
        specificSections = specificSections,
        //TODO
        sections = null,
        anonymousState = anonymousState,
        replyRequiredCount = replyRequiredCount
    )
}