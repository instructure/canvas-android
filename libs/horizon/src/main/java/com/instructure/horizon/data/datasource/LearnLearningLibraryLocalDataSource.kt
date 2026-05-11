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
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryModuleInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningRecommendationReason
import com.instructure.canvasapi2.models.journey.learninglibrary.RecommendationSourceContext
import com.instructure.horizon.database.dao.HorizonLearnBrowseItemDao
import com.instructure.horizon.database.dao.HorizonLearnCollectionDao
import com.instructure.horizon.database.dao.HorizonLearnRecommendationDao
import com.instructure.horizon.database.dao.HorizonLearnSavedItemDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonLearnBrowseItemEntity
import com.instructure.horizon.database.entity.HorizonLearnCollectionEntity
import com.instructure.horizon.database.entity.HorizonLearnCollectionItemEntity
import com.instructure.horizon.database.entity.HorizonLearnRecommendationEntity
import com.instructure.horizon.database.entity.HorizonLearnSavedItemEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import java.util.Date
import javax.inject.Inject

class LearnLearningLibraryLocalDataSource @Inject constructor(
    private val collectionDao: HorizonLearnCollectionDao,
    private val savedItemDao: HorizonLearnSavedItemDao,
    private val browseItemDao: HorizonLearnBrowseItemDao,
    private val recommendationDao: HorizonLearnRecommendationDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getEnrolledLearningLibraries(): List<EnrolledLearningLibraryCollection> {
        val collections = collectionDao.getAllCollections()
        return collections.map { collection ->
            val items = collectionDao.getItemsByCollectionId(collection.id).mapNotNull { it.toModel() }
            collection.toModel(items)
        }
    }

    suspend fun getCollection(id: String): EnrolledLearningLibraryCollection? {
        val collection = collectionDao.getCollectionById(id) ?: return null
        val items = collectionDao.getItemsByCollectionId(collection.id).mapNotNull { it.toModel() }
        return collection.toModel(items)
    }

    suspend fun saveEnrolledLearningLibraries(collections: List<EnrolledLearningLibraryCollection>) {
        val collectionEntities = collections.map { it.toEntity() }
        val itemEntities = collections.flatMap { collection ->
            collection.items.map { it.toCollectionItemEntity(collection.id) }
        }
        collectionDao.replaceAll(collectionEntities, itemEntities)
        markSynced(SyncDataType.LEARN_LIBRARY_COLLECTIONS)
    }

    suspend fun saveCollection(collection: EnrolledLearningLibraryCollection) {
        collectionDao.replaceCollection(
            collection.toEntity(),
            collection.items.map { it.toCollectionItemEntity(collection.id) },
        )
    }

    suspend fun getSavedItems(): LearningLibraryCollectionItemsResponse {
        val items = savedItemDao.getAll().mapNotNull { it.toModel() }
        return LearningLibraryCollectionItemsResponse(
            items = items,
            pageInfo = offlinePageInfo(items.size),
        )
    }

    suspend fun saveSavedItems(items: List<LearningLibraryCollectionItem>) {
        val entities = items.map { it.toSavedItemEntity() }
        savedItemDao.replaceAll(entities)
        markSynced(SyncDataType.LEARN_SAVED_ITEMS)
    }

    suspend fun getBrowseItems(
        searchQuery: String?,
        typeFilter: CollectionItemType?,
        sortBy: CollectionItemSortOption?,
        bookmarkedOnly: Boolean,
        completedOnly: Boolean,
    ): LearningLibraryCollectionItemsResponse {
        val query = searchQuery?.takeIf { it.isNotBlank() }?.trim()
        val rows = browseItemDao.queryFiltered(
            searchQuery = query,
            itemType = typeFilter?.name,
            bookmarkedOnly = bookmarkedOnly,
            completedOnly = completedOnly,
            sortMode = sortBy?.name,
        )
        val items = rows.mapNotNull { it.toModel() }
        return LearningLibraryCollectionItemsResponse(
            items = items,
            pageInfo = offlinePageInfo(items.size),
        )
    }

    suspend fun saveBrowseItems(items: List<LearningLibraryCollectionItem>) {
        browseItemDao.replaceAll(items.map { it.toBrowseItemEntity() })
        markSynced(SyncDataType.LEARN_LIBRARY_ITEMS)
    }

    suspend fun getRecommendations(): List<LearningLibraryRecommendation> {
        return recommendationDao.getAll().mapNotNull { it.toModel() }
    }

    suspend fun saveRecommendations(recommendations: List<LearningLibraryRecommendation>) {
        val entities = recommendations.mapIndexed { index, recommendation ->
            recommendation.toEntity(index)
        }
        recommendationDao.replaceAll(entities)
        markSynced(SyncDataType.LEARN_LIBRARY_RECOMMENDATIONS)
    }

    suspend fun findItemById(id: String): LearningLibraryCollectionItem? {
        browseItemDao.findById(id)?.toModel()?.let { return it }
        savedItemDao.findById(id)?.toModel()?.let { return it }
        return null
    }

    suspend fun updateBookmark(itemId: String, isBookmarked: Boolean) {
        browseItemDao.updateBookmark(itemId, isBookmarked)
        collectionDao.updateItemBookmark(itemId, isBookmarked)
        recommendationDao.updateBookmark(itemId, isBookmarked)
        if (isBookmarked) {
            val existing = savedItemDao.findById(itemId)
            if (existing == null) {
                browseItemDao.findById(itemId)?.let { browse ->
                    savedItemDao.insertAll(listOf(browse.toSavedItemEntity()))
                }
            } else {
                savedItemDao.updateBookmark(itemId, true)
            }
        } else {
            savedItemDao.deleteById(itemId)
        }
    }

    private suspend fun markSynced(type: SyncDataType) {
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = type,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    private fun offlinePageInfo(totalCount: Int) = LearningLibraryPageInfo(
        nextCursor = null,
        previousCursor = null,
        hasNextPage = false,
        hasPreviousPage = false,
        totalCount = totalCount,
        pageCursors = null,
    )

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

    private fun HorizonLearnCollectionItemEntity.toModel(): LearningLibraryCollectionItem? {
        val resolvedItemType = CollectionItemType.safeValueOf(itemType) ?: return null
        return LearningLibraryCollectionItem(
            id = id,
            libraryId = libraryId,
            itemType = resolvedItemType,
            displayOrder = displayOrder,
            canvasCourse = canvasCourseInfo(
                canvasCourseId, canvasUrl, courseName, courseImageUrl,
                moduleCount, moduleItemCount, estimatedDurationMinutes,
            ),
            moduleInfo = moduleInfo(moduleId, moduleItemId, moduleItemType, resourceId),
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

    private fun HorizonLearnSavedItemEntity.toModel(): LearningLibraryCollectionItem? {
        val resolvedItemType = CollectionItemType.safeValueOf(itemType) ?: return null
        return LearningLibraryCollectionItem(
            id = id,
            libraryId = libraryId,
            itemType = resolvedItemType,
            displayOrder = displayOrder,
            canvasCourse = canvasCourseInfo(
                canvasCourseId, canvasUrl, courseName, courseImageUrl,
                moduleCount, moduleItemCount, estimatedDurationMinutes,
            ),
            moduleInfo = moduleInfo(moduleId, moduleItemId, moduleItemType, resourceId),
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

    private fun HorizonLearnBrowseItemEntity.toModel(): LearningLibraryCollectionItem? {
        val resolvedItemType = CollectionItemType.safeValueOf(itemType) ?: return null
        return LearningLibraryCollectionItem(
            id = id,
            libraryId = libraryId,
            itemType = resolvedItemType,
            displayOrder = displayOrder,
            canvasCourse = canvasCourseInfo(
                canvasCourseId, canvasUrl, courseName, courseImageUrl,
                moduleCount, moduleItemCount, estimatedDurationMinutes,
            ),
            moduleInfo = moduleInfo(moduleId, moduleItemId, moduleItemType, resourceId),
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

    private fun HorizonLearnBrowseItemEntity.toSavedItemEntity(): HorizonLearnSavedItemEntity {
        return HorizonLearnSavedItemEntity(
            id = id,
            libraryId = libraryId,
            itemType = itemType,
            displayOrder = displayOrder,
            canvasCourseId = canvasCourseId,
            canvasUrl = canvasUrl,
            courseName = courseName,
            courseImageUrl = courseImageUrl,
            moduleCount = moduleCount,
            moduleItemCount = moduleItemCount,
            estimatedDurationMinutes = estimatedDurationMinutes,
            moduleId = moduleId,
            moduleItemId = moduleItemId,
            moduleItemType = moduleItemType,
            resourceId = resourceId,
            programId = programId,
            programCourseId = programCourseId,
            createdAtMs = createdAtMs,
            updatedAtMs = updatedAtMs,
            isBookmarked = true,
            completionPercentage = completionPercentage,
            isEnrolledInCanvas = isEnrolledInCanvas,
            canvasEnrollmentId = canvasEnrollmentId,
        )
    }

    private fun HorizonLearnRecommendationEntity.toModel(): LearningLibraryRecommendation? {
        val resolvedItemType = CollectionItemType.safeValueOf(itemType) ?: return null
        val item = LearningLibraryCollectionItem(
            id = itemId,
            libraryId = libraryId,
            itemType = resolvedItemType,
            displayOrder = itemDisplayOrder,
            canvasCourse = canvasCourseInfo(
                canvasCourseId, canvasUrl, courseName, courseImageUrl,
                moduleCount, moduleItemCount, estimatedDurationMinutes,
            ),
            moduleInfo = moduleInfo(moduleId, moduleItemId, moduleItemType, resourceId),
            programId = programId,
            programCourseId = programCourseId,
            createdAt = Date(createdAtMs),
            updatedAt = Date(updatedAtMs),
            isBookmarked = isBookmarked,
            completionPercentage = completionPercentage,
            isEnrolledInCanvas = isEnrolledInCanvas,
            canvasEnrollmentId = canvasEnrollmentId,
        )
        return LearningLibraryRecommendation(
            courseId = courseId.orEmpty(),
            primaryReason = primaryReason?.let { runCatching { LearningRecommendationReason.valueOf(it) }.getOrNull() },
            sourceContext = if (sourceCourseId != null || sourceCourseName != null || sourceSkillName != null) {
                RecommendationSourceContext(
                    sourceCourseId = sourceCourseId,
                    sourceCourseName = sourceCourseName,
                    sourceSkillName = sourceSkillName,
                )
            } else null,
            popularityCount = popularityCount ?: 0,
            item = item,
        )
    }

    private fun canvasCourseInfo(
        canvasCourseId: String?,
        canvasUrl: String?,
        courseName: String?,
        courseImageUrl: String?,
        moduleCount: Double?,
        moduleItemCount: Double?,
        estimatedDurationMinutes: Double?,
    ): CanvasCourseInfo? {
        if (canvasCourseId == null || canvasUrl == null) return null
        return CanvasCourseInfo(
            courseId = canvasCourseId,
            canvasUrl = canvasUrl,
            courseName = courseName,
            courseImageUrl = courseImageUrl,
            moduleCount = moduleCount ?: 0.0,
            moduleItemCount = moduleItemCount ?: 0.0,
            estimatedDurationMinutes = estimatedDurationMinutes,
        )
    }

    private fun moduleInfo(
        moduleId: String?,
        moduleItemId: String?,
        moduleItemType: String?,
        resourceId: String?,
    ): LearningLibraryModuleInfo? {
        if (moduleItemId == null) return null
        return LearningLibraryModuleInfo(
            moduleId = moduleId,
            moduleItemId = moduleItemId,
            moduleItemType = moduleItemType,
            resourceId = resourceId,
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

    private fun LearningLibraryCollectionItem.toBrowseItemEntity(): HorizonLearnBrowseItemEntity {
        return HorizonLearnBrowseItemEntity(
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

    private fun LearningLibraryRecommendation.toEntity(displayOrder: Int): HorizonLearnRecommendationEntity {
        return HorizonLearnRecommendationEntity(
            itemId = item.id,
            displayOrder = displayOrder,
            courseId = courseId.takeIf { it.isNotEmpty() },
            primaryReason = primaryReason?.name,
            sourceCourseId = sourceContext?.sourceCourseId,
            sourceCourseName = sourceContext?.sourceCourseName,
            sourceSkillName = sourceContext?.sourceSkillName,
            popularityCount = popularityCount,
            libraryId = item.libraryId,
            itemType = item.itemType.name,
            itemDisplayOrder = item.displayOrder,
            canvasCourseId = item.canvasCourse?.courseId,
            canvasUrl = item.canvasCourse?.canvasUrl,
            courseName = item.canvasCourse?.courseName,
            courseImageUrl = item.canvasCourse?.courseImageUrl,
            moduleCount = item.canvasCourse?.moduleCount,
            moduleItemCount = item.canvasCourse?.moduleItemCount,
            estimatedDurationMinutes = item.canvasCourse?.estimatedDurationMinutes,
            moduleId = item.moduleInfo?.moduleId,
            moduleItemId = item.moduleInfo?.moduleItemId,
            moduleItemType = item.moduleInfo?.moduleItemType,
            resourceId = item.moduleInfo?.resourceId,
            programId = item.programId,
            programCourseId = item.programCourseId,
            createdAtMs = item.createdAt.time,
            updatedAtMs = item.updatedAt.time,
            isBookmarked = item.isBookmarked,
            completionPercentage = item.completionPercentage,
            isEnrolledInCanvas = item.isEnrolledInCanvas,
            canvasEnrollmentId = item.canvasEnrollmentId,
        )
    }
}
