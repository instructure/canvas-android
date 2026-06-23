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

import com.instructure.horizon.database.dao.HorizonDashboardEnrollmentDao
import com.instructure.horizon.database.dao.HorizonNotificationStreamItemDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonNotificationStreamItemEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.horizon.model.NotificationStreamItem
import javax.inject.Inject

class NotificationStreamLocalDataSource @Inject constructor(
    private val notificationDao: HorizonNotificationStreamItemDao,
    private val enrollmentDao: HorizonDashboardEnrollmentDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {
    suspend fun getStream(): List<NotificationStreamItem> {
        return notificationDao.getAll().map { it.toDomain() }
    }

    suspend fun replaceAll(items: List<NotificationStreamItem>) {
        notificationDao.replaceAll(items.map { it.toEntity() })
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.NOTIFICATION_STREAM,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    suspend fun courseNameFor(courseId: Long): String? {
        return enrollmentDao.getAll().firstOrNull { it.courseId == courseId }?.courseName
    }

    private fun HorizonNotificationStreamItemEntity.toDomain() = NotificationStreamItem(
        id = id,
        type = type,
        notificationCategory = notificationCategory,
        title = title,
        courseId = courseId,
        courseName = courseName,
        isRead = isRead,
        score = score,
        grade = grade,
        updatedAt = updatedAtMs?.let { java.util.Date(it) },
        htmlUrl = htmlUrl,
        assignmentHtmlUrl = assignmentHtmlUrl,
    )

    private fun NotificationStreamItem.toEntity() = HorizonNotificationStreamItemEntity(
        id = id,
        type = type,
        notificationCategory = notificationCategory,
        title = title,
        courseId = courseId,
        courseName = courseName,
        isRead = isRead,
        score = score,
        grade = grade,
        updatedAtMs = updatedAt?.time,
        htmlUrl = htmlUrl,
        assignmentHtmlUrl = assignmentHtmlUrl,
    )
}
