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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.horizon.data.datasource.LearnLearningLibraryLocalDataSource
import com.instructure.horizon.data.datasource.LearnLearningLibraryNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class LearnLearningLibraryRepository @Inject constructor(
    private val networkDataSource: LearnLearningLibraryNetworkDataSource,
    private val localDataSource: LearnLearningLibraryLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getEnrolledLearningLibraries(limit: Int, forceRefresh: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getEnrolledLearningLibraries(limit, forceRefresh)
                .also { if (shouldSync()) localDataSource.saveEnrolledLearningLibraries(it) }
        } else {
            localDataSource.getEnrolledLearningLibraries()
        }
    }

    suspend fun getEnrolledLearningLibraryCollection(id: String, forceRefresh: Boolean = false): EnrolledLearningLibraryCollection {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getEnrolledLearningLibraryCollection(id, forceRefresh)
                .also { if (shouldSync()) localDataSource.saveCollection(it) }
        } else {
            localDataSource.getCollection(id)
                ?: throw IllegalStateException("Learning library collection $id is not available offline")
        }
    }

    suspend fun getLearningLibraryItems(
        cursor: String?,
        limit: Int?,
        searchQuery: String?,
        typeFilter: CollectionItemType?,
        bookmarkedOnly: Boolean,
        completedOnly: Boolean,
        sortBy: CollectionItemSortOption?,
        forceRefresh: Boolean = false,
    ): LearningLibraryCollectionItemsResponse {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getLearningLibraryItems(cursor, limit, searchQuery, typeFilter, bookmarkedOnly, completedOnly, sortBy, forceRefresh)
                .also { response ->
                    if (shouldSync() && cursor == null) {
                        if (bookmarkedOnly) {
                            localDataSource.saveSavedItems(response.items)
                        } else if (searchQuery.isNullOrBlank() && typeFilter == null && !completedOnly) {
                            localDataSource.saveBrowseItems(response.items)
                        }
                    }
                }
        } else {
            if (bookmarkedOnly) {
                localDataSource.getSavedItems()
            } else {
                localDataSource.getBrowseItems(searchQuery, typeFilter, sortBy, bookmarkedOnly = false, completedOnly = completedOnly)
            }
        }
    }

    suspend fun getLearningLibraryItem(id: String, forceRefresh: Boolean = false): LearningLibraryCollectionItem {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getLearningLibraryItem(id, forceRefresh)
        } else {
            localDataSource.findItemById(id)
                ?: throw IllegalStateException("Learning library item $id is not available offline")
        }
    }

    suspend fun getLearningLibraryRecommendations(forceRefresh: Boolean = false): List<LearningLibraryRecommendation> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getLearningLibraryRecommendations(forceRefresh)
                .also { if (shouldSync()) localDataSource.saveRecommendations(it) }
        } else {
            localDataSource.getRecommendations()
        }
    }

    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        if (!isOnline()) {
            throw IllegalStateException("Cannot toggle bookmark while offline")
        }
        val newValue = networkDataSource.toggleLearningLibraryItemIsBookmarked(itemId)
        if (shouldSync()) {
            localDataSource.updateBookmark(itemId, newValue)
        }
        return newValue
    }

    suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        if (!isOnline()) {
            throw IllegalStateException("Cannot enroll while offline")
        }
        return networkDataSource.enrollLearningLibraryItem(itemId)
    }

    suspend fun getCourseWithProgress(courseId: Long, userId: Long): CourseWithProgress {
        return networkDataSource.getCourseWithProgress(courseId, userId)
    }
}
