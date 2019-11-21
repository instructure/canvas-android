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
                when(request.url().queryParameter("scope")) {
                    "unread" -> {
                        request.successResponse(data.conversations.values.toList().filter {
                            it.workflowState == Conversation.WorkflowState.UNREAD
                        })
                    }
                    "starred" -> {
                        request.successResponse(data.conversations.values.toList().filter {
                            it.isStarred
                        })
                    }
                    "archived" -> {
                        request.successResponse(data.conversations.values.toList().filter {
                            it.workflowState == Conversation.WorkflowState.ARCHIVED
                        })
                    }
                    "sent" -> {
                        request.successResponse(data.conversations.values.toList().filter {
                            it.messages.first().authorId == request.user!!.id
                        })
                    }
                    else -> { // We need to filter out "sent" messages for "ALL"
                        request.successResponse(data.conversations.values.toList().filter {
                            it.messages.first().authorId != request.user!!.id
                        })
                    }
                }
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
                        createdAt = APIHelper.dateToString(GregorianCalendar()),
                        body = messageBody,
                        participatingUserIds = listOf(data.newItemId(), data.newItemId())
                    )
                    data.conversations[pathVars.conversationId] = conversation.copy(messages = conversation.messages.plus(message))
                    request.successResponse(data.conversations[pathVars.conversationId]!!)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
    ),
    response = {
        GET {
            when {
                data.conversations.containsKey(pathVars.conversationId) -> {
                    request.successResponse(data.conversations[pathVars.conversationId]!!)
                }
                else -> {
                    request.unauthorizedResponse()
                }
            }
        }
    }
)
