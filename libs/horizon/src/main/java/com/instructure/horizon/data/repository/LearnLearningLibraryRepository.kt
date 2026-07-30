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
import com.instructure.horizon.data.datasource.LearnLearningLibraryNetworkDataSource
import javax.inject.Inject

class LearnLearningLibraryRepository @Inject constructor(
    private val networkDataSource: LearnLearningLibraryNetworkDataSource,
) {

    suspend fun getEnrolledLearningLibraries(limit: Int, forceRefresh: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return networkDataSource.getEnrolledLearningLibraries(limit, forceRefresh)
    }

    suspend fun getEnrolledLearningLibraryCollection(id: String, forceRefresh: Boolean = false): EnrolledLearningLibraryCollection {
        return networkDataSource.getEnrolledLearningLibraryCollection(id, forceRefresh)
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
        return networkDataSource.getLearningLibraryItems(
            cursor, limit, searchQuery, typeFilter, bookmarkedOnly, completedOnly, sortBy, forceRefresh,
        )
    }

    suspend fun getLearningLibraryItem(id: String, forceRefresh: Boolean = false): LearningLibraryCollectionItem {
        return networkDataSource.getLearningLibraryItem(id, forceRefresh)
    }

    suspend fun getLearningLibraryRecommendations(forceRefresh: Boolean = false): List<LearningLibraryRecommendation> {
        return networkDataSource.getLearningLibraryRecommendations(forceRefresh)
    }

    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        return networkDataSource.toggleLearningLibraryItemIsBookmarked(itemId)
    }

    suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        return networkDataSource.enrollLearningLibraryItem(itemId)
    }

    suspend fun getCourseWithProgress(courseId: Long, userId: Long): CourseWithProgress {
        return networkDataSource.getCourseWithProgress(courseId, userId)
    }
}
