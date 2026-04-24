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

import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
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
                    if (shouldSync() && cursor == null && bookmarkedOnly) {
                        localDataSource.saveSavedItems(response.items)
                    }
                }
        } else {
            localDataSource.getSavedItems()
        }
    }

}
