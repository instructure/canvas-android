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

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class HorizonInboxComposeRepository @Inject constructor(
    private var courseApi: CourseAPI.CoursesInterface,
    private var recipientApi: RecipientAPI.RecipientInterface,
    private var inboxApi: InboxApi.InboxInterface
) {
    suspend fun getAllInboxCourses(forceNetwork: Boolean): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return courseApi.getFirstPageCoursesInbox(params).depaginate {
            courseApi.next(it, params)
        }.dataOrThrow
    }

    suspend fun getRecipients(courseId: Long, searchQuery: String?, forceNetwork: Boolean = false): List<Recipient> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return recipientApi.getFirstPageRecipientList(searchQuery, courseId.toString(), params)
            .depaginate { recipientApi.getNextPageRecipientList(it, params) }
            .dataOrThrow
            .filter { it.recipientType == Recipient.Type.Person }
    }

    suspend fun createConversation(
        recipientIds: List<String>,
        body: String,
        subject: String,
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
            params = RestParams()
        )
    }

    fun invalidateConversationListCachedResponse() {
        CanvasRestAdapter.clearCacheUrls("conversations")
    }
}