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
package com.instructure.horizon.features.learn.mycontent.inprogress

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardState
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentRepository
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentViewModel
import com.instructure.horizon.features.learn.mycontent.common.toCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LearnMyContentInProgressViewModel @Inject constructor(
    private val resources: Resources,
    repository: LearnMyContentRepository,
) : LearnMyContentViewModel<LearnContentCardState>(repository) {

    override val errorMessage: String
        get() = resources.getString(R.string.learnMyContentProgramErrorMessage)

    init {
        load()
    }

    override suspend fun fetchPage(
        cursor: String?,
        searchQuery: String,
        sortBy: CollectionItemSortOption,
        forceNetwork: Boolean,
    ): Pair<List<LearnContentCardState>, LearningLibraryPageInfo> {
        val response = repository.getLearnItems(
            cursor = cursor,
            searchQuery = searchQuery.ifEmpty { null },
            sortBy = sortBy,
            status = listOf(LearnItemStatus.IN_PROGRESS, LearnItemStatus.NOT_STARTED),
            forceNetwork = forceNetwork,
        )
        return response.items.map { it.toCardState(resources) } to response.pageInfo
    }
}
