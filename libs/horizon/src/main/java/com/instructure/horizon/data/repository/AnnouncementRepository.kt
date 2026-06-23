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

import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toDate
import com.instructure.horizon.data.datasource.AnnouncementLocalDataSource
import com.instructure.horizon.data.datasource.AnnouncementNetworkDataSource
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementComment
import com.instructure.horizon.model.HorizonAnnouncementDetails
import com.instructure.horizon.model.HorizonAnnouncementKind
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class AnnouncementRepository @Inject constructor(
    private val networkDataSource: AnnouncementNetworkDataSource,
    private val localDataSource: AnnouncementLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getUnreadAnnouncements(forceRefresh: Boolean): List<HorizonAnnouncement> {
        return if (shouldFetchFromNetwork()) {
            val courses = networkDataSource.getCoursesInProgress(forceRefresh)
            val courseAnnouncements = networkDataSource.getCourseAnnouncements(courses, forceRefresh)
                .map { it.toDomain(courses) }
                .filter { it.isUnread }
            val accountAnnouncements = networkDataSource.getAccountNotifications(forceRefresh)
                .map { it.toDomain() }
                .filter { !it.isClosed }
            courseAnnouncements + accountAnnouncements
        } else {
            localDataSource.getUnread().filter { !it.isClosed }
        }
    }

    suspend fun getCourseAnnouncementDetails(
        courseId: Long,
        announcementId: Long,
        forceRefresh: Boolean,
    ): HorizonAnnouncementDetails {
        return if (shouldFetchFromNetwork()) {
            val courses = networkDataSource.getCoursesInProgress(forceRefresh = false)
            val announcement = networkDataSource.getCourseAnnouncement(courseId, announcementId, forceRefresh)
                .toDomain(courses)
            val topic = networkDataSource.getAnnouncementTopic(courseId, announcementId, forceRefresh)
            val comments = topic.toComments()
            HorizonAnnouncementDetails(announcement, comments)
        } else {
            localDataSource.getDetails(HorizonAnnouncementKind.COURSE, announcementId, courseId)
                ?: error("Announcement $announcementId not available offline")
        }
    }

    suspend fun getAccountAnnouncementDetails(announcementId: Long, forceRefresh: Boolean): HorizonAnnouncementDetails {
        return if (shouldFetchFromNetwork()) {
            val notification = networkDataSource.getAccountNotifications(forceRefresh)
                .firstOrNull { it.id == announcementId }
                ?: error("Account notification $announcementId not found")
            HorizonAnnouncementDetails(notification.toDomain(), emptyList())
        } else {
            localDataSource.getDetails(HorizonAnnouncementKind.ACCOUNT, announcementId, courseId = null)
                ?: error("Account notification $announcementId not available offline")
        }
    }

    suspend fun markCourseAnnouncementRead(courseId: Long, announcementId: Long, entryIds: Set<Long>): DataResult<Unit> {
        if (!shouldFetchFromNetwork()) return DataResult.Fail()
        val result = networkDataSource.markCourseAnnouncementRead(courseId, announcementId, entryIds)
        if (result is DataResult.Success && shouldSync()) {
            localDataSource.markRead(HorizonAnnouncementKind.COURSE, announcementId, courseId)
        }
        return result
    }

    suspend fun dismissAccountNotification(announcementId: Long): DataResult<AccountNotification> {
        if (!shouldFetchFromNetwork()) return DataResult.Fail()
        val result = networkDataSource.deleteAccountNotification(announcementId)
        if (result is DataResult.Success && shouldSync()) {
            localDataSource.markClosed(HorizonAnnouncementKind.ACCOUNT, announcementId, courseId = null)
        }
        return result
    }

    private fun DiscussionTopicHeader.toDomain(courses: List<com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress>): HorizonAnnouncement {
        val resolvedCourseId = contextCode?.removePrefix("course_")?.toLongOrNull()
        val course = courses.firstOrNull { it.courseId == resolvedCourseId }
        return HorizonAnnouncement(
            id = id,
            kind = HorizonAnnouncementKind.COURSE,
            courseId = resolvedCourseId,
            courseName = course?.courseName,
            title = title.orEmpty(),
            message = message.orEmpty(),
            authorDisplayName = author?.displayName,
            postedAt = postedDate,
            isUnread = status == DiscussionTopicHeader.ReadState.UNREAD,
            isClosed = false,
        )
    }

    private fun AccountNotification.toDomain(): HorizonAnnouncement {
        return HorizonAnnouncement(
            id = id,
            kind = HorizonAnnouncementKind.ACCOUNT,
            courseId = null,
            courseName = null,
            title = subject,
            message = message,
            authorDisplayName = null,
            postedAt = startDate,
            isUnread = !closed,
            isClosed = closed,
        )
    }

    private fun DiscussionTopic.toComments(): List<HorizonAnnouncementComment> {
        return views.map { entry ->
            HorizonAnnouncementComment(
                id = entry.id,
                authorDisplayName = entry.author?.displayName,
                message = entry.message.orEmpty(),
                createdAt = entry.createdAt.toDate(),
            )
        }
    }
}
