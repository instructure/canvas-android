package com.instructure.pandautils.features.inbox.details

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.DataResult

interface InboxDetailsRepository {
    suspend fun getConversation(conversationId: Long, markAsRead: Boolean = true, forceRefresh: Boolean = false): DataResult<Conversation>
    suspend fun deleteConversation(conversationId: Long): DataResult<Conversation>
    suspend fun deleteMessage(conversationId: Long, messageIds: List<Long>): DataResult<Conversation>
    suspend fun updateStarred(conversationId: Long, isStarred: Boolean): DataResult<Conversation>
    suspend fun updateState(conversationId: Long, state: Conversation.WorkflowState): DataResult<Conversation>
}

class InboxDetailsRepositoryImpl(
    private val inboxAPI: InboxApi.InboxInterface,
): InboxDetailsRepository {
    override suspend fun getConversation(conversationId: Long, markAsRead: Boolean, forceRefresh: Boolean): DataResult<Conversation> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return inboxAPI.getConversation(conversationId, markAsRead, params)
    }

    override suspend fun deleteConversation(conversationId: Long): DataResult<Conversation> {
        val params = RestParams()
        return inboxAPI.deleteConversation(conversationId, params)
    }

    override suspend fun deleteMessage(
        conversationId: Long,
        messageIds: List<Long>
    ): DataResult<Conversation> {
        val params = RestParams()
        return inboxAPI.deleteMessages(conversationId, messageIds, params)
    }

    override suspend fun updateStarred(
        conversationId: Long,
        isStarred: Boolean
    ): DataResult<Conversation> {
        val params = RestParams()
        return inboxAPI.updateConversation(conversationId, null, isStarred, params)
    }

    override suspend fun updateState(
        conversationId: Long,
        state: Conversation.WorkflowState
    ): DataResult<Conversation> {
        val params = RestParams()
        return inboxAPI.updateConversation(conversationId, state.apiString, null, params)
    }
}