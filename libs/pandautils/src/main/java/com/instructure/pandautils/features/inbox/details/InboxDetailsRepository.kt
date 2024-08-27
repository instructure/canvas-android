package com.instructure.pandautils.features.inbox.details

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.DataResult

interface InboxDetailsRepository {
    suspend fun getConversation(conversationId: Long, markAsRead: Boolean = true, forceRefresh: Boolean = false): DataResult<Conversation>
}

class InboxDetailsRepositoryImpl(
    private val inboxAPI: InboxApi.InboxInterface,
): InboxDetailsRepository {
    override suspend fun getConversation(conversationId: Long, markAsRead: Boolean, forceRefresh: Boolean): DataResult<Conversation> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return inboxAPI.getConversation(conversationId, markAsRead, params)
    }
}