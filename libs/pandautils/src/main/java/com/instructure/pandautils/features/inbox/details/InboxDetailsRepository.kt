package com.instructure.pandautils.features.inbox.details

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.DataResult

interface InboxDetailsRepository {
    suspend fun getConversation(conversationId: Long, markAsRead: Boolean = true): DataResult<Conversation>
}

class InboxDetailsRepositoryImpl(
    private val inboxAPI: InboxApi.InboxInterface,
): InboxDetailsRepository {
    override suspend fun getConversation(conversationId: Long, markAsRead: Boolean): DataResult<Conversation> {
        val params = RestParams()
        return inboxAPI.getConversation(conversationId, markAsRead, params)
    }
}