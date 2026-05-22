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
package com.instructure.horizon.features.inbox.compose

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.horizon.domain.usecase.inbox.CreateConversationUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxCoursesUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxRecipientsUseCase
import javax.inject.Inject

class HorizonInboxComposeRepository @Inject constructor(
    private val getInboxCoursesUseCase: GetInboxCoursesUseCase,
    private val getInboxRecipientsUseCase: GetInboxRecipientsUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val inboxRepository: InboxRepository,
) {
    suspend fun getAllInboxCourses(forceNetwork: Boolean): List<Course> {
        return getInboxCoursesUseCase(GetInboxCoursesUseCase.Params(forceNetwork))
    }

    suspend fun getRecipients(courseId: Long, searchQuery: String?, forceNetwork: Boolean = false): List<Recipient> {
        return getInboxRecipientsUseCase(
            GetInboxRecipientsUseCase.Params(courseId = courseId, searchQuery = searchQuery, forceNetwork = forceNetwork)
        )
    }

    suspend fun createConversation(
        recipientIds: List<String>,
        body: String,
        subject: String,
        contextCode: String,
        attachmentIds: LongArray,
        isBulkMessage: Boolean,
    ) {
        createConversationUseCase(
            CreateConversationUseCase.Params(
                recipientIds = recipientIds,
                subject = subject,
                body = body,
                contextCode = contextCode,
                attachmentIds = attachmentIds,
                isBulkMessage = isBulkMessage,
            )
        )
    }

    fun invalidateConversationListCachedResponse() {
        inboxRepository.invalidateConversationListCachedResponse()
    }
}
