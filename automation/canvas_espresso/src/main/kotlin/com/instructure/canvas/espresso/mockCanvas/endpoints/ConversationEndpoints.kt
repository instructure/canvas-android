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


// https://mock-data.instructure.com/api/v1/conversations?interleave_submissions=1&include[]=participant_avatars&scope=&per_page=100, tags={class retrofit2.Invocation=com.instructure.canvasapi2.apis.InboxApi$InboxInterface.getConversations() [], class java.lang.Object=RestParams(canvasContext=null, domain=https://mock-data.instructure.com, apiVersion=/api/v1/, usePerPageQueryParam=true, shouldIgnoreToken=false, isForceReadFromCache=false, isForceReadFromNetwork=false, acceptLanguageOverride=null)}}
// https://mock-data.instructure.com/api/v1/conversations/?interleave_submissions=1&include[]=participant_avatars&scope=&filter=course_1&per_page=100

/**
 * Endpoint that can return a list of [Conversation]s
 *
 * ROUTES:
 * - `unread_count` -> [ConversationUnreadCountEndpoint]
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
                    "unread" -> request.successResponse(data.unreadConversations.values.toList())
                    "starred" -> request.successResponse(data.starredConversations.values.toList())
                    "archived" -> request.successResponse(data.archivedConversations.values.toList())
                    "sent" -> request.successResponse(data.sentConversations.values.toList())
                    else -> request.successResponse(data.conversations.values.toList())
                }
            }
        }
        POST {
            if(data.sentConversation == null) {
                request.unauthorizedResponse()
            } else {
                data.sentConversations[data.sentConversation!!.id] = data.sentConversation!!
                request.successResponse(listOf(data.sentConversation!!))
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

// https://mock-data.instructure.com/api/v1/conversations/5/add_message?group_conversation=true&recipients%5B%5D=123&body=What+is+this%2C+hodor%3F&included_messages%5B%5D=12345 (2ms)
object ConversationEndpoint : Endpoint(
    Segment("add_message") to endpoint(
        configure = {
            POST {
                if (data.conversations.containsKey(pathVars.conversationId)) {
                    val conversation = data.conversations[pathVars.conversationId]!!
                    val messageBody = request.url().queryParameter("body")
                    val message = Message(
                        id = 123L,
                        createdAt = APIHelper.dateToString(GregorianCalendar()),
                        body = messageBody,
                        participatingUserIds = listOf(123L, 1234L)
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
            if(data.conversations.containsKey(pathVars.conversationId)) {
                request.successResponse(data.conversations[pathVars.conversationId]!!)
            } else {
                request.unauthorizedResponse()
            }
        }
    }
)
