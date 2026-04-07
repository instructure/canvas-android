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

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import javax.inject.Inject

class LearnLearningLibraryNetworkDataSource @Inject constructor(
    private val getLearningLibraryManager: GetLearningLibraryManager,
) {

    suspend fun getEnrolledLearningLibraries(limit: Int, forceRefresh: Boolean): List<EnrolledLearningLibraryCollection> {
        return getLearningLibraryManager.getEnrolledLearningLibraryCollections(limit, forceNetwork = forceRefresh).collections
    }

    suspend fun getLearningLibraryItems(
        cursor: String?,
        limit: Int?,
        searchQuery: String?,
        typeFilter: CollectionItemType?,
        bookmarkedOnly: Boolean,
        completedOnly: Boolean,
        sortBy: CollectionItemSortOption?,
        forceRefresh: Boolean,
    ): LearningLibraryCollectionItemsResponse {
        return getLearningLibraryManager.getLearningLibraryCollectionItems(
            cursor = cursor,
            limit = limit,
            bookmarkedOnly = bookmarkedOnly,
            completedOnly = completedOnly,
            searchTerm = searchQuery,
            types = typeFilter?.let { listOf(it) },
            sortBy = sortBy,
            forceNetwork = forceRefresh,
        )
    }
}
