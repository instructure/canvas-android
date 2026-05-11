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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import javax.inject.Inject

class LearnLearningLibraryNetworkDataSource @Inject constructor(
    private val getLearningLibraryManager: GetLearningLibraryManager,
    private val getCoursesManager: HorizonGetCoursesManager,
) {

    suspend fun getEnrolledLearningLibraries(limit: Int, forceRefresh: Boolean): List<EnrolledLearningLibraryCollection> {
        return getLearningLibraryManager.getEnrolledLearningLibraryCollections(limit, forceNetwork = forceRefresh).collections
    }

    suspend fun getEnrolledLearningLibraryCollection(id: String, forceRefresh: Boolean): EnrolledLearningLibraryCollection {
        return getLearningLibraryManager.getEnrolledLearningLibraryCollection(id, forceNetwork = forceRefresh)
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

    suspend fun getLearningLibraryItem(id: String, forceRefresh: Boolean): LearningLibraryCollectionItem {
        return getLearningLibraryManager.getLearningLibraryItem(id, forceNetwork = forceRefresh)
    }

    suspend fun getLearningLibraryRecommendations(forceRefresh: Boolean): List<LearningLibraryRecommendation> {
        return getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork = forceRefresh)
    }

    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        return getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked(itemId)
    }

    suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        return getLearningLibraryManager.enrollLearningLibraryItem(itemId)
    }

    suspend fun getCourseWithProgress(courseId: Long, userId: Long): CourseWithProgress {
        return getCoursesManager.getCourseWithProgressById(courseId, userId).dataOrThrow
    }
}
