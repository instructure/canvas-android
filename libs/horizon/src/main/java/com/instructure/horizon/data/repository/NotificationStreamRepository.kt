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

import com.instructure.canvasapi2.models.StreamItem
import com.instructure.horizon.data.datasource.NotificationStreamLocalDataSource
import com.instructure.horizon.data.datasource.NotificationStreamNetworkDataSource
import com.instructure.horizon.model.NotificationStreamItem
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class NotificationStreamRepository @Inject constructor(
    private val networkDataSource: NotificationStreamNetworkDataSource,
    private val localDataSource: NotificationStreamLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getNotifications(forceRefresh: Boolean): List<NotificationStreamItem> {
        return if (shouldFetchFromNetwork()) {
            val courseIds = networkDataSource.getCoursesInProgressIds(forceRefresh).toSet()
            val streamItems = networkDataSource.getStream(forceRefresh)
                .filter { it.courseId == -1L || courseIds.contains(it.courseId) }
                .filter { it.isRelevantNotification() }

            val items = streamItems.map { item ->
                val courseName = if (item.courseId > 0L) {
                    localDataSource.courseNameFor(item.courseId)
                        ?: runCatching { networkDataSource.getCourse(item.courseId).name }.getOrNull()
                } else null
                item.toDomain(courseName)
            }.sortedByDescending { it.updatedAt }

            if (shouldSync()) {
                localDataSource.replaceAll(items)
            }
            items
        } else {
            localDataSource.getStream()
        }
    }

    private fun StreamItem.isRelevantNotification(): Boolean {
        val isScored = grade != null || score != -1.0
        val isDueDate = notificationCategory == "Due Date"
        val isGradingPolicy = notificationCategory == "Grading Policies"
        return isDueDate || isScored || isGradingPolicy
    }

    private fun StreamItem.toDomain(courseName: String?): NotificationStreamItem {
        return NotificationStreamItem(
            id = id,
            type = type,
            notificationCategory = notificationCategory,
            title = title.orEmpty(),
            courseId = courseId,
            courseName = courseName,
            isRead = isReadState,
            score = score,
            grade = grade,
            updatedAt = updatedDate,
            htmlUrl = htmlUrl,
            assignmentHtmlUrl = assignment?.htmlUrl,
        )
    }
}
