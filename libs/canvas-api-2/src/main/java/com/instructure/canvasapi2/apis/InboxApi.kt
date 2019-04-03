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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ApiType
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.io.IOException


object InboxApi {

    const val CONVERSATION_MARK_UNREAD = "mark_as_unread"

    enum class Scope {
        ALL, UNREAD, ARCHIVED, STARRED, SENT
    }

    fun conversationScopeToString(scope: Scope): String = when (scope) {
        Scope.UNREAD -> "unread"
        Scope.STARRED -> "starred"
        Scope.ARCHIVED -> "archived"
        Scope.SENT -> "sent"
        else -> ""
    }

    internal interface InboxInterface {

        @GET("conversations/?interleave_submissions=1&include[]=participant_avatars")
        fun getConversations(@Query("scope") scope: String): Call<List<Conversation>>

        @GET("conversations/?interleave_submissions=1&include[]=participant_avatars")
        fun getConversationsFiltered(@Query("scope") scope: String, @Query("filter") canvasContextFilter: String): Call<List<Conversation>>

        @GET
        fun getNextPage(@Url nextURL: String): Call<List<Conversation>>

        @POST("conversations?group_conversation=true")
        fun createConversation(@Query("recipients[]") recipients: List<String>, @Query(value = "body", encoded = true) message: String, @Query(value = "subject", encoded = true) subject: String, @Query("context_code") contextCode: String, @Query("attachment_ids[]") attachmentIds: LongArray, @Query("bulk_message") isBulk: Int): Call<List<Conversation>>

        @GET("conversations/{conversationId}?include[]=participant_avatars")
        fun getConversation(@Path("conversationId") conversationId: Long): Call<Conversation>

        @PUT("conversations/{conversationId}")
        fun updateConversation(@Path("conversationId") conversationId: Long, @Query("conversation[workflow_state]") workflowState: String, @Query("conversation[starred]") isStarred: Boolean?): Call<Conversation>

        @DELETE("conversations/{conversationId}")
        fun deleteConversation(@Path("conversationId") conversationId: Long): Call<Conversation>

        @POST("conversations/{conversationId}/remove_messages")
        fun deleteMessages(@Path("conversationId") conversationId: Long, @Query("remove[]") messageIds: List<Long>): Call<Conversation>

        @POST("conversations/{conversationId}/add_message?group_conversation=true")
        fun addMessage(@Path("conversationId") conversationId: Long, @Query("recipients[]") recipientIds: List<String>, @Query(value = "body", encoded = true) body: String, @Query("included_messages[]") includedMessageIds: LongArray, @Query("attachment_ids[]") attachmentIds: LongArray): Call<Conversation>

        @PUT("conversations")
        fun markConversationAsUnread(@Query("conversation_ids[]") conversationId: Long, @Query("event") conversationEvent: String): Call<Void>

        @POST("conversations/{id}/add_message")
        fun addMessageToConversationSynchronous(@Path("id") conversationId: Long, @Query("body") message: String, @Query("attachment_ids[]") attachments: List<Long>): Call<Conversation>

        @POST("conversations?group_conversation=true")
        fun createConversationWithAttachmentSynchronous(@Query("recipients[]") recipients: List<String>, @Query("body") message: String, @Query("subject") subject: String, @Query("context_code") contextCode: String, @Query("bulk_message") isGroup: Int, @Query("attachment_ids[]") attachments: List<Long>): Call<List<Conversation>>

    }

    fun getConversation(adapter: RestBuilder, callback: StatusCallback<Conversation>, params: RestParams, conversationId: Long) {
        callback.addCall(adapter.build(InboxInterface::class.java, params).getConversation(conversationId)).enqueue(callback)
    }

    fun getConversations(scope: Scope, adapter: RestBuilder, callback: StatusCallback<List<Conversation>>, params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            adapter.build(InboxInterface::class.java, params).getConversations(conversationScopeToString(scope)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            adapter.build(InboxInterface::class.java, params).getNextPage(callback.linkHeaders!!.nextUrl!!).enqueue(callback)
        } else {
            callback.onCallbackFinished(ApiType.API)
        }
    }

