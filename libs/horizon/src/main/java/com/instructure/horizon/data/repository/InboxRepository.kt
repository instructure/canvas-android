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
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.data.datasource.InboxNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class InboxRepository @Inject constructor(
    private val networkDataSource: InboxNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {
    suspend fun getConversations(scope: InboxApi.Scope, forceNetwork: Boolean): List<Conversation> {
        return networkDataSource.getConversations(scope, forceNetwork)
    }

    suspend fun getConversation(id: Long, markAsRead: Boolean, forceNetwork: Boolean): Conversation {
        return networkDataSource.getConversation(id, markAsRead, forceNetwork)
    }

    suspend fun addMessageToConversation(
        contextCode: String,
        conversationId: Long,
        recipientIds: List<String>,
        body: String,
        includedMessageIds: List<Long>,
        attachmentIds: List<Long>,
    ): Conversation {
        requireOnlineForWrite()
        return networkDataSource.addMessageToConversation(
            conversationId = conversationId,
            recipientIds = recipientIds,
            body = body,
            includedMessageIds = includedMessageIds,
            attachmentIds = attachmentIds,
            contextCode = contextCode,
        )
    }

    suspend fun createConversation(
        recipientIds: List<String>,
        subject: String,
        body: String,
        contextCode: String,
        attachmentIds: LongArray,
        isBulkMessage: Boolean,
    ) {
        requireOnlineForWrite()
        networkDataSource.createConversation(
            recipientIds = recipientIds,
            subject = subject,
            body = body,
            contextCode = contextCode,
            attachmentIds = attachmentIds,
            isBulkMessage = isBulkMessage,
        )
    }

    suspend fun getInboxCourses(forceNetwork: Boolean): List<Course> {
        return networkDataSource.getInboxCourses(forceNetwork)
    }

    suspend fun getRecipientsForUserContext(searchQuery: String?, forceNetwork: Boolean): List<Recipient> {
        return networkDataSource.getRecipients(networkDataSource.userContextCode(), searchQuery, forceNetwork)
    }

    suspend fun getRecipientsForCourse(courseId: Long, searchQuery: String?, forceNetwork: Boolean): List<Recipient> {
        return networkDataSource.getRecipients(courseId.toString(), searchQuery, forceNetwork)
    }

    suspend fun getInboxCourseAnnouncements(forceNetwork: Boolean): List<Pair<Course, DiscussionTopicHeader>> {
        val courses = networkDataSource.getInboxCourses(forceNetwork)
        if (courses.isEmpty()) return emptyList()
        val announcements = networkDataSource.getCourseAnnouncements(courses.map { it.contextId }, forceNetwork)
        return announcements.map { announcement ->
            val course = courses.first { it.contextId == announcement.contextCode }
            course to announcement
        }
    }

    suspend fun getInboxAccountAnnouncements(forceNetwork: Boolean): List<AccountNotification> {
        return networkDataSource.getAccountAnnouncements(forceNetwork)
    }

    fun invalidateConversationListCachedResponse() {
        networkDataSource.invalidateConversationListCachedResponse()
    }

    fun invalidateConversationDetailsCachedResponse(conversationId: Long) {
        networkDataSource.invalidateConversationDetailsCachedResponse(conversationId)
    }
}
