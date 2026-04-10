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
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import com.instructure.horizon.data.repository.LearnMyContentRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

private const val QUERY_KEY = "IN_PROGRESS"

data class GetLearnMyContentInProgressItemsParams(
    val cursor: String?,
    val searchQuery: String?,
    val sortBy: CollectionItemSortOption?,
    val itemTypes: List<LearnItemType>?,
    val forceRefresh: Boolean = false,
)

class GetLearnMyContentInProgressItemsUseCase @Inject constructor(
    private val repository: LearnMyContentRepository,
) : BaseUseCase<GetLearnMyContentInProgressItemsParams, LearnItemsResponse>() {

    override suspend fun execute(params: GetLearnMyContentInProgressItemsParams): LearnItemsResponse {
        return repository.getLearnItems(
            cursor = params.cursor,
            searchQuery = params.searchQuery,
            sortBy = params.sortBy,
            status = listOf(LearnItemStatus.IN_PROGRESS, LearnItemStatus.NOT_STARTED),
            itemTypes = params.itemTypes,
            queryKey = QUERY_KEY,
            forceRefresh = params.forceRefresh,
        )
    }
}