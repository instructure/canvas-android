/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.toApiString
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class DiscussionTopicHeader(
        override var id: Long = 0, // Discussion Topic Id;
        @SerializedName("discussion_type")
        var discussionType: String? = null, // Type of discussion (side_comment or threaded).
        var title: String? = null,
        var message: String? = null, // HTML content
        @SerializedName("html_url")
        var htmlUrl: String? = null, // URL to the topic on canvas.
        // Only one of the following two will be filled out. the other will be null.
        // If posted_at isn't null, it represents when the discussion WAS posted.
        // If delayed_post_at isn't null, it represents when the discussion WILL be posted.
        @SerializedName("posted_at")
        var postedDate: Date? = null,
        @SerializedName("created_at")
        var createdDate: Date? = null,
        @SerializedName("delayed_post_at")
        var delayedPostDate: Date? = null,
        @SerializedName("last_reply_at")
        var lastReplyDate: Date? = null,  // Last response to the thread.
        @SerializedName("require_initial_post")
        var requireInitialPost: Boolean = false, // Whether or not users are required to post before they can respond to comments.
        @SerializedName("discussion_subentry_count")
        var discussionSubentryCount: Int = 0, // The count of entries in the topic.
        @SerializedName("read_state")
        var readState: String? = null, // Whether or not the topic has been read yet.
        @SerializedName("unread_count")
        var unreadCount: Int = 0, // Number of unread messages.
        var position: Int = 0, // If topic is pinned it'll have a position
        @SerializedName("assignment_id")
        var assignmentId: Long = 0, // The unique identifier of the assignment if the topic is for grading, otherwise null.
        @SerializedName("locked")
        var locked: Boolean = false, // Whether or not the discussion is 'closed for comments'.
        @SerializedName("locked_for_user")
        var lockedForUser: Boolean = false, // whether or not this is Locked for students to see.
        @SerializedName("lock_explanation")
        var lockExplanation: String? = null, // (Optional) An explanation of why this is Locked for the user. Present when locked_for_user is true.
        var pinned: Boolean = false, // whether or not the discussion has been "pinned" by an instructor
        var author: DiscussionParticipant? = null, // The user that Started the thread.
        @SerializedName("podcast_url")
        var podcastUrl: String? = null, // If the topic is a podcast topic this is the feed url for the current user.
        @SerializedName("group_category_id")
        var groupCategoryId: String? = null,

        // If true, this topic is an announcement. This requires announcement-posting permissions.
        @SerializedName("is_announcement")
        var announcement: Boolean = false,

        @SerializedName("reply_to_entry_required_count")
        val replyRequiredCount: Int? = 0,

        // If the topic is for grading and a group assignment this will
        // point to the original topic in the course.
        // String maybe?
        // NOT USED in our code
        //@SerializedName("root_topic_id")
        //var rootTopicId: Long = 0,

        // A list of topic_ids for the group discussions the user is a part of.
        // NOT USED in our code
        //@SerializedName("topic_children")
        //var topicChildren: List<Long> = ArrayList(),

        // Used primarily to determine whether a discussion is a group discussion
        @SerializedName("group_topic_children")
        var groupTopicChildren: List<GroupTopicChild> = ArrayList(),

        // List of file attachments
        var attachments: MutableList<RemoteFile> = ArrayList(),

        // NOT USED in our code
        //var unauthorized: Boolean = false,
        var permissions: DiscussionTopicPermission? = null,
        var assignment: Assignment? = null,
        @SerializedName("lock_info")
        var lockInfo: LockInfo? = null,
        var published: Boolean = false, // Whether this discussion topic is published (true) or draft state (false)
        @SerializedName("allow_rating")
        var allowRating: Boolean = false, // Whether or not users can rate entries in this topic.
        @SerializedName("only_graders_can_rate")
        var onlyGradersCanRate: Boolean = false, // Whether or not grade permissions are required to rate entries.
        @SerializedName("sort_by_rating")
        var sortByRating: Boolean = false, // Whether or not entries should be sorted by rating.

        @SerializedName("context_code")
        var contextCode: String? = null,

        var subscribed: Boolean = false,
        @SerializedName("lock_at")
        var lockAt: Date? = null,
        @SerializedName("user_can_see_posts")
        var userCanSeePosts: Boolean = true,
        @SerializedName("specific_sections")
        var specificSections: String? = null, // For when we're submitting the sections
        var sections: List<Section>? = null, // Comes back from the server

        @SerializedName("anonymous_state")
        var anonymousState: String? = null,
) : CanvasModel<DiscussionTopicHeader>() {
    override val comparisonDate: Date?
        get() = if (lastReplyDate != null)
            lastReplyDate
        else
            postedDate

    val shouldShowReplies get() = !lockedForUser || lockInfo?.unlockDate?.after(Date()) != true

    var type: DiscussionType
        get() {
            if ("side_comment" == this.discussionType) {
                return DiscussionType.SIDE_COMMENT
            } else if ("threaded" == this.discussionType) {
                return DiscussionType.THREADED
            }
            return DiscussionType.UNKNOWN
        }
        set(type) = when (type) {
            DiscussionType.SIDE_COMMENT -> this.discussionType = "side_comment"
            DiscussionType.THREADED -> this.discussionType = "threaded"
            else -> this.discussionType = "unknown"
        }

    var status: ReadState
        get() = when (readState) {
            "read" -> ReadState.READ
            "unread" -> ReadState.UNREAD
            else -> ReadState.UNREAD
        }
        set(status) = if (status == ReadState.READ) {
            this.readState = "read"
        } else {
            this.readState = "unread"
        }

    enum class ReadState {
        READ, UNREAD
    }

    enum class DiscussionType(val apiString: String) {
        UNKNOWN("unknown"),
        SIDE_COMMENT("side_comment"),
        THREADED("threaded")
    }

    fun convertToDiscussionEntry(localizedGradedDiscussion: String, localizedPointsPossible: String): DiscussionEntry {
        val discussionEntry = DiscussionEntry()
        discussionEntry.message = this.message
        discussionEntry.parent = null
        discussionEntry.parentId = -1L
        discussionEntry.replies = ArrayList()

        var description = ""
        if (assignment != null) {
            description = localizedGradedDiscussion
            if (assignment!!.pointsPossible > 0)
                description += "<br>" + java.lang.Double.toString(assignment!!.pointsPossible) + " " + localizedPointsPossible
        }
        discussionEntry.description = description

        discussionEntry.message = this.message

        when {
            this.lastReplyDate != null -> discussionEntry.updatedAt = this.lastReplyDate.toApiString()
            this.postedDate != null -> discussionEntry.updatedAt = this.postedDate.toApiString()
            else -> discussionEntry.updatedAt = this.delayedPostDate.toApiString()
        }

        discussionEntry.author = author
        discussionEntry.attachments = this.attachments
        discussionEntry.unread = this.status == ReadState.UNREAD

        return discussionEntry
    }

    fun incrementDiscussionSubentryCount() {
        discussionSubentryCount += 1
    }

    fun decrementDiscussionSubentryCount() {
        discussionSubentryCount -= 1
        if (discussionSubentryCount < 0) {
            discussionSubentryCount = 0
        }
    }
}
