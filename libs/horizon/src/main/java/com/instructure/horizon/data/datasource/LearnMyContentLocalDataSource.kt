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

    suspend fun getLearnItems(queryKey: String): LearnItemsResponse {
        val items = learnItemDao.getByQueryKey(queryKey).map { it.toModel() }
        return LearnItemsResponse(
            items = items,
            pageInfo = LearningLibraryPageInfo(
                nextCursor = null,
                previousCursor = null,
                hasNextPage = false,
                hasPreviousPage = false,
                totalCount = items.size,
                pageCursors = null,
            )
        )
    }

    suspend fun saveLearnItems(items: List<LearnItem>, queryKey: String) {
        val entities = items.map { it.toEntity(queryKey) }
        learnItemDao.replaceByQueryKey(entities, queryKey)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.LEARN_MY_CONTENT_ITEMS,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
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
