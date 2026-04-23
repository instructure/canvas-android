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
package com.instructure.horizon.features.learn.mycontent.completed

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnMyContentCompletedItemsParams
import com.instructure.horizon.domain.usecase.GetLearnMyContentCompletedItemsUseCase
import com.instructure.horizon.domain.usecase.GetNextModuleItemUseCase
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardState
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentViewModel
import com.instructure.horizon.features.learn.mycontent.common.toCardState
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LearnMyContentCompletedViewModel @Inject constructor(
    private val resources: Resources,
    private val getLearnMyContentCompletedItemsUseCase: GetLearnMyContentCompletedItemsUseCase,
    getNextModuleItemUseCase: GetNextModuleItemUseCase,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    getLastSyncedAtUseCase: GetLastSyncedAtUseCase
) : LearnMyContentViewModel<LearnContentCardState>(getNextModuleItemUseCase, networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase) {

    override val errorMessage: String
        get() = resources.getString(R.string.learnMyContentProgramErrorMessage)

    override suspend fun fetchPage(
        cursor: String?,
        searchQuery: String,
        sortBy: CollectionItemSortOption,
        typeFilter: LearnLearningLibraryTypeFilter,
        forceRefresh: Boolean,
    ): Pair<List<LearnContentCardState>, LearningLibraryPageInfo> {
        val response = getLearnMyContentCompletedItemsUseCase(
            GetLearnMyContentCompletedItemsParams(
                cursor = cursor,
                searchQuery = searchQuery.ifEmpty { null },
                sortBy = sortBy,
                itemTypes = typeFilter.toLearnItemType()?.let { listOf(it) },
                forceRefresh = forceRefresh,
            )
        )
        return response.items.map {
            it.toCardState(resources) { courseId -> fetchNextModuleItemRoute(courseId) }
        } to response.pageInfo
    }
}