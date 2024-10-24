/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.alerts.details

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import javax.inject.Inject

class AnnouncementDetailsRepository @Inject constructor(
    private val announcementApi: AnnouncementAPI.AnnouncementInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface
) {
    suspend fun getCourse(
        courseId: Long,
        forceNetwork: Boolean
    ): Course? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourse(courseId, restParams).dataOrNull
    }

    suspend fun getCourseAnnouncement(
        courseId: Long,
        announcementId: Long,
        forceNetwork: Boolean
    ): DiscussionTopicHeader {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return announcementApi.getCourseAnnouncement(
            courseId,
            announcementId,
            restParams
        ).dataOrThrow
    }

    suspend fun getGlobalAnnouncement(
        announcementId: Long,
        forceNetwork: Boolean
    ): AccountNotification {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return accountNotificationApi.getAccountNotification(announcementId, restParams).dataOrThrow
    }
}
