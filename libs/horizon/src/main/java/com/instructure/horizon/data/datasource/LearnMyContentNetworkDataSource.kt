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

import com.instructure.canvasapi2.managers.graphql.horizon.journey.MyContentManager
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import javax.inject.Inject

class LearnMyContentNetworkDataSource @Inject constructor(
    private val myContentManager: MyContentManager,
) {

    suspend fun getLearnItems(
        cursor: String?,
        searchQuery: String?,
        sortBy: CollectionItemSortOption?,
        status: List<LearnItemStatus>?,
        itemTypes: List<LearnItemType>?,
        forceRefresh: Boolean,
    ): LearnItemsResponse {
        return myContentManager.getLearnItems(
            cursor = cursor,
            searchTerm = searchQuery,
            sortBy = sortBy,
            status = status,
            itemTypes = itemTypes,
            forceNetwork = forceRefresh,
        )
    }
}
