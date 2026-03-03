/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import kotlinx.coroutines.withTimeoutOrNull

abstract class InboxComposeRepository(
    private val courseAPI: CourseAPI.CoursesInterface,
    private val recipientAPI: RecipientAPI.RecipientInterface,
    private val inboxAPI: InboxApi.InboxInterface,
    private val inboxSettingsManager: InboxSettingsManager
) {
    abstract suspend fun getCourses(forceRefresh: Boolean = false): DataResult<List<Course>>

    abstract suspend fun getGroups(forceRefresh: Boolean = false): DataResult<List<Group>>

    abstract suspend fun isInboxSignatureFeatureEnabled(): Boolean

    open suspend fun getRecipients(
        searchQuery: String,
        contextId: String,
        forceRefresh: Boolean
    ): DataResult<List<Recipient>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return recipientAPI.getFirstPageRecipientListNoSyntheticContexts(
            searchQuery = searchQuery,
            context = contextId,
            restParams = params,
        ).depaginate {
            recipientAPI.getNextPageRecipientList(it, params)
        }
    }

    open suspend fun createConversation(
        recipients: List<Recipient>,
        subject: String,
        message: String,
        context: CanvasContext,
        attachments: List<Attachment>,
        isIndividual: Boolean
    ): DataResult<List<Conversation>> {
        val restParams = RestParams()

        return inboxAPI.createConversation(
            recipients = recipients.mapNotNull { it.stringId },
            subject = subject,
            message = message,
            contextCode = context.contextId,
            attachmentIds = attachments.map { it.id }.toLongArray(),
            isBulk = if (!isIndividual) { 0 } else { 1 },
            params = restParams
        )
    }

    open suspend fun addMessage(
        conversationId: Long,
        recipients: List<Recipient>,
        message: String,
        includedMessages: List<Message>,
        attachments: List<Attachment>,
        context: CanvasContext
    ): DataResult<Conversation> {
        val restParams = RestParams()

        return inboxAPI.addMessage(
            conversationId = conversationId,
            recipientIds = recipients.mapNotNull { it.stringId },
            body = message,
            includedMessageIds = includedMessages.map { it.id }.toLongArray(),
            attachmentIds = attachments.map { it.id }.toLongArray(),
            contextCode = context.contextId,
            params = restParams
        )
    }

    open suspend fun canSendToAll(context: CanvasContext): DataResult<Boolean> {
        val restParams = RestParams()
        val permissionResponse = courseAPI.getCoursePermissions(
            context.id,
            listOf(CanvasContextPermission.SEND_MESSAGES_ALL),
            restParams
        )

        return permissionResponse.map {
            it.send_messages_all
        }
    }

    suspend fun getInboxSignature(): String {
        // Just to ensure we won't show the loading forever if there is an issue with the network connection
        val inboxSignatureSettings = withTimeoutOrNull(3000) {
            if (isInboxSignatureFeatureEnabled()) inboxSettingsManager.getInboxSignatureSettings() else null
        }?.dataOrNull

        return if (inboxSignatureSettings != null && inboxSignatureSettings.useSignature && inboxSignatureSettings.signature.isNotBlank()) {
            inboxSignatureSettings.signature
        } else {
            ""
        }
    }
}