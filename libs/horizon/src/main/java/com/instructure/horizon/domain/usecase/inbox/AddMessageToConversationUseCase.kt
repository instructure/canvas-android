/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.domain.usecase.inbox

import com.instructure.canvasapi2.models.Conversation
import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class AddMessageToConversationUseCase @Inject constructor(
    private val inboxRepository: InboxRepository,
) : BaseUseCase<AddMessageToConversationUseCase.Params, Conversation>() {

    data class Params(
        val contextCode: String,
        val conversationId: Long,
        val recipientIds: List<String>,
        val body: String,
        val includedMessageIds: List<Long>,
        val attachmentIds: List<Long>,
    )

    override suspend fun execute(params: Params): Conversation {
        val result = inboxRepository.addMessageToConversation(
            contextCode = params.contextCode,
            conversationId = params.conversationId,
            recipientIds = params.recipientIds,
            body = params.body,
            includedMessageIds = params.includedMessageIds,
            attachmentIds = params.attachmentIds,
        )
        inboxRepository.invalidateConversationDetailsCachedResponse(params.conversationId)
        return result
    }
}
