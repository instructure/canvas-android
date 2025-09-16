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
package com.instructure.horizon.features.notification

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.StreamAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val streamApi: StreamAPI.StreamInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface,
) {
    suspend fun getNotifications(forceRefresh: Boolean): List<StreamItem> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return streamApi.getUserStream(restParams)
            .depaginate { streamApi.getNextPageStream(it, restParams) }
            .dataOrThrow
            .filter { it.isCourseNotification() || it.isDueDateNotification() || it.isNotificationItemScored() }
    }

    suspend fun getGlobalAnnouncements(forceRefresh: Boolean): List<AccountNotification> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return accountNotificationApi.getAccountNotifications(params, true, true)
            .depaginate { accountNotificationApi.getNextPageNotifications(it, params) }
            .dataOrThrow
    }

    suspend fun getCourse(courseId: Long): Course {
        val restParams = RestParams()
        return courseApi.getCourse(courseId, restParams).dataOrThrow
    }
}