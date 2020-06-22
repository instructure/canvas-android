/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.logic

import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

//
// region DiscussionTopicPermission support
//

fun LambdaDslObject.populateDiscussionTopicPermissionFields() : LambdaDslObject {
    this
            .booleanType("attach")
            .booleanType("update")
            .booleanType("delete")
            .booleanType("reply")

    return this
}

fun assertDiscussionTopicPermissionPopulated(description: String, permission: DiscussionTopicPermission) {
    assertNotNull("$description + attach", permission.attach)
    assertNotNull("$description + update", permission.update)
    assertNotNull("$description + delete", permission.delete)
    assertNotNull("$description + reply", permission.reply)
}
//endregion

//
// region DiscussionParticipant support
//
// Very similar to Author, but we type-specific support for population assertion
//

fun LambdaDslObject.populateDiscussionParticipantFields() : LambdaDslObject {
    this

            .id("id")
            .stringType("display_name")
            .stringType("pronouns")
            .stringType("avatar_image_url")
            .stringType("html_url")

    return this
}

fun assertDiscussionParticipantPopulated(description: String, participant: DiscussionParticipant) {
    assertNotNull("$description + id", participant.id)
    assertNotNull("$description + id", participant.displayName)
    assertNotNull("$description + id", participant.pronouns)
    assertNotNull("$description + id", participant.avatarImageUrl)
    assertNotNull("$description + id", participant.htmlUrl)
}

// endregion

//
// region DiscussionTopicHeader support
//

