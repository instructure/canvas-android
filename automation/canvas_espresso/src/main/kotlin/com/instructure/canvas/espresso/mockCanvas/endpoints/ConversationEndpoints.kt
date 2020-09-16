/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockCanvas.endpoints

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.utils.APIHelper
import java.util.*

/**
 * Endpoint that can return a list of [Conversation]s
 *
 * ROUTES:
 * - `unread_count` -> [ConversationUnreadCountEndpoint]
 * - `conversation_id` -> [ConversationEndpoint]
 */
object ConversationListEndpoint : Endpoint(
    Segment("unread_count") to ConversationUnreadCountEndpoint,
    LongId(PathVars::conversationId) to ConversationEndpoint,
    response = {
        GET {
            val filter = request.url().queryParameter("filter")
            if(filter != null) {
                val conversationList = data.conversationCourseMap[filter.substringAfter("course_").toLong()]
                if(conversationList.isNullOrEmpty()) {
                    request.unauthorizedResponse()
                } else {
                    request.successResponse(conversationList)
                }
            } else {
                var response = when(request.url().queryParameter("scope")) {
                    "unread" -> {
                        data.conversations.values.toList().filter {
                            it.workflowState == Conversation.WorkflowState.UNREAD
                        }
                    }
                    "starred" -> {
                        data.conversations.values.toList().filter {
                            it.isStarred
                        }
                    }
                    "archived" -> {
                        data.conversations.values.toList().filter {
                            it.workflowState == Conversation.WorkflowState.ARCHIVED
                        }
                    }
                    "sent" -> {
                        data.conversations.values.toList().filter {
                            it.messages.first().authorId == request.user!!.id
                        }
                    }
                    else -> { // We need to filter out "sent" messages for "ALL"
                        data.conversations.values.toList().filter {
                            // Filter out "sent" messages for "ALL"
                            it.messages.first().authorId != request.user!!.id
                        }
                    }
                }

                response = response.map {c ->
                    // Make sure audience does not include caller
                    // TODO: Monologues?  Should we assume that we don't do them?
                    c.copy(audience = c.messages[0].participatingUserIds.filter {id -> id != request.user!!.id})
                }

                request.successResponse(response)
            }
        }
        POST {
            if(data.sentConversation == null) {
                request.unauthorizedResponse()
            } else {
                val sentConversation = data.sentConversation
                data.conversations[sentConversation!!.id] = sentConversation
                data.sentConversation = null
                request.successResponse(listOf(sentConversation))
            }
        }

        PUT {
            // Assumes a single conversationId
            val conversationId = request.url().queryParameter("conversation_ids[]")?.toLongOrNull()
            val event = request.url().queryParameter("event")
            if(event == "mark_as_unread" && conversationId != null && data.conversations.containsKey(conversationId)) {
                val conversation = data.conversations[conversationId]!!
                val updatedConversation = conversation.copy(workflowState = Conversation.WorkflowState.UNREAD)
                data.conversations[conversationId] = updatedConversation
                println("okhttp: PUT conversations: updatedConversation = $updatedConversation")
                request.successResponse(updatedConversation)
            }
            else {
                // We only know how to handle a single conversation id and event = "mark as unread".
                // Everything else gets bounced.
                request.unauthorizedResponse()
            }
        }
    }
)

/**
 * Endpoint that can return a count of unread conversation messages
 */
object ConversationUnreadCountEndpoint : Endpoint(response = {
    GET {
        val count = data.conversations.values.count { it.workflowState == Conversation.WorkflowState.UNREAD }
        request.successResponse(UnreadConversationCount(count.toString()))
    }
})

/**
 * Endpoint that can return a [Conversation] based on pathVar.conversationId
 *
 * ROUTES:
 * - `add_message`
 * */
object ConversationEndpoint : Endpoint(
    Segment("add_message") to endpoint(
        configure = {
            POST {
                if (data.conversations.containsKey(pathVars.conversationId)) {
                    val conversation = data.conversations[pathVars.conversationId]!!
                    val messageBody = request.url().queryParameter("body")
                    val message = Message(
                        id = data.newItemId(),
                        authorId = request.user!!.id,
                        createdAt = APIHelper.dateToString(GregorianCalendar()),
                        body = messageBody,
                        participatingUserIds = conversation.messages.first().participatingUserIds
                    )
                    data.conversations[pathVars.conversationId] = conversation.copy(
                            messages = conversation.messages.plus(message),
                            lastMessage = messageBody
                    )
                    request.successResponse(data.conversations[pathVars.conversationId]!!)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
    ),
    response = {
        GET {
            val conversationId = pathVars.conversationId
            val userId = request.user?.id
            println("okhttp: user = ${request.user}")
            when {
                data.conversations.containsKey(conversationId) -> {
                    var conversation = data.conversations[conversationId]!!

                    // Mark as read if conversation is currently unread
                    if(conversation.workflowState == Conversation.WorkflowState.UNREAD) {
                        conversation.workflowState = Conversation.WorkflowState.READ
                    }

                    // Remove caller from audience
                    val result = conversation.copy(
                            audience = conversation.audience?.toMutableList()?.filter { id -> id != userId },
                    )

                    request.successResponse(result)
                }
                else -> {
                    request.unauthorizedResponse()
                }
            }
        }

        PUT {
            // Alter the conversation -- starring, archiving, etc...
            val conversationId = pathVars.conversationId
            val newStarred = request.url().queryParameter("conversation[starred]")?.equals("true")
            val newStateRaw = request.url().queryParameter("conversation[workflow_state]")
            val newState = when(newStateRaw) {
                "read" -> Conversation.WorkflowState.UNREAD
                "unread" -> Conversation.WorkflowState.UNREAD
                "archived" -> Conversation.WorkflowState.ARCHIVED
                else -> null
            }

            val conversation = data.conversations[conversationId]
            if(conversation != null) {
                val updatedConversation = conversation.copy(
                        isStarred = if(newStarred != null) newStarred else conversation.isStarred,
                        workflowState = if(newState != null) newState else conversation.workflowState
                )
                data.conversations[conversationId] = updatedConversation
                request.successResponse(updatedConversation)
            }
            else {
                request.unauthorizedResponse()
            }
        }
    }
)
