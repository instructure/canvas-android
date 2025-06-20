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

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import javax.inject.Inject

class HorizonInboxDetailsRepository @Inject constructor(
    private val inboxApi: InboxApi.InboxInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface,
    private val announcementApi: AnnouncementAPI.AnnouncementInterface,
    private val discussionApi: DiscussionAPI.DiscussionInterface
) {
    suspend fun getConversation(id: Long, markAsRead: Boolean = true, forceRefresh: Boolean): Conversation {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return inboxApi.getConversation(id, markAsRead, params).dataOrThrow
    }

    suspend fun getAccountAnnouncement(id: Long, forceRefresh: Boolean): AccountNotification {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return accountNotificationApi.getAccountNotification(id, params).dataOrThrow
    }

    suspend fun getAnnouncement(id: Long, courseId: Long, forceRefresh: Boolean): DiscussionTopicHeader {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return announcementApi.getCourseAnnouncement(courseId, id, params).dataOrThrow
    }

    suspend fun getAnnouncementTopic(id: Long, courseId: Long, forceRefresh: Boolean): DiscussionTopic {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return discussionApi.getFullDiscussionTopic(CanvasContext.Type.COURSE.apiString, courseId, id, 1, params).dataOrThrow
    }
}