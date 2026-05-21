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
import com.instructure.canvasapi2.models.journey.mycontent.LearnItem
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import com.instructure.horizon.data.datasource.LearnMyContentLocalDataSource
import com.instructure.horizon.data.datasource.LearnMyContentNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class LearnMyContentRepository @Inject constructor(
    private val networkDataSource: LearnMyContentNetworkDataSource,
    private val localDataSource: LearnMyContentLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getLearnItems(
        cursor: String?,
        searchQuery: String?,
        sortBy: CollectionItemSortOption?,
        status: List<LearnItemStatus>?,
        itemTypes: List<LearnItemType>?,
        queryKey: String,
        forceRefresh: Boolean = false,
    ): LearnItemsResponse {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getLearnItems(cursor, searchQuery, sortBy, status, itemTypes, forceRefresh)
                .also {
                    if (shouldSync() && cursor == null) {
                        depaginateAndSync(status, queryKey)
                    }
                }
        } else {
            localDataSource.getLearnItems(queryKey, searchQuery, sortBy, itemTypes, cursor)
        }
    }

    private suspend fun depaginateAndSync(status: List<LearnItemStatus>?, queryKey: String) {
        val allItems = mutableListOf<LearnItem>()
        var nextCursor: String? = null
        do {
            val page = networkDataSource.getLearnItems(
                cursor = nextCursor,
                searchQuery = null,
                sortBy = null,
                status = status,
                itemTypes = null,
                forceRefresh = true,
            )
            allItems.addAll(page.items)
            nextCursor = if (page.pageInfo.hasNextPage) page.pageInfo.nextCursor else null
        } while (nextCursor != null)
        localDataSource.saveLearnItems(allItems, queryKey)
    }

}
