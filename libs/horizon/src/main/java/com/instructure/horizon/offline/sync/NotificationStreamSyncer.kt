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

import com.instructure.canvasapi2.models.StreamItem
import com.instructure.horizon.data.datasource.NotificationStreamLocalDataSource
import com.instructure.horizon.data.datasource.NotificationStreamNetworkDataSource
import com.instructure.horizon.model.NotificationStreamItem
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class NotificationStreamSyncer @Inject constructor(
    private val networkDataSource: NotificationStreamNetworkDataSource,
    private val localDataSource: NotificationStreamLocalDataSource,
) {
    suspend fun sync() {
        val courseIds = networkDataSource.getCoursesInProgressIds(forceRefresh = true).toSet()
        val streamItems = networkDataSource.getStream(
            forceRefresh = true,
            shouldLoginOnTokenError = false,
        )
            .filter { it.courseId == -1L || courseIds.contains(it.courseId) }
            .filter { it.isRelevant() }

        val items = streamItems.map { item ->
            val courseName = if (item.courseId > 0L) {
                localDataSource.courseNameFor(item.courseId) ?: try {
                    networkDataSource.getCourse(
                        courseId = item.courseId,
                        shouldLoginOnTokenError = false,
                    ).name
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            } else null
            NotificationStreamItem(
                id = item.id,
                type = item.type,
                notificationCategory = item.notificationCategory,
                title = item.title.orEmpty(),
                courseId = item.courseId,
                courseName = courseName,
                isRead = item.isReadState,
                score = item.score,
                grade = item.grade,
                updatedAt = item.updatedDate,
                htmlUrl = item.htmlUrl,
                assignmentHtmlUrl = item.assignment?.htmlUrl,
            )
        }.sortedByDescending { it.updatedAt }

        localDataSource.replaceAll(items)
    }

    private fun StreamItem.isRelevant(): Boolean {
        val isScored = grade != null || score != -1.0
        val isDueDate = notificationCategory == NOTIFICATION_CATEGORY_DUE_DATE
        val isGradingPolicy = notificationCategory == NOTIFICATION_CATEGORY_GRADING_POLICIES
        return isDueDate || isScored || isGradingPolicy
    }

    companion object {
        private const val NOTIFICATION_CATEGORY_DUE_DATE = "Due Date"
        private const val NOTIFICATION_CATEGORY_GRADING_POLICIES = "Grading Policies"
    }
}
