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
package com.instructure.horizon.features.dashboard.widget.announcement

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.models.DiscussionTopicHeader.ReadState
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class DashboardAnnouncementBannerRepository @Inject constructor(
    private val announcementApi: AnnouncementAPI.AnnouncementInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface,
    private val getCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getUnreadAnnouncements(forceRefresh: Boolean): List<AnnouncementBannerItem> {
        val courseAnnouncements = getUnreadCourseAnnouncements(forceRefresh)
        val globalAnnouncements = getUnreadGlobalAnnouncements(forceRefresh)

        return (courseAnnouncements + globalAnnouncements)
            .sortedByDescending { it.date }
    }

    private suspend fun getUnreadCourseAnnouncements(forceRefresh: Boolean): List<AnnouncementBannerItem> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        val courses = getCourses(forceRefresh)

        if (courses.isEmpty()) {
            return emptyList()
        }

        return announcementApi.getFirstPageAnnouncements(
            courseCode = courses.map { "course_" + it.courseId }.toTypedArray(),
            startDate = Calendar.getInstance()
                .apply { set(Calendar.YEAR, get(Calendar.YEAR) - 1) }.time.toApiString(),
            endDate = Date().toApiString(),
            params = params
        )
            .depaginate { announcementApi.getNextPageAnnouncementsList(it, params) }
            .dataOrThrow
            .filter { it.status == ReadState.UNREAD }
            .map { announcement ->
                val course = courses.first { it.courseId == announcement.contextCode?.removePrefix("course_")?.toLong() }
                AnnouncementBannerItem(
                    title = announcement.title.orEmpty(),
                    source = course.courseName,
                    date = announcement.postedDate,
                    type = AnnouncementType.COURSE,
                    route =
                        HorizonInboxRoute.InboxDetails.route(
                            type = HorizonInboxItemType.CourseNotification,
                            id = announcement.id,
                            courseId = announcement.contextCode?.removePrefix("course_")?.toLong()
                        )
                )
            }
    }

    private suspend fun getUnreadGlobalAnnouncements(forceRefresh: Boolean): List<AnnouncementBannerItem> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        val fromDate = Calendar.getInstance()
            .apply { set(Calendar.YEAR, get(Calendar.YEAR) - 1) }.time
        return accountNotificationApi.getAccountNotifications(
            params,
            includePast = true,
            showIsClosed = true
        )
            .depaginate { accountNotificationApi.getNextPageNotifications(it, params) }
            .dataOrThrow
            .filter { !it.closed }
            .filter { it.startDate?.after(fromDate) ?: true }
            .map { notification ->
                AnnouncementBannerItem(
                    title = notification.subject,
                    date = notification.startDate,
                    type = AnnouncementType.GLOBAL,
                    route =
                        HorizonInboxRoute.InboxDetails.route(
                            type = HorizonInboxItemType.AccountNotification,
                            id = notification.id,
                            courseId = null
                        )
                )
            }
    }

    suspend fun getCourses(forceNetwork: Boolean): List<CourseWithProgress> {
        return getCoursesManager.getCoursesWithProgress(apiPrefs.user!!.id, forceNetwork).dataOrThrow
    }
}
