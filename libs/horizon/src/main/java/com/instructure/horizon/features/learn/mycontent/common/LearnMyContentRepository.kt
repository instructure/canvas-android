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
package com.instructure.horizon.features.learn.mycontent.common

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.MyContentManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import javax.inject.Inject

class LearnMyContentRepository @Inject constructor(
    private val myContentManager: MyContentManager,
    private val getLearningLibraryManager: GetLearningLibraryManager,
) {
    suspend fun getLearnItems(
        cursor: String? = null,
        searchQuery: String? = null,
        sortBy: CollectionItemSortOption? = null,
        status: List<LearnItemStatus>? = null,
        itemTypes: List<LearnItemType>? = null,
        forceNetwork: Boolean = false,
    ): LearnItemsResponse {
        return myContentManager.getLearnItems(
            cursor = cursor,
            searchTerm = searchQuery,
            sortBy = sortBy,
            status = status,
            itemTypes = itemTypes,
            forceNetwork = forceNetwork,
        )
    }

    suspend fun getBookmarkedLearningLibraryItems(
        afterCursor: String? = null,
        limit: Int? = 10,
        searchQuery: String? = null,
        sortBy: CollectionItemSortOption? = null,
        types: List<CollectionItemType>? = null,
        forceNetwork: Boolean = false,
    ): LearningLibraryCollectionItemsResponse {
        return getLearningLibraryManager.getLearningLibraryCollectionItems(
            cursor = afterCursor,
            limit = limit,
            bookmarkedOnly = true,
            completedOnly = false,
            searchTerm = searchQuery,
            types = types,
            sortBy = sortBy,
            forceNetwork = forceNetwork,
        )
    }

    suspend fun getLearningLibraryRecommendedItems(forceNetwork: Boolean): List<LearningLibraryRecommendation> {
        return getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork)
    }
}
