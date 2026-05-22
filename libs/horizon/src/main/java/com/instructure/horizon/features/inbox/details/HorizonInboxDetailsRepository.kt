/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.inbox.details

import com.instructure.canvasapi2.models.Conversation
import com.instructure.horizon.domain.usecase.inbox.AddMessageToConversationUseCase
import com.instructure.horizon.domain.usecase.inbox.GetConversationUseCase
import javax.inject.Inject

class HorizonInboxDetailsRepository @Inject constructor(
    private val getConversationUseCase: GetConversationUseCase,
    private val addMessageToConversationUseCase: AddMessageToConversationUseCase,
) {
    suspend fun getConversation(id: Long, markAsRead: Boolean = true, forceRefresh: Boolean): Conversation {
        return getConversationUseCase(GetConversationUseCase.Params(id, markAsRead, forceRefresh))
    }

    suspend fun addMessageToConversation(
        contextCode: String,
        conversationId: Long,
        recipientIds: List<String>,
        body: String,
        includedMessageIds: List<Long>,
        attachmentIds: List<Long>,
    ): Conversation {
        return addMessageToConversationUseCase(
            AddMessageToConversationUseCase.Params(
                contextCode = contextCode,
                conversationId = conversationId,
                recipientIds = recipientIds,
                body = body,
                includedMessageIds = includedMessageIds,
                attachmentIds = attachmentIds,
            )
        )
    }
}
