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
package com.instructure.horizon.features.inbox.list

import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.horizon.domain.usecase.inbox.GetConversationsUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxCoursesUseCase
import com.instructure.horizon.domain.usecase.inbox.GetInboxRecipientsUseCase
import javax.inject.Inject

class HorizonInboxListRepository @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getInboxRecipientsUseCase: GetInboxRecipientsUseCase,
    private val getInboxCoursesUseCase: GetInboxCoursesUseCase,
    private val inboxRepository: InboxRepository,
    private val announcementsApi: AnnouncementAPI.AnnouncementInterface,
    private val accountNotificationApi: com.instructure.canvasapi2.apis.AccountNotificationAPI.AccountNotificationInterface,
) {
    suspend fun getConversations(scope: InboxApi.Scope = InboxApi.Scope.INBOX, forceNetwork: Boolean): List<Conversation> {
        return getConversationsUseCase(GetConversationsUseCase.Params(scope, forceNetwork))
    }

    suspend fun getRecipients(searchQuery: String?, forceNetwork: Boolean): List<Recipient> {
        return getInboxRecipientsUseCase(GetInboxRecipientsUseCase.Params(searchQuery = searchQuery, forceNetwork = forceNetwork))
    }

    suspend fun getCourseAnnouncements(forceNetwork: Boolean): List<Pair<Course, DiscussionTopicHeader>> {
        val courses = getInboxCoursesUseCase(GetInboxCoursesUseCase.Params(forceNetwork))
        if (courses.isEmpty()) return emptyList()

        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return announcementsApi.getFirstPageAnnouncements(
            courseCode = courses.map { it.contextId }.toTypedArray(),
            params = params,
        )
            .depaginate { announcementsApi.getNextPageAnnouncementsList(it, params) }
            .dataOrThrow
            .map { announcement ->
                val course = courses.first { it.contextId == announcement.contextCode }
                course to announcement
            }
    }

    suspend fun getAccountAnnouncements(forceNetwork: Boolean): List<AccountNotification> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return accountNotificationApi.getAccountNotifications(params, true, true)
            .depaginate { accountNotificationApi.getNextPageNotifications(it, params) }
            .dataOrThrow
    }

    fun invalidateConversationListCachedResponse() {
        inboxRepository.invalidateConversationListCachedResponse()
    }
}
