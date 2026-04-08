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

import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.models.journey.mycontent.CourseEnrollmentItem
import com.instructure.canvasapi2.models.journey.mycontent.LearnItem
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import com.instructure.canvasapi2.models.journey.mycontent.ProgramEnrollmentItem
import com.instructure.horizon.database.dao.HorizonLearnItemDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonLearnItemEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import java.util.Date
import javax.inject.Inject

class LearnMyContentLocalDataSource @Inject constructor(
    private val learnItemDao: HorizonLearnItemDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getLearnItems(
        queryKey: String,
        searchQuery: String?,
        sortBy: CollectionItemSortOption?,
        itemTypes: List<LearnItemType>?,
        cursor: String?,
    ): LearnItemsResponse {
        var items = learnItemDao.getByQueryKey(queryKey).map { it.toModel() }

        if (!itemTypes.isNullOrEmpty()) {
            items = items.filter { item ->
                itemTypes.any { type ->
                    when (type) {
                        LearnItemType.PROGRAM -> item is ProgramEnrollmentItem
                        LearnItemType.COURSE -> item is CourseEnrollmentItem
                    }
                }
            }
        }

        if (!searchQuery.isNullOrBlank()) {
            items = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        items = when (sortBy) {
            CollectionItemSortOption.NAME_A_Z -> items.sortedBy { it.name }
            CollectionItemSortOption.NAME_Z_A -> items.sortedByDescending { it.name }
            CollectionItemSortOption.MOST_RECENT -> items.sortedByDescending { it.enrolledAt?.time ?: 0L }
            CollectionItemSortOption.LEAST_RECENT -> items.sortedBy { it.enrolledAt?.time ?: Long.MAX_VALUE }
            null -> items.sortedBy { it.position }
        }

        val offset = cursor?.toIntOrNull() ?: 0
        val page = items.drop(offset).take(PAGE_SIZE)
        val hasNextPage = offset + PAGE_SIZE < items.size
        return LearnItemsResponse(
            items = page,
            pageInfo = LearningLibraryPageInfo(
                nextCursor = if (hasNextPage) (offset + PAGE_SIZE).toString() else null,
                previousCursor = null,
                hasNextPage = hasNextPage,
                hasPreviousPage = false,
                totalCount = items.size,
                pageCursors = null,
            )
        )
    }

    suspend fun saveLearnItems(items: List<LearnItem>, queryKey: String) {
        learnItemDao.replaceByQueryKey(items.map { it.toEntity(queryKey) }, queryKey)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = syncDataTypeFor(queryKey),
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    private fun syncDataTypeFor(queryKey: String): SyncDataType = when (queryKey) {
        QUERY_KEY_IN_PROGRESS -> SyncDataType.LEARN_MY_CONTENT_IN_PROGRESS
        QUERY_KEY_COMPLETED -> SyncDataType.LEARN_MY_CONTENT_COMPLETED
        else -> throw IllegalArgumentException("Unknown queryKey: $queryKey")
    }

    companion object {
        const val QUERY_KEY_IN_PROGRESS = "IN_PROGRESS"
        const val QUERY_KEY_COMPLETED = "COMPLETED"
        private const val PAGE_SIZE = 4
    }

    private fun HorizonLearnItemEntity.toModel(): LearnItem {
        return when (itemType) {
            LearnItemType.PROGRAM.name -> ProgramEnrollmentItem(
                id = id,
                name = name,
                position = position,
                enrolledAt = enrolledAtMs?.let { Date(it) },
                completionPercentage = completionPercentage,
                startDate = startDateMs?.let { Date(it) },
                endDate = endDateMs?.let { Date(it) },
                status = enrollmentStatus.orEmpty(),
                description = description,
                variant = variant.orEmpty(),
                estimatedDurationMinutes = estimatedDurationMinutes,
                courseCount = courseCount ?: 0,
            )
            else -> CourseEnrollmentItem(
                id = id,
                name = name,
                position = position,
                enrolledAt = enrolledAtMs?.let { Date(it) },
                completionPercentage = completionPercentage,
                startAt = startAtMs?.let { Date(it) },
                endAt = endAtMs?.let { Date(it) },
                requirementCount = requirementCount,
                requirementCompletedCount = requirementCompletedCount,
                completedAt = completedAtMs?.let { Date(it) },
                grade = grade,
                imageUrl = imageUrl,
                workflowState = workflowState.orEmpty(),
                lastActivityAt = lastActivityAtMs?.let { Date(it) },
            )
        }
    }

    private fun LearnItem.toEntity(queryKey: String): HorizonLearnItemEntity {
        return when (this) {
            is ProgramEnrollmentItem -> HorizonLearnItemEntity(
                id = id,
                queryKey = queryKey,
                itemType = LearnItemType.PROGRAM.name,
                name = name,
                position = position,
                enrolledAtMs = enrolledAt?.time,
                completionPercentage = completionPercentage,
                startDateMs = startDate?.time,
                endDateMs = endDate?.time,
                enrollmentStatus = status,
                description = description,
                variant = variant,
                estimatedDurationMinutes = estimatedDurationMinutes,
                courseCount = courseCount,
                startAtMs = null,
                endAtMs = null,
                requirementCount = null,
                requirementCompletedCount = null,
                completedAtMs = null,
                grade = null,
                imageUrl = null,
                workflowState = null,
                lastActivityAtMs = null,
            )
            is CourseEnrollmentItem -> HorizonLearnItemEntity(
                id = id,
                queryKey = queryKey,
                itemType = LearnItemType.COURSE.name,
                name = name,
                position = position,
                enrolledAtMs = enrolledAt?.time,
                completionPercentage = completionPercentage,
                startDateMs = null,
                endDateMs = null,
                enrollmentStatus = null,
                description = null,
                variant = null,
                estimatedDurationMinutes = null,
                courseCount = null,
                startAtMs = startAt?.time,
                endAtMs = endAt?.time,
                requirementCount = requirementCount,
                requirementCompletedCount = requirementCompletedCount,
                completedAtMs = completedAt?.time,
                grade = grade,
                imageUrl = imageUrl,
                workflowState = workflowState,
                lastActivityAtMs = lastActivityAt?.time,
            )
        }
    }
}
