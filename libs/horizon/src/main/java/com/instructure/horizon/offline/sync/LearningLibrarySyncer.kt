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

import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.horizon.data.datasource.LearnLearningLibraryLocalDataSource
import com.instructure.horizon.data.datasource.LearnLearningLibraryNetworkDataSource
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonGlobalSyncPlanDao
import com.instructure.horizon.database.entity.HorizonGlobalSyncPlanEntity
import javax.inject.Inject

/**
 * Syncs the full learning library to the local DB for offline browsing.
 *
 * Steps:
 *  1. Depaginate enrolled collections (with all their items).
 *  2. Depaginate the global browse list of items.
 *  3. Depaginate bookmarked items.
 *  4. Sync recommendations.
 *  5. Sync course content (modules + pages + files + assignments) for every course
 *     referenced by non-disabled items, except for courses already in the per-course
 *     sync plan (those are handled by HorizonCourseSync's per-course loop).
 *  6. Sync all referenced images.
 */
class LearningLibrarySyncer @Inject constructor(
    private val networkDataSource: LearnLearningLibraryNetworkDataSource,
    private val localDataSource: LearnLearningLibraryLocalDataSource,
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val globalSyncPlanDao: HorizonGlobalSyncPlanDao,
    private val courseContentSyncer: CourseContentSyncer,
    private val assignmentSyncer: AssignmentSyncer,
    private val pageSyncer: PageSyncer,
    private val fileSyncer: FileSyncer,
    private val imageSyncer: ImageSyncer,
) {

    suspend fun sync() {
        runCatching { updateLearningLibraryState(HorizonProgressState.IN_PROGRESS) }
        var finalState = HorizonProgressState.COMPLETED
        try {
            val collections = runCatching { syncCollections() }
                .onFailure { finalState = HorizonProgressState.ERROR }
                .getOrDefault(emptyList())
            val browseItems = runCatching { syncBrowseItems() }
                .onFailure { finalState = HorizonProgressState.ERROR }
                .getOrDefault(emptyList())
            runCatching { syncBookmarkedItems() }
                .onFailure { finalState = HorizonProgressState.ERROR }
            val recommendations = runCatching { syncRecommendations() }
                .onFailure { finalState = HorizonProgressState.ERROR }
                .getOrDefault(emptyList())

            runCatching {
                syncReferencedCourseContent(browseItems + collections.flatMap { it.items })
            }.onFailure { finalState = HorizonProgressState.ERROR }

            runCatching { syncImages(browseItems, collections.flatMap { it.items }, recommendations) }
                .onFailure { finalState = HorizonProgressState.ERROR }
        } catch (t: Throwable) {
            finalState = HorizonProgressState.ERROR
            throw t
        } finally {
            runCatching { updateLearningLibraryState(finalState) }
        }
    }

    private suspend fun updateLearningLibraryState(state: HorizonProgressState) {
        val existing = globalSyncPlanDao.getPlanOnce()
        val updated = existing?.copy(learningLibraryState = state)
            ?: HorizonGlobalSyncPlanEntity(learningLibraryState = state)
        globalSyncPlanDao.upsert(updated)
    }

    private suspend fun syncCollections(): List<com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection> {
        val collections = networkDataSource.getEnrolledLearningLibraries(
            limit = COLLECTION_ITEM_LIMIT,
            forceRefresh = true,
        )
        val expanded = collections.map { collection ->
            if (collection.totalItemCount > collection.items.size) {
                runCatching {
                    networkDataSource.getEnrolledLearningLibraryCollection(collection.id, forceRefresh = true)
                }.getOrElse { collection }
            } else collection
        }
        localDataSource.saveEnrolledLearningLibraries(expanded)
        return expanded
    }

    private suspend fun syncBrowseItems(): List<LearningLibraryCollectionItem> {
        val all = mutableListOf<LearningLibraryCollectionItem>()
        var cursor: String? = null
        do {
            val page = networkDataSource.getLearningLibraryItems(
                cursor = cursor,
                limit = PAGE_SIZE,
                searchQuery = null,
                typeFilter = null,
                bookmarkedOnly = false,
                completedOnly = false,
                sortBy = null,
                forceRefresh = true,
            )
            all.addAll(page.items)
            cursor = if (page.pageInfo.hasNextPage) page.pageInfo.nextCursor else null
        } while (cursor != null)
        localDataSource.saveBrowseItems(all)
        return all
    }

    private suspend fun syncBookmarkedItems() {
        val all = mutableListOf<LearningLibraryCollectionItem>()
        var cursor: String? = null
        do {
            val page = networkDataSource.getLearningLibraryItems(
                cursor = cursor,
                limit = PAGE_SIZE,
                searchQuery = null,
                typeFilter = null,
                bookmarkedOnly = true,
                completedOnly = false,
                sortBy = null,
                forceRefresh = true,
            )
            all.addAll(page.items)
            cursor = if (page.pageInfo.hasNextPage) page.pageInfo.nextCursor else null
        } while (cursor != null)
        localDataSource.saveSavedItems(all)
    }

    private suspend fun syncRecommendations(): List<LearningLibraryRecommendation> {
        val recommendations = networkDataSource.getLearningLibraryRecommendations(forceRefresh = true)
        localDataSource.saveRecommendations(recommendations)
        return recommendations
    }

    private suspend fun syncReferencedCourseContent(items: List<LearningLibraryCollectionItem>) {
        val alreadyPlannedCourseIds = courseSyncPlanDao.findAll().map { it.courseId }.toSet()

        val itemsByCourse = items
            .filter { it.itemType != CollectionItemType.PROGRAM }
            .filter { !it.canEnroll() }
            .mapNotNull { item -> item.canvasCourse?.courseId?.toLongOrNull()?.let { it to item } }
            .groupBy({ it.first }, { it.second })

        for ((courseId, courseItems) in itemsByCourse) {
            if (courseId in alreadyPlannedCourseIds) continue
            runCatching { syncCourse(courseId, courseItems) }
        }
    }

    private suspend fun syncCourse(courseId: Long, items: List<LearningLibraryCollectionItem>) {
        val courseResult = runCatching { courseContentSyncer.sync(courseId) }.getOrNull()
            ?: CourseSyncResult()

        val assignmentIds = items
            .filter { it.itemType == CollectionItemType.ASSIGNMENT }
            .mapNotNull { it.moduleInfo?.resourceId?.toLongOrNull() }
            .distinct()
        val pageUrls = items
            .filter { it.itemType == CollectionItemType.PAGE }
            .mapNotNull { it.moduleInfo?.resourceId }
            .distinct()
        val fileIds = items
            .filter { it.itemType == CollectionItemType.FILE }
            .mapNotNull { it.moduleInfo?.resourceId?.toLongOrNull() }
            .distinct()

        val additionalFileIds = courseResult.additionalFileIds.toMutableSet()
        val externalUrls = courseResult.externalFileUrls.toMutableSet()

        if (assignmentIds.isNotEmpty()) {
            runCatching { assignmentSyncer.syncAssignments(courseId, assignmentIds) }.getOrNull()?.let { r ->
                additionalFileIds += r.additionalFileIds
                externalUrls += r.externalFileUrls
            }
        }
        if (pageUrls.isNotEmpty()) {
            runCatching { pageSyncer.syncPages(courseId, pageUrls) }.getOrNull()?.let { r ->
                additionalFileIds += r.additionalFileIds
                externalUrls += r.externalFileUrls
            }
        }

        if (fileIds.isNotEmpty() || additionalFileIds.isNotEmpty() || externalUrls.isNotEmpty()) {
            runCatching {
                fileSyncer.syncFiles(
                    courseId = courseId,
                    selectedFileIds = fileIds,
                    additionalFileIds = additionalFileIds,
                    externalUrls = externalUrls,
                    isStopped = { false },
                )
            }
        }
    }

    private suspend fun syncImages(
        browseItems: List<LearningLibraryCollectionItem>,
        collectionItems: List<LearningLibraryCollectionItem>,
        recommendations: List<LearningLibraryRecommendation>,
    ) {
        val urls = mutableSetOf<String>()
        browseItems.forEach { it.canvasCourse?.courseImageUrl?.let(urls::add) }
        collectionItems.forEach { it.canvasCourse?.courseImageUrl?.let(urls::add) }
        recommendations.forEach { it.item.canvasCourse?.courseImageUrl?.let(urls::add) }
        if (urls.isNotEmpty()) imageSyncer.syncImages(urls)
    }

    private fun LearningLibraryCollectionItem.canEnroll(): Boolean {
        return (itemType == CollectionItemType.COURSE || itemType == CollectionItemType.PROGRAM)
            && isEnrolledInCanvas != true
    }

    companion object {
        private const val COLLECTION_ITEM_LIMIT = 100
        private const val PAGE_SIZE = 100
    }
}
