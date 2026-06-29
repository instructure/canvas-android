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
package com.instructure.horizon.offline.sync

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.toDate
import com.instructure.horizon.data.datasource.AnnouncementLocalDataSource
import com.instructure.horizon.data.datasource.AnnouncementNetworkDataSource
import com.instructure.horizon.database.entity.ANNOUNCEMENT_TYPE_COURSE
import com.instructure.horizon.database.entity.announcementKey
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementComment
import com.instructure.horizon.model.HorizonAnnouncementKind
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class AnnouncementSyncer @Inject constructor(
    private val networkDataSource: AnnouncementNetworkDataSource,
    private val localDataSource: AnnouncementLocalDataSource,
) {
    suspend fun sync() {
        val courses = networkDataSource.getCoursesInProgress(forceRefresh = true)

        val courseAnnouncements = networkDataSource.getCourseAnnouncements(
            courses = courses,
            forceRefresh = true,
            shouldLoginOnTokenError = false,
        ).map { it.toDomain(courses) }

        val accountAnnouncements = networkDataSource.getAccountNotifications(
            forceRefresh = true,
            shouldLoginOnTokenError = false,
        ).map { it.toDomain() }

        val comments = mutableMapOf<String, List<HorizonAnnouncementComment>>()
        for (announcement in courseAnnouncements) {
            val courseId = announcement.courseId ?: continue
            try {
                val topic = networkDataSource.getAnnouncementTopic(
                    courseId = courseId,
                    announcementId = announcement.id,
                    forceRefresh = true,
                    shouldLoginOnTokenError = false,
                )
                val key = announcementKey(ANNOUNCEMENT_TYPE_COURSE, announcement.id, courseId)
                comments[key] = topic.toComments()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Topic fetch failure is non-fatal for the announcement
            }
        }

        localDataSource.replaceAll(courseAnnouncements + accountAnnouncements, comments)
    }

    private fun DiscussionTopicHeader.toDomain(courses: List<CourseWithProgress>): HorizonAnnouncement {
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
