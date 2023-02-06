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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conversation
import java.io.IOException

object InboxManager {

    fun createConversation(
        userIDs: List<String>,
        message: String,
        subject: String,
        contextId: String,
        attachmentIds: LongArray,
        isBulk: Boolean,
        callback: StatusCallback<List<Conversation>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        InboxApi.createConversation(
            adapter,
            params,
            userIDs,
            message,
            subject,
            contextId,
            attachmentIds,
            isBulk,
            callback
        )
    }

    fun getConversations(scope: InboxApi.Scope, forceNetwork: Boolean, callback: StatusCallback<List<Conversation>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        InboxApi.getConversations(scope, adapter, callback, params)
    }

    fun getConversationsFiltered(
        scope: InboxApi.Scope,
        canvasContext: String,
        forceNetwork: Boolean,
        callback: StatusCallback<List<Conversation>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        InboxApi.getConversationsFiltered(scope, canvasContext, adapter, callback, params)
    }

    fun getConversation(conversationId: Long, forceNetwork: Boolean, callback: StatusCallback<Conversation>, markAsRead: Boolean = true) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        InboxApi.getConversation(adapter, callback, params, conversationId, markAsRead)
    }

    fun starConversation(
        conversationId: Long,
        starred: Boolean,
        workFlowState: Conversation.WorkflowState,
        callback: StatusCallback<Conversation>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        InboxApi.updateConversation(adapter, callback, params, conversationId, workFlowState, starred)
    }

    fun archiveConversation(conversationId: Long, archive: Boolean, callback: StatusCallback<Conversation>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        InboxApi.updateConversation(
            adapter,
            callback,
            params,
            conversationId,
            if (archive) Conversation.WorkflowState.ARCHIVED else Conversation.WorkflowState.READ,
            null
        )
    }

    fun deleteConversation(conversationId: Long, callback: StatusCallback<Conversation>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        InboxApi.deleteConversation(adapter, callback, params, conversationId)
    }

    fun deleteMessages(conversationId: Long, messageIds: List<Long>, callback: StatusCallback<Conversation>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        InboxApi.deleteMessages(adapter, callback, params, conversationId, messageIds)
    }

    fun addMessage(
        conversationId: Long,
        message: String,
        recipientIds: List<String>,
        includedMessageIds: LongArray,
        attachmentIds: LongArray,
        contextId: String?,
        callback: StatusCallback<Conversation>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        InboxApi.addMessage(
            adapter,
            callback,
            params,
            conversationId,
            recipientIds,
            message,
            includedMessageIds,
            attachmentIds,
            contextId
        )
    }

    fun markConversationAsUnread(conversationId: Long, callback: StatusCallback<Void>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        InboxApi.markConversationAsUnread(adapter, callback, params, conversationId)
    }

    fun getConversationSynchronous(conversationId: Long, forceNetwork: Boolean): Conversation? {
        val adapter = RestBuilder()
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return try {
            val response = InboxApi.getConversation(adapter, params, conversationId, markAsRead = true)
            if (response.isSuccessful) response.body() else null
        } catch (e: IOException) {
            null
        }
    }

}
