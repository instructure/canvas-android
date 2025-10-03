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
package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.LongId
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import com.instructure.canvas.espresso.mockcanvas.utils.Segment
import com.instructure.canvas.espresso.mockcanvas.utils.successResponse
import com.instructure.canvas.espresso.mockcanvas.utils.unauthorizedResponse
import com.instructure.canvas.espresso.mockcanvas.utils.user
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.utils.APIHelper
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.util.GregorianCalendar

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
            val filter = request.url.queryParameter("filter")
            if(filter != null) {
                val conversationList = data.conversationCourseMap[filter.substringAfter("course_").toLong()]
                if(conversationList.isNullOrEmpty()) {
                    request.unauthorizedResponse()
                } else {
                    request.successResponse(conversationList)
                }
            } else {
                var response = when(request.url.queryParameter("scope")) {
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
                            // Filter out "sent and archived" messages for "ALL"
                            it.messages.firstOrNull()?.authorId != request.user!!.id &&
                                it.workflowState != Conversation.WorkflowState.ARCHIVED
                        }
                    }
                }

                response = response.map {c ->
                    // Make sure audience does not include caller
                    // TODO: Monologues?  Should we assume that we don't do them?
                    c.copy(audience = c.messages.firstOrNull()?.participatingUserIds?.filter {id -> id != request.user!!.id})
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
            val conversationIds = request.url.queryParameterValues("conversation_ids[]").map { it?.toLongOrNull() }
                .filterNotNull()
            val event = request.url.queryParameter("event")

            when (event) {
                "mark_as_unread" -> successfulRequestWithModifiedConversation(request, data) { it.copy(workflowState = Conversation.WorkflowState.UNREAD) }
                "mark_as_read" -> successfulRequestWithModifiedConversation(request, data) { it.copy(workflowState = Conversation.WorkflowState.READ) }
                "archive" -> successfulRequestWithModifiedConversation(request, data) { it.copy(workflowState = Conversation.WorkflowState.ARCHIVED) }
                "star" -> successfulRequestWithModifiedConversation(request, data) { it.copy(isStarred = true) }
                "unstar" -> successfulRequestWithModifiedConversation(request, data) { it.copy(isStarred = false) }
                "destroy" -> {
                    conversationIds.forEach {
                        data.conversations.remove(it)
                    }
                    request.successResponse(Any())
                }
                else -> {
                    // We only know how to handle a single conversation id and event = "mark as unread".
                    // Everything else gets bounced.
                    request.unauthorizedResponse()
                }
            }
        }
    }
)

private fun successfulRequestWithModifiedConversation(request: Request, data: MockCanvas, action: (Conversation) -> Conversation): Response {
    val conversationIds = request.url.queryParameterValues("conversation_ids[]").map { it?.toLongOrNull() }
        .filterNotNull()

    conversationIds.forEach {
        val conversation = data.conversations[it]!!
        val updatedConversation = action(conversation)
        data.conversations[it] = updatedConversation
    }
    return request.successResponse(Any())
}

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
        Segment("remove_messages") to endpoint(
                configure = {
                    POST {
                        val conversationId = pathVars.conversationId
                        val conversation = data.conversations[conversationId]
                        val messageId = request.url.queryParameter("remove[]")?.toLongOrNull()
                        if(conversation != null && messageId != null) {
                            val newMessages = conversation.messages.filter {m -> m.id != messageId}
                            val updatedConversation = conversation.copy(
                                    messages = newMessages,
                                    messageCount = newMessages.count(),
                                    lastMessage = newMessages.lastOrNull()?.body
                            )

                            data.conversations[conversationId] = updatedConversation
                            request.successResponse(updatedConversation)
                        }
                        else {
                            // We'll fail on anything other than a valid conversationId and
                            // a single specified messageId
                            request.unauthorizedResponse()
                        }
                    }
                }
        ),
    Segment("add_message") to endpoint(
        configure = {
            POST {
                if (data.conversations.containsKey(pathVars.conversationId)) {
                    val conversation = data.conversations[pathVars.conversationId]!!
                    val bodyParamsSize = (request.body as? FormBody)?.size ?: 0
                    var bodyIndex = -1
                    for (index in 0..bodyParamsSize) {
                        if ((request.body as? FormBody)?.name(index) == "body") {
                            bodyIndex = index
                            break
                        }
                    }

                    val messageBody = if (bodyIndex != -1) {
                        (request.body as? FormBody)?.value(bodyIndex)
                    } else {
                        null
                    }

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
                    val conversation = data.conversations[conversationId]!!

                    // Mark as read if conversation is currently unread
                    if(conversation.workflowState == Conversation.WorkflowState.UNREAD) {
                        conversation.workflowState = Conversation.WorkflowState.READ
                    }

                    // Remove caller from audience
                    val result = conversation.copy(
                            audience = conversation.audience?.toMutableList()?.filter { id -> id != userId }
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
            val newStarred = request.url.queryParameter("conversation[starred]")?.equals("true")
            val newStateRaw = request.url.queryParameter("conversation[workflow_state]")
            val newState = when(newStateRaw) {
                "read" -> Conversation.WorkflowState.READ
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

        DELETE {
            // Delete a conversation
            val conversationId = pathVars.conversationId
            val conversation = data.conversations[conversationId]
            if(conversation != null) {
                // This is a bit of a cheat, since we're deleting the conversation for EVERYBODY
                // instead of just the caller.  So we're assuming that we won't be interested
                // in accessing this data as another user.  We make general assumptions like that in
                // MockCanvas that may need to be tweaked if our requirements become more nuanced.
                data.conversations.remove(conversationId)
                request.successResponse(conversation)
            }
            else {
                request.unauthorizedResponse()
            }

        }
    }
)
