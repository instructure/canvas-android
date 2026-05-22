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

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class AnnouncementNetworkDataSource @Inject constructor(
    private val announcementApi: AnnouncementAPI.AnnouncementInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface,
    private val discussionApi: DiscussionAPI.DiscussionInterface,
    private val getCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getCoursesInProgress(forceRefresh: Boolean): List<CourseWithProgress> {
        return getCoursesManager.getCoursesWithProgress(apiPrefs.user?.id ?: -1L, forceRefresh).dataOrThrow
    }

    suspend fun getCourseAnnouncements(courses: List<CourseWithProgress>, forceRefresh: Boolean): List<DiscussionTopicHeader> {
        if (courses.isEmpty()) return emptyList()
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return announcementApi.getFirstPageAnnouncements(
            courseCode = courses.map { "course_" + it.courseId }.toTypedArray(),
            params = params,
        )
            .depaginate { announcementApi.getNextPageAnnouncementsList(it, params) }
            .dataOrThrow
    }

    suspend fun getCourseAnnouncement(courseId: Long, announcementId: Long, forceRefresh: Boolean): DiscussionTopicHeader {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return announcementApi.getCourseAnnouncement(courseId, announcementId, params).dataOrThrow
    }

    suspend fun getAnnouncementTopic(courseId: Long, announcementId: Long, forceRefresh: Boolean): DiscussionTopic {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return discussionApi.getFullDiscussionTopic(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            announcementId,
            1,
            params,
        ).dataOrThrow
    }

    suspend fun getAccountNotifications(forceRefresh: Boolean): List<AccountNotification> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return accountNotificationApi.getAccountNotifications(params, includePast = true, showIsClosed = true)
            .depaginate { accountNotificationApi.getNextPageNotifications(it, params) }
            .dataOrThrow
    }

    suspend fun deleteAccountNotification(id: Long): DataResult<AccountNotification> {
        return accountNotificationApi.deleteAccountNotification(id, RestParams())
    }

    suspend fun markCourseAnnouncementRead(courseId: Long, announcementId: Long, entryIds: Set<Long>): DataResult<Unit> {
        val params = RestParams()
        val result = discussionApi.markDiscussionTopicRead(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            announcementId,
            params,
        )
        if (result is DataResult.Fail) return result

        val successes = mutableSetOf<Long>()
        entryIds.forEach { entryId ->
            val entryResult = discussionApi.markDiscussionTopicEntryRead(
                CanvasContext.Type.COURSE.apiString,
                courseId,
                announcementId,
                entryId,
                params,
            )
            if (entryResult is DataResult.Success) successes.add(entryId)
        }
        return if (successes == entryIds) DataResult.Success(Unit) else DataResult.Fail()
    }
}
