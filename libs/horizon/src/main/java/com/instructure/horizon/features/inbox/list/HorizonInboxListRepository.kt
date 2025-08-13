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
import com.instructure.canvasapi2.utils.toApiString
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class HorizonInboxListRepository @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val inboxApi: InboxApi.InboxInterface,
    private val recipientsApi: RecipientAPI.RecipientInterface,
    private val announcementsApi: AnnouncementAPI.AnnouncementInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface,
    private val courseApi: CourseAPI.CoursesInterface,
) {
    suspend fun getConversations(scope: InboxApi.Scope = InboxApi.Scope.INBOX, forceNetwork: Boolean): List<Conversation> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return inboxApi.getConversations(InboxApi.conversationScopeToString(scope), params).depaginate {
            inboxApi.getNextPage(it, params)
        }.dataOrThrow
    }

    suspend fun getRecipients(searchQuery: String?, forceNetwork: Boolean): List<Recipient> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return recipientsApi.getFirstPageRecipientList(searchQuery, apiPrefs.user!!.contextId, params)
            .depaginate { recipientsApi.getNextPageRecipientList(it, params) }
            .dataOrThrow
            .filter { it.recipientType == Recipient.Type.Person }
    }

    suspend fun getCourseAnnouncements(forceNetwork: Boolean): List<Pair<Course, DiscussionTopicHeader>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        val courses = getAllInboxCourses(forceNetwork)
        return if (courses.isEmpty()) {
            return emptyList()
        } else {
            announcementsApi.getFirstPageAnnouncements(
                courseCode = courses.map { it.contextId }.toTypedArray(),
                startDate = Calendar.getInstance()
                    .apply { set(Calendar.YEAR, get(Calendar.YEAR) - 1) }.time.toApiString(),
                endDate = Date().toApiString(),
                params = params
            )
            .depaginate { announcementsApi.getNextPageAnnouncementsList(it, params) }
            .dataOrThrow
            .map { announcement ->
                Pair(
                    courses.first { course -> course.contextId == announcement.contextCode },
                    announcement
                )
            }
        }
    }

    suspend fun getAccountAnnouncements(forceNetwork: Boolean): List<AccountNotification> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return accountNotificationApi.getAccountNotifications(params, true, true)
            .depaginate { accountNotificationApi.getNextPageNotifications(it, params) }
            .dataOrThrow
    }

    private suspend fun getAllInboxCourses(forceNetwork: Boolean): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return courseApi.getFirstPageCoursesInbox(params).depaginate {
            courseApi.next(it, params)
        }.dataOrThrow
    }

    fun invalidateConversationListCachedResponse() {
        CanvasRestAdapter.clearCacheUrls("conversations")
    }
}