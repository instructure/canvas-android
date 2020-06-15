package com.instructure.canvasapi2.pact.canvas.logic

import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

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

fun LambdaDslObject.populateDiscussionTopicHeaderFields(): LambdaDslObject {
    this
            .id("id")
            .stringMatcher("discussion_type", "side_comment|threaded", "threaded")
            .stringType("title")
            .stringType("message")
            .stringType("html_url")
            .stringMatcher("posted_at", PACT_DATE_REGEX, "2020-01-23") // TODO: Only one of these two is populated
            .stringMatcher("delayed_post_at", PACT_DATE_REGEX, "2020-01-23")
            .stringMatcher("last_reply_at", PACT_DATE_REGEX, "2020-01-23")
            .booleanType("require_initial_post")
            .id("discussion_subentry_count")
            .stringMatcher("read_state", "read|unread", "read")
            .id("unread_count")
            .id("position")
            .id("assignment_id")
            .booleanType("locked")
            .booleanType("locked_for_user")
            .stringType("lock_explanation")
            .booleanType("pinned")
            .`object`("author") { obj ->
                obj.populateAuthorFields()
            }
            .stringType("podcast_url")
            .stringType("group_category_id")
            .booleanType("is_announcement")
    // TODO: group_topic_children
    // TODO: attachments
            .`object`("permissions") { obj ->
                obj.populateDiscussionTopicPermissionFields()
            }
    // TODO: assignment
    // TODO: lock_info
            .booleanType("published")
            .booleanType("allow_rating")
            .booleanType("only_graders_can_rate")
            .booleanType("sort_by_rating")
            .booleanType("subscribed")
            .stringMatcher("lock_at", PACT_DATE_REGEX, "2020-01-23")
            .booleanType("user_can_see_posts")
            .stringType("specific_sections")
            .minArrayLike("sections",1) { obj ->
                obj.populateSectionFields()
            }

    return this

}

fun assertDiscussionTopicHeaderPopulated(description: String, header: DiscussionTopicHeader) {
    assertNotNull("$description + id", header.id)
    assertNotNull("$description + discussionType", header.discussionType)
    assertNotNull("$description + message", header.message)
    assertNotNull("$description + htmlUrl", header.htmlUrl)
    assertNotNull("$description + postedDate", header.postedDate)
    assertNotNull("$description + delayedPostDate", header.delayedPostDate)
    assertNotNull("$description + lastReplyDate", header.lastReplyDate)
    assertNotNull("$description + requireInitialPost", header.requireInitialPost)
    assertNotNull("$description + discussionSubentryCount", header.discussionSubentryCount)
    assertNotNull("$description + readState", header.readState)
    assertNotNull("$description + unreadCount", header.unreadCount)
    assertNotNull("$description + position", header.position)
    assertNotNull("$description + assignmentId", header.assignmentId)
    assertNotNull("$description + locked", header.locked)
    assertNotNull("$description + lockedForUser", header.lockedForUser)
    assertNotNull("$description + lockExplanation", header.lockExplanation)
    assertNotNull("$description + pinned", header.pinned)
    assertNotNull("$description + author", header.author)
    //assertAuthorPopulated("$description + author", header.author!!) // TODO
    assertNotNull("$description + podcastUrl", header.podcastUrl)
    assertNotNull("$description + groupCategoryId", header.groupCategoryId)
    assertNotNull("$description + announcement", header.announcement)
    assertNotNull("$description + permissions", header.permissions)
    assertDiscussionTopicPermissionPopulated("$description + permissions", header.permissions!!)
    assertNotNull("$description + published", header.published)
    assertNotNull("$description + allowRating", header.allowRating)
    assertNotNull("$description + onlyGradersCanRate", header.onlyGradersCanRate)
    assertNotNull("$description + sortByRating", header.sortByRating)
    assertNotNull("$description + subscribed", header.subscribed)
    assertNotNull("$description + lockAt", header.lockAt)
    assertNotNull("$description + userCanSeePosts", header.userCanSeePosts)
    assertNotNull("$description + specificSections", header.specificSections)
    assertNotNull("$description + sections", header.sections)
}

fun LambdaDslObject.populateDiscussionEntryFields(hasReply: Boolean = false) : LambdaDslObject {
    this
            .id("id")
            .booleanType("unread")
            .stringMatcher("updated_at",PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("created_at",PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .`object`("author") { obj ->
                obj.populateAuthorFields() // Pretty much the same as DiscussionParticipant
            }
            .stringType("description")
            .id("user_id")
            .id("parent_id")
            .stringType("message")
            .booleanType("deleted")
            .id("total_children") // "id" is good for int fields
            .id("unread_children")
    // TODO: attachments
            .id("rating_count")
            .id("rating_sum")
            .stringType("user_name")
            .id("editor_id")
            .booleanType("_hasRated")

    if(hasReply) {
        this.minArrayLike("replies",1) { obj ->
            obj.populateDiscussionEntryFields()
        }
    }

    return this
}

fun assertDiscussionEntryPopulated(description: String, entry: DiscussionEntry, hasReply: Boolean = false) {

    assertNotNull("$description + id", entry.id)
    assertNotNull("$description + unread", entry.unread)
    assertNotNull("$description + updatedAt", entry.updatedAt)
    assertNotNull("$description + createdAt", entry.createdAt)
    assertNotNull("$description + author", entry.author)
    assertNotNull("$description + description", entry.description)
    assertNotNull("$description + userId", entry.userId)
    assertNotNull("$description + parentId", entry.parentId)
    assertNotNull("$description + message", entry.message)
    assertNotNull("$description + deleted", entry.deleted)
    assertNotNull("$description + totalChildren", entry.totalChildren)
    assertNotNull("$description + unreadChildren", entry.unreadChildren)
    assertNotNull("$description + ratingCount", entry.ratingCount)
    assertNotNull("$description + ratingSum", entry.ratingSum)
    assertNotNull("$description + userName", entry.userName)
    assertNotNull("$description + editorId", entry.editorId)
    assertNotNull("$description + _hasRated", entry._hasRated)
    if(hasReply) {
        assertNotNull("$description + replies", entry.replies)
        assertTrue("$description + replies: Expected at least one reply", entry.replies!!.size >= 1)
    }
}


fun LambdaDslObject.populateDiscussionTopicFields() : LambdaDslObject {
    this
            .booleanType("forbidden")
            .minArrayLike("unread_entries", 1, PactDslJsonRootValue.integerType(), 1)
            .minArrayLike("participants", 1) { obj ->
                obj.populateAuthorFields() // Supposed to be DiscussionParticipant, but Author is the same
            }
            .minArrayLike("view",1) { obj ->
                obj.populateDiscussionEntryFields()
            }
    // TODO: unread_entriesMap, entry_ratings: Not sure how to specify a map with non-string key in Pact

    return this
}

fun assertDiscussionTopicPopulated(description: String, topic: DiscussionTopic) {
    assertNotNull("$description + isForbidden", topic.isForbidden)
    assertNotNull("$description + unreadEntries", topic.unreadEntries)
    assertTrue("$description + unreadEntries: Expected at least one", topic.unreadEntries.size > 0)
    assertNotNull("$description + participants", topic.participants)
    assertTrue("$description + participants: Expected at least one", topic.participants!!.size > 0)
//    for(i in 0..topic.participants!!.size-1) {
//        // TODO
//    }
    assertNotNull("$description + views", topic.views)
    assertTrue("$description + views: Expected at least one", topic.views.size > 0)
    for(i in 0..topic.views.size-1) {
        assertDiscussionEntryPopulated("$description + views[$i]", topic.views[i])
    }

}