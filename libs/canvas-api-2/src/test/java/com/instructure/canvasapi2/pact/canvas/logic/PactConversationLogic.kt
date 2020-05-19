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
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.Message
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

//
// region MediaComment object support
//

fun LambdaDslObject.populateMediaCommentFields() : LambdaDslObject {
    this
            .stringType("media_id")
            .stringType("display_name")
            .stringType("url")
            .stringMatcher("media_type", "audio|video", "audio")
            .stringType("content-type")

    return this
}

fun assertMediaCommentPopulated(description: String, mc: MediaComment) {
    assertNotNull("$description + mediaId", mc.mediaId)
    assertNotNull("$description + displayName", mc.displayName)
    assertNotNull("$description + url", mc.url)
    assertNotNull("$description + mediaType", mc.mediaType)
    assertNotNull("$description + contentType", mc.contentType)
}
//endregion

//
// region BasicUser object support
//

fun LambdaDslObject.populateBasicUserFields() : LambdaDslObject {
    this
            .id("id")
            .stringType("name")
            .stringType("avatar_url")
            .stringType("pronouns")

    return this
}

fun assertBasicUserPopulated(description: String, basicUser: BasicUser) {
    assertNotNull("$description + id", basicUser.id)
    assertNotNull("$description + name", basicUser.name)
    assertNotNull("$description + avatarUrl", basicUser.avatarUrl)
    assertNotNull("$description + pronouns", basicUser.pronouns)
}
// endregion

//
// region Message object support
//

