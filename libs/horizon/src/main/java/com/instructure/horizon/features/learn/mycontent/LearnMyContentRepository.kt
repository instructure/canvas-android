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
package com.instructure.horizon.features.learn.mycontent

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.canvasapi2.utils.ApiPrefs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LearnMyContentRepository @Inject constructor(
    private val getCoursesManager: HorizonGetCoursesManager,
    private val getProgramsManager: GetProgramsManager,
    private val getLearningLibraryManager: GetLearningLibraryManager,
    private val apiPrefs: ApiPrefs
) {
    suspend fun getCoursesWithProgress(forceNetwork: Boolean): List<CourseWithProgress> {
        val courseWithProgress = getCoursesManager.getCoursesWithProgress(apiPrefs.user?.id ?: -1, forceNetwork).dataOrThrow
        return courseWithProgress
    }

    suspend fun getPrograms(forceRefresh: Boolean): List<Program> {
        return getProgramsManager.getPrograms(forceRefresh)
    }

    suspend fun getCoursesById(courseIds: List<Long>, forceNetwork: Boolean = false): List<CourseWithModuleItemDurations> = coroutineScope {
        courseIds.map { id ->
            async { getCoursesManager.getProgramCourses(id, forceNetwork).dataOrThrow }
        }.awaitAll()
    }

    suspend fun getBookmarkedLearningLibraryItems(
        afterCursor: String? = null,
        limit: Int? = 10,
        searchQuery: String? = null,
        sortBy: CollectionItemSortOption? = null,
        forceNetwork: Boolean
    ): LearningLibraryCollectionItemsResponse {
        return getLearningLibraryManager.getLearningLibraryCollectionItems(
            cursor = afterCursor,
            limit = limit,
            bookmarkedOnly = true,
            completedOnly = false,
            searchTerm = searchQuery,
            types = null,
            sortBy = sortBy,
            forceNetwork = forceNetwork
        )
    }

    suspend fun getLearningLibraryRecommendedItems(forceNetwork: Boolean): List<LearningLibraryRecommendation> {
        return getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork)
    }
}