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

import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class CreateConversationUseCase @Inject constructor(
    private val inboxRepository: InboxRepository,
) : BaseUseCase<CreateConversationUseCase.Params, Unit>() {

    data class Params(
        val recipientIds: List<String>,
        val subject: String,
        val body: String,
        val contextCode: String,
        val attachmentIds: LongArray,
        val isBulkMessage: Boolean,
    )

    override suspend fun execute(params: Params) {
        inboxRepository.createConversation(
            recipientIds = params.recipientIds,
            subject = params.subject,
            body = params.body,
            contextCode = params.contextCode,
            attachmentIds = params.attachmentIds,
            isBulkMessage = params.isBulkMessage,
        )
        inboxRepository.invalidateConversationListCachedResponse()
    }
}
