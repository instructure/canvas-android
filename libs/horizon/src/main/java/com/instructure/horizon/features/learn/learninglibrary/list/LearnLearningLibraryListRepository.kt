/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.learn.learninglibrary.list

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import javax.inject.Inject

class LearnLearningLibraryListRepository @Inject constructor(
    private val getLearningLibraryManager: GetLearningLibraryManager,
) {
    private val itemLimitPerCollection = 4

    suspend fun getLearningLibraryItems(
        afterCursor: String? = null,
        limit: Int? = 10,
        searchQuery: String? = null,
        typeFilter: CollectionItemType? = null,
        bookmarkedOnly: Boolean = false,
        completedOnly: Boolean = false,
        sortBy: CollectionItemSortOption? = null,
        forceNetwork: Boolean
    ): LearningLibraryCollectionItemsResponse {
        return getLearningLibraryManager.getLearningLibraryCollectionItems(
            cursor = afterCursor,
            limit = limit,
            bookmarkedOnly = bookmarkedOnly,
            completedOnly = completedOnly,
            searchTerm = searchQuery,
            types = typeFilter?.let { listOf(it) },
            sortBy = sortBy,
            forceNetwork = forceNetwork
        )
    }

    suspend fun getEnrolledLearningLibraries(forceNetwork: Boolean): List<EnrolledLearningLibraryCollection> {
        return getLearningLibraryManager.getEnrolledLearningLibraryCollections(itemLimitPerCollection, forceNetwork).collections
    }

    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        return getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked(itemId)
    }

    suspend fun getLearningLibraryRecommendedItems(forceNetwork: Boolean): List<LearningLibraryRecommendation> {
        return getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork)
    }
}