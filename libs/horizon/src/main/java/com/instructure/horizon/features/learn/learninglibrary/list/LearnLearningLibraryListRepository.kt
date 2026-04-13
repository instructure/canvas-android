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
import com.instructure.horizon.data.repository.LearnLearningLibraryRepository
import javax.inject.Inject

class LearnLearningLibraryListRepository @Inject constructor(
    private val learningLibraryRepository: LearnLearningLibraryRepository,
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
        forceRefresh: Boolean = false,
    ): LearningLibraryCollectionItemsResponse {
        return learningLibraryRepository.getLearningLibraryItems(
            cursor = afterCursor,
            limit = limit,
            searchQuery = searchQuery,
            typeFilter = typeFilter,
            bookmarkedOnly = bookmarkedOnly,
            completedOnly = completedOnly,
            sortBy = sortBy,
            forceRefresh = forceRefresh,
        )
    }

    suspend fun getEnrolledLearningLibraries(forceRefresh: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return learningLibraryRepository.getEnrolledLearningLibraries(itemLimitPerCollection, forceRefresh)
    }

    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        return getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked(itemId)
    }

    suspend fun getLearningLibraryRecommendedItems(forceRefresh: Boolean = false): List<LearningLibraryRecommendation> {
        return getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork = forceRefresh)
    }
}
