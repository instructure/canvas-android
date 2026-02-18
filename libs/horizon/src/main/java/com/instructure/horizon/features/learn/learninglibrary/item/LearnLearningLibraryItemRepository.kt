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
package com.instructure.horizon.features.learn.learninglibrary.item

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import javax.inject.Inject

class LearnLearningLibraryItemRepository @Inject constructor(
    private val getLearningLibraryManager: GetLearningLibraryManager
) {
    suspend fun getLearningLibraryItems(
        afterCursor: String? = null,
        limit: Int? = 10,
        searchQuery: String? = null,
        typeFilter: CollectionItemType? = null,
        bookmarkedOnly: Boolean = false,
        completedOnly: Boolean = false,
        forceNetwork: Boolean
    ): LearningLibraryCollectionItemsResponse {
        return getLearningLibraryManager.getLearningLibraryCollectionItems(
            cursor = afterCursor,
            limit = limit,
            bookmarkedOnly = bookmarkedOnly,
            completedOnly = completedOnly,
            searchTerm = searchQuery,
            types = typeFilter?.let { listOf(it) },
            forceNetwork = forceNetwork
        )
    }

    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        return getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked(itemId)
    }

    suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        return getLearningLibraryManager.enrollLearningLibraryItem(itemId)
    }
}