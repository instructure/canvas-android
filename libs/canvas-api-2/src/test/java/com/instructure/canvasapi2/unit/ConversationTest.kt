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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Conversation.WorkflowState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*


class ConversationTest {

    @Test
    fun getWorkflowState_unread() {
        val conversation = Conversation(workflowState = WorkflowState.UNREAD)

        assertEquals(WorkflowState.UNREAD, conversation.workflowState)
    }

    @Test
    fun getWorkflowState_archived() {
        val conversation = Conversation(workflowState = WorkflowState.ARCHIVED)

        assertEquals(WorkflowState.ARCHIVED, conversation.workflowState)
    }

    @Test
    fun getWorkflowState_read() {
        val conversation = Conversation(workflowState = WorkflowState.READ)

        assertEquals(WorkflowState.READ, conversation.workflowState)
    }

    @Test
    fun getWorkflowState_unknown() {
        val conversation = Conversation()

        assertEquals(WorkflowState.UNKNOWN, conversation.workflowState)
    }

    @Test
    fun hasAttachments() {
        val properties = arrayListOf("attachments")
        val conversation = Conversation(properties = properties)

        assertEquals(true, conversation.hasAttachments())
    }

    @Test
    fun hasMedia() {
        val properties = arrayListOf("media_objects")
        val conversation = Conversation(properties = properties)

        assertEquals(true, conversation.hasMedia())
    }

    @Test
    fun getLastMessagePreview() {
        val conversation = Conversation(lastMessage = "last message")
        conversation.deletedString = "conversation deleted"
        conversation.isDeleted = false

        assertEquals("last message", conversation.lastMessagePreview)
    }

    @Test
    fun getLastMessagePreview_deleted() {
        val conversation = Conversation(lastMessage = "last message")
        conversation.deletedString = "conversation deleted"
        conversation.isDeleted = true

        assertEquals("conversation deleted", conversation.lastMessagePreview)
    }

    @Test
    fun isMonologue_noAudience() {
        val conversation = Conversation(audience = null)

        assertEquals(false, conversation.isMonologue(0L))
    }

    @Test
    fun isMonologue_emptyAudience() {
        val conversation = Conversation(audience = arrayListOf())

        assertEquals(true, conversation.isMonologue(0L))
    }

    @Test
    fun isMonologue_userInAudience() {
        val audience = arrayListOf(1L, 2L, 3L)
        val conversation = Conversation(audience = audience)

        assertEquals(true, conversation.isMonologue(1L))
    }

    @Test
    fun isMonologue_userNotInAudience() {
        val audience = arrayListOf(1L, 2L, 3L)
        val conversation = Conversation(audience = audience)

        assertEquals(false, conversation.isMonologue(4L))
    }

    @Test
    fun getLastMessageSent() {
        val conversation = Conversation(lastMessageAt = "2008-09-15T15:53:00+05:00")
        val expectedDate = Date(1221475980000L)

        assertEquals(0, expectedDate.compareTo(conversation.lastMessageSent))
    }

    @Test
    fun getLastAuthoredMessageSent() {
        val conversation = Conversation(lastAuthoredMessageAt = "2008-09-15T15:53:00+05:00")
        val expectedDate = Date(1221475980000L)

        assertEquals(0, expectedDate.compareTo(conversation.lastAuthoredMessageSent).toLong())
    }
}