    fun getConversationsFiltered(scope: Scope, canvasContextFilter: String, adapter: RestBuilder, callback: StatusCallback<List<Conversation>>, params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            adapter.build(InboxInterface::class.java, params).getConversationsFiltered(conversationScopeToString(scope), canvasContextFilter).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            adapter.build(InboxInterface::class.java, params).getNextPage(callback.linkHeaders!!.nextUrl!!).enqueue(callback)
        } else {
            callback.onCallbackFinished(ApiType.API)
        }
    }

    fun createConversation(adapter: RestBuilder, params: RestParams, userIDs: List<String>, message: String, subject: String, contextId: String, attachmentIds: LongArray, isBulk: Boolean, callback: StatusCallback<List<Conversation>>) {
        // The message has to be sent to somebody.
        if (userIDs.isEmpty()) {
            return
        }
        // "true" has to be hardcoded for group_conversations, see the base url above at the interface.
        // isBulk is what we use to differentiate between sent individually vs as a group
        callback.addCall(adapter.build(InboxInterface::class.java, params).createConversation(userIDs, message, subject, contextId, attachmentIds, if (isBulk) 1 else 0)).enqueue(callback)
    }

    fun updateConversation(adapter: RestBuilder, callback: StatusCallback<Conversation>, params: RestParams, conversationId: Long, workflowState: Conversation.WorkflowState?, starred: Boolean?) {
        callback.addCall(adapter.build(InboxInterface::class.java, params).updateConversation(conversationId, Conversation.getWorkflowStateAPIString(workflowState), starred)).enqueue(callback)
    }

    fun deleteConversation(adapter: RestBuilder, callback: StatusCallback<Conversation>, params: RestParams, conversationId: Long) {
        callback.addCall(adapter.build(InboxInterface::class.java, params).deleteConversation(conversationId)).enqueue(callback)
    }

    fun deleteMessages(adapter: RestBuilder, callback: StatusCallback<Conversation>, params: RestParams, conversationId: Long, messageIds: List<Long>) {
        callback.addCall(adapter.build(InboxInterface::class.java, params).deleteMessages(conversationId, messageIds)).enqueue(callback)
    }

    fun addMessage(adapter: RestBuilder, callback: StatusCallback<Conversation>, params: RestParams, conversationId: Long, recipientIds: List<String>, message: String, includedMessageIds: LongArray, attachmentIds: LongArray) {
        callback.addCall(adapter.build(InboxInterface::class.java, params).addMessage(conversationId, recipientIds, message, includedMessageIds, attachmentIds)).enqueue(callback)
    }

    fun markConversationAsUnread(adapter: RestBuilder, callback: StatusCallback<Void>, params: RestParams, conversationId: Long, conversationEvent: String) {
        callback.addCall(adapter.build(InboxInterface::class.java, params).markConversationAsUnread(conversationId, conversationEvent)).enqueue(callback)
    }

    @Throws(IOException::class)
    fun getConversation(adapter: RestBuilder, params: RestParams, conversationId: Long): Response<Conversation> {
        return adapter.build(InboxInterface::class.java, params).getConversation(conversationId).execute()
    }

    @Throws(IOException::class)
    fun addMessageToConversationSynchronous(adapter: RestBuilder, params: RestParams, conversationId: Long, messageBody: String, attachmentIds: List<Long>): Response<Conversation> {

        return adapter.build(InboxInterface::class.java, params).addMessageToConversationSynchronous(conversationId, messageBody, attachmentIds).execute()
    }

    @Throws(IOException::class)
    fun createConversationWithAttachmentSynchronous(adapter: RestBuilder, params: RestParams, userIDs: List<String>, message: String, subject: String, contextId: String, isGroup: Boolean, attachmentIds: List<Long>): List<Conversation>? {
        // The message has to be sent to somebody.
        if (userIDs.isEmpty()) return null

        return try {
            adapter.build(InboxInterface::class.java, params).createConversationWithAttachmentSynchronous(userIDs, message, subject, contextId, if (isGroup) 0 else 1, attachmentIds).execute().body()
        } catch (e: Exception) {
            null
        }

    }
}
