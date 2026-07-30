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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class InboxNetworkDataSource @Inject constructor(
    private val inboxApi: InboxApi.InboxInterface,
    private val recipientApi: RecipientAPI.RecipientInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val announcementsApi: AnnouncementAPI.AnnouncementInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getConversations(scope: InboxApi.Scope, forceNetwork: Boolean): List<Conversation> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return inboxApi.getConversations(InboxApi.conversationScopeToString(scope), params)
            .depaginate { inboxApi.getNextPage(it, params) }
            .dataOrThrow
    }

    suspend fun getConversation(id: Long, markAsRead: Boolean, forceNetwork: Boolean): Conversation {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return inboxApi.getConversation(id, markAsRead, params).dataOrThrow
    }

    suspend fun addMessageToConversation(
        conversationId: Long,
        recipientIds: List<String>,
        body: String,
        includedMessageIds: List<Long>,
        attachmentIds: List<Long>,
        contextCode: String,
    ): Conversation {
        return inboxApi.addMessage(
            conversationId = conversationId,
            recipientIds = recipientIds,
            body = body,
            includedMessageIds = includedMessageIds.toLongArray(),
            attachmentIds = attachmentIds.toLongArray(),
            contextCode = contextCode,
            params = RestParams(),
        ).dataOrThrow
    }

    suspend fun createConversation(
        recipientIds: List<String>,
        subject: String,
        body: String,
        contextCode: String,
        attachmentIds: LongArray,
        isBulkMessage: Boolean,
    ) {
        inboxApi.createConversation(
            recipients = recipientIds,
            message = body,
            subject = subject,
            contextCode = contextCode,
            isBulk = if (isBulkMessage) 1 else 0,
            attachmentIds = attachmentIds,
            params = RestParams(),
        )
    }

    suspend fun getRecipients(contextCode: String, searchQuery: String?, forceNetwork: Boolean): List<Recipient> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return recipientApi.getFirstPageRecipientList(searchQuery, contextCode, params)
            .depaginate { recipientApi.getNextPageRecipientList(it, params) }
            .dataOrThrow
            .filter { it.recipientType == Recipient.Type.Person }
    }

    suspend fun getInboxCourses(forceNetwork: Boolean): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return courseApi.getFirstPageCoursesInbox(params)
            .depaginate { courseApi.next(it, params) }
            .dataOrThrow
    }

    suspend fun getCourseAnnouncements(courseContextIds: List<String>, forceNetwork: Boolean): List<DiscussionTopicHeader> {
        if (courseContextIds.isEmpty()) return emptyList()
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return announcementsApi.getFirstPageAnnouncements(
            courseCode = courseContextIds.toTypedArray(),
            params = params,
        )
            .depaginate { announcementsApi.getNextPageAnnouncementsList(it, params) }
            .dataOrThrow
    }

    suspend fun getAccountAnnouncements(forceNetwork: Boolean): List<AccountNotification> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return accountNotificationApi.getAccountNotifications(params, true, true)
            .depaginate { accountNotificationApi.getNextPageNotifications(it, params) }
            .dataOrThrow
    }

    fun userContextCode(): String = apiPrefs.user!!.contextId

    fun invalidateConversationListCachedResponse() {
        CanvasRestAdapter.clearCacheUrls("conversations")
    }

    fun invalidateConversationDetailsCachedResponse(conversationId: Long) {
        CanvasRestAdapter.clearCacheUrls("conversations/$conversationId")
    }
}
