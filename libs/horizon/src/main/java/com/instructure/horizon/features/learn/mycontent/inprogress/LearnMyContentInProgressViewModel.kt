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
import com.instructure.horizon.R
import com.instructure.canvasapi2.models.journey.mycontent.CourseEnrollmentItem
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnMyContentInProgressItemsParams
import com.instructure.horizon.domain.usecase.GetLearnMyContentInProgressItemsUseCase
import com.instructure.horizon.domain.usecase.GetNextModuleItemUseCase
import com.instructure.horizon.domain.usecase.OfflineCardStateHelper
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardState
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentViewModel
import com.instructure.horizon.features.learn.mycontent.common.toCardState
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LearnMyContentInProgressViewModel @Inject constructor(
    private val resources: Resources,
    private val getLearnMyContentInProgressItemsUseCase: GetLearnMyContentInProgressItemsUseCase,
    private val offlineCardStateHelper: OfflineCardStateHelper,
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
        val response = getLearnMyContentInProgressItemsUseCase(
            GetLearnMyContentInProgressItemsParams(
                cursor = cursor,
                searchQuery = searchQuery.ifEmpty { null },
                sortBy = sortBy,
                itemTypes = typeFilter.toLearnItemType()?.let { listOf(it) },
                forceRefresh = forceRefresh,
            )
        )
        val offlineContext = offlineCardStateHelper.buildContext(
            response.items.filterIsInstance<CourseEnrollmentItem>().mapNotNull { it.imageUrl }
        )
        return response.items.map { item ->
            val courseId = (item as? CourseEnrollmentItem)?.id?.toLongOrNull()
            item.toCardState(resources, { fetchNextModuleItemRoute(it) }, offlineContext.isSynced(courseId), offlineContext.resolvedImageUrls)
        } to response.pageInfo
    }
}