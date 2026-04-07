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

import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryModuleInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.horizon.database.dao.HorizonLearnCollectionDao
import com.instructure.horizon.database.dao.HorizonLearnSavedItemDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonLearnCollectionEntity
import com.instructure.horizon.database.entity.HorizonLearnCollectionItemEntity
import com.instructure.horizon.database.entity.HorizonLearnSavedItemEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import java.util.Date
import javax.inject.Inject

class LearnLearningLibraryLocalDataSource @Inject constructor(
    private val collectionDao: HorizonLearnCollectionDao,
    private val savedItemDao: HorizonLearnSavedItemDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getEnrolledLearningLibraries(): List<EnrolledLearningLibraryCollection> {
        val collections = collectionDao.getAllCollections()
        return collections.map { collection ->
            val items = collectionDao.getItemsByCollectionId(collection.id).map { it.toModel() }
            collection.toModel(items)
        }
    }

    suspend fun saveEnrolledLearningLibraries(collections: List<EnrolledLearningLibraryCollection>) {
        val collectionEntities = collections.map { it.toEntity() }
        val itemEntities = collections.flatMap { collection ->
            collection.items.map { it.toCollectionItemEntity(collection.id) }
        }
        collectionDao.replaceAll(collectionEntities, itemEntities)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.LEARN_LIBRARY_COLLECTIONS,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    suspend fun getSavedItems(): LearningLibraryCollectionItemsResponse {
        val items = savedItemDao.getAll().map { it.toModel() }
        return LearningLibraryCollectionItemsResponse(
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

    suspend fun saveSavedItems(items: List<LearningLibraryCollectionItem>) {
        val entities = items.map { it.toSavedItemEntity() }
        savedItemDao.replaceAll(entities)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.LEARN_SAVED_ITEMS,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    private fun HorizonLearnCollectionEntity.toModel(items: List<LearningLibraryCollectionItem>): EnrolledLearningLibraryCollection {
        return EnrolledLearningLibraryCollection(
            id = id,
            name = name,
            publicName = publicName,
            description = description,
            createdAt = Date(createdAtMs),
            updatedAt = Date(updatedAtMs),
            totalItemCount = totalItemCount,
            items = items,
        )
    }

    private fun HorizonLearnCollectionItemEntity.toModel(): LearningLibraryCollectionItem {
        val canvasCourse = if (canvasCourseId != null && canvasUrl != null) {
            CanvasCourseInfo(
                courseId = canvasCourseId,
                canvasUrl = canvasUrl,
                courseName = courseName,
                courseImageUrl = courseImageUrl,
                moduleCount = moduleCount ?: 0.0,
                moduleItemCount = moduleItemCount ?: 0.0,
                estimatedDurationMinutes = estimatedDurationMinutes,
            )
        } else null
        val moduleInfo = if (moduleItemId != null) {
            LearningLibraryModuleInfo(
                moduleId = moduleId,
                moduleItemId = moduleItemId,
                moduleItemType = moduleItemType,
                resourceId = resourceId,
            )
        } else null
        return LearningLibraryCollectionItem(
            id = id,
            libraryId = libraryId,
            itemType = CollectionItemType.valueOf(itemType),
            displayOrder = displayOrder,
            canvasCourse = canvasCourse,
            moduleInfo = moduleInfo,
            programId = programId,
            programCourseId = programCourseId,
            createdAt = Date(createdAtMs),
            updatedAt = Date(updatedAtMs),
            isBookmarked = isBookmarked,
            completionPercentage = completionPercentage,
            isEnrolledInCanvas = isEnrolledInCanvas,
            canvasEnrollmentId = canvasEnrollmentId,
        )
    }

    private fun HorizonLearnSavedItemEntity.toModel(): LearningLibraryCollectionItem {
        val canvasCourse = if (canvasCourseId != null && canvasUrl != null) {
            CanvasCourseInfo(
                courseId = canvasCourseId,
                canvasUrl = canvasUrl,
                courseName = courseName,
                courseImageUrl = courseImageUrl,
                moduleCount = moduleCount ?: 0.0,
                moduleItemCount = moduleItemCount ?: 0.0,
                estimatedDurationMinutes = estimatedDurationMinutes,
            )
        } else null
        val moduleInfo = if (moduleItemId != null) {
            LearningLibraryModuleInfo(
                moduleId = moduleId,
                moduleItemId = moduleItemId,
                moduleItemType = moduleItemType,
                resourceId = resourceId,
            )
        } else null
        return LearningLibraryCollectionItem(
            id = id,
            libraryId = libraryId,
            itemType = CollectionItemType.valueOf(itemType),
            displayOrder = displayOrder,
            canvasCourse = canvasCourse,
            moduleInfo = moduleInfo,
            programId = programId,
            programCourseId = programCourseId,
            createdAt = Date(createdAtMs),
            updatedAt = Date(updatedAtMs),
            isBookmarked = isBookmarked,
            completionPercentage = completionPercentage,
            isEnrolledInCanvas = isEnrolledInCanvas,
            canvasEnrollmentId = canvasEnrollmentId,
        )
    }

    private fun EnrolledLearningLibraryCollection.toEntity(): HorizonLearnCollectionEntity {
        return HorizonLearnCollectionEntity(
            id = id,
            name = name,
            publicName = publicName,
            description = description,
            createdAtMs = createdAt.time,
            updatedAtMs = updatedAt.time,
            totalItemCount = totalItemCount,
        )
    }

    private fun LearningLibraryCollectionItem.toCollectionItemEntity(collectionId: String): HorizonLearnCollectionItemEntity {
        return HorizonLearnCollectionItemEntity(
            id = id,
            collectionId = collectionId,
            libraryId = libraryId,
            itemType = itemType.name,
            displayOrder = displayOrder,
            canvasCourseId = canvasCourse?.courseId,
            canvasUrl = canvasCourse?.canvasUrl,
            courseName = canvasCourse?.courseName,
            courseImageUrl = canvasCourse?.courseImageUrl,
            moduleCount = canvasCourse?.moduleCount,
            moduleItemCount = canvasCourse?.moduleItemCount,
            estimatedDurationMinutes = canvasCourse?.estimatedDurationMinutes,
            moduleId = moduleInfo?.moduleId,
            moduleItemId = moduleInfo?.moduleItemId,
            moduleItemType = moduleInfo?.moduleItemType,
            resourceId = moduleInfo?.resourceId,
            programId = programId,
            programCourseId = programCourseId,
            createdAtMs = createdAt.time,
            updatedAtMs = updatedAt.time,
            isBookmarked = isBookmarked,
            completionPercentage = completionPercentage,
            isEnrolledInCanvas = isEnrolledInCanvas,
            canvasEnrollmentId = canvasEnrollmentId,
        )
    }

    private fun LearningLibraryCollectionItem.toSavedItemEntity(): HorizonLearnSavedItemEntity {
        return HorizonLearnSavedItemEntity(
            id = id,
            libraryId = libraryId,
            itemType = itemType.name,
            displayOrder = displayOrder,
            canvasCourseId = canvasCourse?.courseId,
            canvasUrl = canvasCourse?.canvasUrl,
            courseName = canvasCourse?.courseName,
            courseImageUrl = canvasCourse?.courseImageUrl,
            moduleCount = canvasCourse?.moduleCount,
            moduleItemCount = canvasCourse?.moduleItemCount,
            estimatedDurationMinutes = canvasCourse?.estimatedDurationMinutes,
            moduleId = moduleInfo?.moduleId,
            moduleItemId = moduleInfo?.moduleItemId,
            moduleItemType = moduleInfo?.moduleItemType,
            resourceId = moduleInfo?.resourceId,
            programId = programId,
            programCourseId = programCourseId,
            createdAtMs = createdAt.time,
            updatedAtMs = updatedAt.time,
            isBookmarked = isBookmarked,
            completionPercentage = completionPercentage,
            isEnrolledInCanvas = isEnrolledInCanvas,
            canvasEnrollmentId = canvasEnrollmentId,
        )
    }
}