fun LambdaDslObject.populateMessageFields(
        includeMediaComment: Boolean = false,
        includeAttachments: Boolean = false
) : LambdaDslObject {

    this
            .id("id")
            .stringMatcher("created_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringType("body")
            .id("author_id")
            .booleanType("generated")
    // TODO: forwarded_messages
            .minArrayLike("participating_user_ids", 1, PactDslJsonRootValue.id(), 1)

    if(includeMediaComment) {
        this.`object`("media_comment") { obj ->
            obj.populateMediaCommentFields()
        }
    }

    if(includeAttachments) {
        this.minArrayLike("attachments", 1) { obj ->
            obj.populateAttachmentFields()
        }
    }
    return this
}

fun assertMessagePopulated(
        description: String,
        message: Message,
        includeMediaComment: Boolean = false,
        includeAttachments: Boolean = false
) {
    assertNotNull("$description + id", message.id)
    assertNotNull("$description + createdAt", message.createdAt)
    assertNotNull("$description + body", message.body)
    assertNotNull("$description + authorId", message.authorId)
    assertNotNull("$description + isGenerated", message.isGenerated)
    assertNotNull("$description + participatingUserIds", message.participatingUserIds)
    if(includeMediaComment) {
        assertNotNull("$description + participatingUserIds", message.mediaComment)
        assertMediaCommentPopulated("$description + mediaComment", message.mediaComment!!)
    }
    if(includeAttachments) {
        assertNotNull("$description + attachments", message.attachments)
        assertTrue("$description: Expected at least one attachment", message.attachments.size > 0)
        for(i in 0..message.attachments.size-1) {
            assertAttachmentPopulated("$description + attachments[$i]", message.attachments[i])
        }
    }
}
// endregion

//
// region Conversation object support
//

/**
 * Pact Conversation object configuration instructions
 *
 * [includeMessages] should only be set to true if you are retrieving a specific conversation or
 * adding a message to an existing conversation.
 * Set [starred] to a non-null value if you want to check for a specific value in the starred field.
 * Set [state] to a non-null value if you want to check for a specific value in the workflow_state field.
 * [includeContextName] should be set to false for non-GET operations (PUT, POST, etc...)
 * [includeMediaComment] and [includeAttachments] are passed through to the message object
 * if [includeMessages] is true.
 * If [includeLastMessageData] is true, then we will check for fields last_message and
 * last_message_at.  Only set to false for createConversation call.
 */
data class PactConversationFieldConfig (
        val includeMessages: Boolean = false,
        val starred: Boolean? = null, // null means "don't care about the value"
        val state: String? = null, // null means "don't care about the value"
        val includeContextName: Boolean = true,
        val includeMediaComment: Boolean = false, // Only meaningful if includeMessages is true
        val includeAttachments: Boolean = false, // Only meaningful is includeMessages is true
        val includeLastMessageData: Boolean = true
)

fun LambdaDslObject.populateConversationFields(fieldConfig: PactConversationFieldConfig) : LambdaDslObject {

    this
            .id("id")
            .stringType("subject")
            .stringMatcher("last_authored_message_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .id("message_count")
            .booleanType("subscribed")
            .minArrayLike("properties", 1, PactDslJsonRootValue.stringType(), 1)
            .stringType("avatar_url")
            .booleanType("visible")
            .minArrayLike("audience", 1, PactDslJsonRootValue.id(), 1)
            .minArrayLike("participants", 1) { obj ->
                obj.populateBasicUserFields()
            }
            .stringType("context_code")

    if(fieldConfig.includeLastMessageData) {
        this
                .stringType("last_message")
                .stringMatcher("last_message_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
    }

    if(fieldConfig.includeContextName) {
        this.stringType("context_name")
    }

    if(fieldConfig.starred != null) {
        this.booleanValue("starred", fieldConfig.starred)
    }
    else {
        this.booleanType("starred")
    }

    if(fieldConfig.state != null) {
        this.stringValue("workflow_state", fieldConfig.state)
    }
    else {
        this.stringMatcher("workflow_state", "read|unread|archived", "read")
    }

    if(fieldConfig.includeMessages) {
        this.minArrayLike("messages", 1) { obj ->
            obj.populateMessageFields(fieldConfig.includeMediaComment, fieldConfig.includeAttachments)
        }
    }
    return this
}

fun assertConversationPopulated(
        description: String,
        conversation: Conversation,
        fieldConfig: PactConversationFieldConfig
) {
    assertNotNull("$description + id", conversation.id)
    assertNotNull("$description + subject", conversation.subject)
    assertNotNull("$description + workflowState", conversation.workflowState)
    if(fieldConfig.state != null) {
        assertEquals("workflowState value", fieldConfig.state, conversation.workflowState!!.apiString)
    }
    assertNotNull("$description + lastAuthoredMessageAt", conversation.lastAuthoredMessageAt)
    assertNotNull("$description + messageCount", conversation.messageCount)
    assertNotNull("$description + isSubscribed", conversation.isSubscribed)
    assertNotNull("$description + isStarred", conversation.isStarred)
    if(fieldConfig.starred != null) {
        assertEquals("starred value", fieldConfig.starred, conversation.isStarred)
    }
    assertNotNull("$description + properties", conversation.properties)
    assertNotNull("$description + avatarUrl", conversation.avatarUrl)
    assertNotNull("$description + isVisible", conversation.isVisible)
    assertNotNull("$description + audience", conversation.audience)
    assertNotNull("$description + participants", conversation.participants)
    for(i in 0..conversation.participants.size-1) {
        assertBasicUserPopulated("$description + participants[$i]", conversation.participants[i])
    }
    assertNotNull("$description + contextCode", conversation.contextCode)
    if(fieldConfig.includeLastMessageData) {
        assertNotNull("$description + lastMessage", conversation.lastMessage)
        assertNotNull("$description + lastMessageAt", conversation.lastMessageAt)
    }
    if(fieldConfig.includeContextName) {
        assertNotNull("$description + contextName", conversation.contextName)
    }

    if(fieldConfig.includeMessages) {
        assertNotNull("$description + messages", conversation.messages)
        for (i in 0..conversation.messages.size - 1) {
            assertMessagePopulated(
                    "$description + messages[$i]",
                    conversation.messages[i],
                    fieldConfig.includeMediaComment,
                    fieldConfig.includeAttachments
            )
        }
    }

}
//endregion