data class DiscussionTopicHeaderConfig(
        val hasAssignment : Boolean = false,
        val hasSections : Boolean = false,
        val isDelayed: Boolean = false,
        val hasRequireInitialPost: Boolean = false,
        val isLocked: Boolean = false
)
fun LambdaDslObject.populateDiscussionTopicHeaderFields(config: DiscussionTopicHeaderConfig = DiscussionTopicHeaderConfig()): LambdaDslObject {
    this
            .id("id")
            .stringMatcher("discussion_type", "side_comment|threaded", "threaded")
            .stringType("title")
            .stringType("message")
            .stringType("html_url")
            .stringMatcher("posted_at", PACT_DATE_REGEX, "2020-01-23")
            .stringMatcher("last_reply_at", PACT_DATE_REGEX, "2020-01-23")
            .id("discussion_subentry_count")
            .stringMatcher("read_state", "read|unread", "read")
            .id("unread_count")
            .id("position")
            .booleanType("pinned")
            .`object`("author") { obj ->
                obj.populateDiscussionParticipantFields()
            }
            .stringType("podcast_url")
            //.stringType("group_category_id") // PUNT
            //.booleanType("is_announcement") // PUNT
    // TODO: group_topic_children
    // TODO: attachments
            .`object`("permissions") { obj ->
                obj.populateDiscussionTopicPermissionFields()
            }
            .booleanType("published")
            .booleanType("allow_rating")
            .booleanType("only_graders_can_rate")
            .booleanType("sort_by_rating")
            .booleanType("subscribed")
            .stringMatcher("lock_at", PACT_DATE_REGEX, "2020-01-23")
            .booleanType("user_can_see_posts")
            //.stringType("specific_sections") // PUNT

    if(config.isDelayed) {
        this.stringMatcher("delayed_post_at", PACT_DATE_REGEX, "2020-01-23")
    }

    if(config.hasSections) {
        this.minArrayLike("sections",1) { obj ->
            obj.populateSectionFields()
        }
    }

    if(config.hasAssignment) {
        this
                .id("assignment_id")
                .`object`("assignment") { obj ->
                    // Abbreviated requirements for assignment field inclusion
                    obj
                            .id("id")
                            .stringType("name")
                            .id("course_id")
                            .numberType("points_possible")
                }
    }

    if(config.hasRequireInitialPost) {
        this.booleanType("require_initial_post")
    }

    if(config.isLocked) {
        this
                .booleanType("locked")
                .booleanType("locked_for_user")
                .stringType("lock_explanation")
                .`object`("lock_info") { obj ->
                    obj.stringMatcher("unlock_at",PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
                    obj.stringType("asset_string")
                }
    }

    return this

}

fun assertDiscussionTopicHeaderPopulated(description: String, header: DiscussionTopicHeader, config: DiscussionTopicHeaderConfig = DiscussionTopicHeaderConfig()) {
    assertNotNull("$description + id", header.id)
    assertNotNull("$description + discussionType", header.discussionType)
    assertNotNull("$description + message", header.message)
    assertNotNull("$description + htmlUrl", header.htmlUrl)
    assertNotNull("$description + postedDate", header.postedDate)
    assertNotNull("$description + lastReplyDate", header.lastReplyDate)
    assertNotNull("$description + discussionSubentryCount", header.discussionSubentryCount)
    assertNotNull("$description + readState", header.readState)
    assertNotNull("$description + unreadCount", header.unreadCount)
    assertNotNull("$description + position", header.position)
    assertNotNull("$description + pinned", header.pinned)
    assertNotNull("$description + author", header.author)
    assertDiscussionParticipantPopulated("$description + author", header.author!!)
    assertNotNull("$description + podcastUrl", header.podcastUrl)
    //assertNotNull("$description + groupCategoryId", header.groupCategoryId) // PUNT
    //assertNotNull("$description + announcement", header.announcement) // PUNT
    assertNotNull("$description + permissions", header.permissions)
    assertDiscussionTopicPermissionPopulated("$description + permissions", header.permissions!!)
    assertNotNull("$description + published", header.published)
    assertNotNull("$description + allowRating", header.allowRating)
    assertNotNull("$description + onlyGradersCanRate", header.onlyGradersCanRate)
    assertNotNull("$description + sortByRating", header.sortByRating)
    assertNotNull("$description + subscribed", header.subscribed)
    assertNotNull("$description + lockAt", header.lockAt)
    assertNotNull("$description + userCanSeePosts", header.userCanSeePosts)
    //assertNotNull("$description + specificSections", header.specificSections) // PUNT

    if(config.isLocked) {
        assertNotNull("$description + locked", header.locked)
        assertNotNull("$description + lockedForUser", header.lockedForUser)
        assertNotNull("$description + lockExplanation", header.lockExplanation)
        assertNotNull("$description + lockInfo", header.lockInfo)
    }

    if(config.hasRequireInitialPost) {
        assertNotNull("$description + requireInitialPost", header.requireInitialPost)
    }

    if(config.hasAssignment) {
        assertNotNull("$description + assignmentId", header.assignmentId)
        assertNotNull("$description + assignment", header.assignment)
        assertNotNull("$description + assignment.id", header.assignment!!.id)
        assertNotNull("$description + assignment.name", header.assignment!!.name)
        assertNotNull("$description + assignment.courseId", header.assignment!!.courseId)
        assertNotNull("$description + assignment.pointsPossible", header.assignment!!.pointsPossible)
    }

    if(config.hasSections) {
        assertNotNull("$description + sections", header.sections)
        assertTrue("$description + sections: Expected at least one section", header.sections!!.size > 0)
        assertSectionPopulated("$description + sections[0]", header.sections!![0])
    }

    if(config.isDelayed) {
        assertNotNull("$description + delayedPostDate", header.delayedPostDate)
    }
}
// endregion

//
// region DiscussionEntry support
//

fun LambdaDslObject.populateDiscussionEntryFields(hasRatings: Boolean = false, hasReply: Boolean = false) : LambdaDslObject {

    // Base case
    this
            .id("id")
            .stringMatcher("updated_at",PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("created_at",PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .id("user_id")
            .stringType("message")
    // TODO: attachments
    // TODO: parent_id -- would show up in a reply

    if(hasRatings) {
        this
                .id("rating_count")
                .id("rating_sum")
    }


    if(hasReply) {
        this.minArrayLike("replies",1) { obj ->
            obj.populateDiscussionEntryFields()
        }
    }

    return this
}

fun assertDiscussionEntryPopulated(description: String, entry: DiscussionEntry, hasRatings: Boolean = true, hasReply: Boolean = false) {

    assertNotNull("$description + id", entry.id)
    assertNotNull("$description + updatedAt", entry.updatedAt)
    assertNotNull("$description + createdAt", entry.createdAt)
    assertNotNull("$description + userId", entry.userId)
    assertNotNull("$description + message", entry.message)

    if(hasRatings) {
        assertNotNull("$description + ratingCount", entry.ratingCount)
        assertNotNull("$description + ratingSum", entry.ratingSum)
    }
    if(hasReply) {
        assertNotNull("$description + replies", entry.replies)
        assertTrue("$description + replies: Expected at least one reply", entry.replies!!.size >= 1)
    }
}
// endregion

//
// region DiscussionTopic support
//

fun LambdaDslObject.populateDiscussionTopicFields() : LambdaDslObject {
    this
            //.booleanType("forbidden")
            .minArrayLike("unread_entries", 1, PactDslJsonRootValue.integerType(), 1)
            .minArrayLike("participants", 1) { obj ->
                obj.populateDiscussionParticipantFields()
            }
            .minArrayLike("view",1) { obj ->
                obj.populateDiscussionEntryFields(hasRatings = true)
            }
    // TODO: unread_entriesMap, entry_ratings: Not sure how to specify a map with non-string key in Pact

    return this
}

fun assertDiscussionTopicPopulated(description: String, topic: DiscussionTopic) {
    //assertNotNull("$description + isForbidden", topic.isForbidden)
    assertNotNull("$description + unreadEntries", topic.unreadEntries)
    assertTrue("$description + unreadEntries: Expected at least one", topic.unreadEntries.size > 0)
    assertNotNull("$description + participants", topic.participants)
    assertTrue("$description + participants: Expected at least one", topic.participants!!.size > 0)
    for(i in 0..topic.participants!!.size-1) {
        assertDiscussionParticipantPopulated("$description + participants[$i]", topic.participants!![i])
    }
    assertNotNull("$description + views", topic.views)
    assertTrue("$description + views: Expected at least one", topic.views.size > 0)
    for(i in 0..topic.views.size-1) {
        assertDiscussionEntryPopulated("$description + views[$i]", topic.views[i], hasRatings = true)
    }
}

// endregion