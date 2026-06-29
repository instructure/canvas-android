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
package com.instructure.horizon.domain.usecase

import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.horizon.data.repository.LearnLearningLibraryRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class GetLearnLearningLibraryItemsParams(
    val cursor: String?,
    val limit: Int?,
    val searchQuery: String?,
    val typeFilter: CollectionItemType?,
    val bookmarkedOnly: Boolean,
    val completedOnly: Boolean,
    val sortBy: CollectionItemSortOption?,
    val forceRefresh: Boolean = false,
)

class GetLearnLearningLibraryItemsUseCase @Inject constructor(
    private val repository: LearnLearningLibraryRepository,
) : BaseUseCase<GetLearnLearningLibraryItemsParams, LearningLibraryCollectionItemsResponse>() {

    override suspend fun execute(params: GetLearnLearningLibraryItemsParams): LearningLibraryCollectionItemsResponse {
        return repository.getLearningLibraryItems(
            cursor = params.cursor,
            limit = params.limit,
            searchQuery = params.searchQuery,
            typeFilter = params.typeFilter,
            bookmarkedOnly = params.bookmarkedOnly,
            completedOnly = params.completedOnly,
            sortBy = params.sortBy,
            forceRefresh = params.forceRefresh,
        )
    }
}
