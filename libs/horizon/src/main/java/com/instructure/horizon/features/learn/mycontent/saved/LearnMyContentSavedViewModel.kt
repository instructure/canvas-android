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
package com.instructure.horizon.features.learn.mycontent.saved

import android.content.res.Resources
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryItemsParams
import com.instructure.horizon.domain.usecase.OfflineCardStateHelper
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryItemsUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryRecommendationsParams
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryRecommendationsUseCase
import com.instructure.horizon.domain.usecase.GetNextModuleItemUseCase
import com.instructure.horizon.domain.usecase.ToggleLearnLearningLibraryItemBookmarkParams
import com.instructure.horizon.domain.usecase.ToggleLearnLearningLibraryItemBookmarkUseCase
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentViewModel
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnMyContentSavedViewModel @Inject constructor(
    private val resources: Resources,
    private val getLearnLearningLibraryItemsUseCase: GetLearnLearningLibraryItemsUseCase,
    private val getLearnLearningLibraryRecommendationsUseCase: GetLearnLearningLibraryRecommendationsUseCase,
    private val toggleLearnLearningLibraryItemBookmarkUseCase: ToggleLearnLearningLibraryItemBookmarkUseCase,
    private val offlineCardStateHelper: OfflineCardStateHelper,
    getNextModuleItemUseCase: GetNextModuleItemUseCase,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    getLastSyncedAtUseCase: GetLastSyncedAtUseCase
) : LearnMyContentViewModel<LearnLearningLibraryCollectionItemState>(getNextModuleItemUseCase, networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase) {

    override val errorMessage: String
        get() = resources.getString(R.string.learnMyContentProgramErrorMessage)

    override suspend fun fetchPage(
        cursor: String?,
        searchQuery: String,
        sortBy: CollectionItemSortOption,
        typeFilter: LearnLearningLibraryTypeFilter,
        forceRefresh: Boolean,
    ): Pair<List<LearnLearningLibraryCollectionItemState>, LearningLibraryPageInfo> {
        val recommendations = getLearnLearningLibraryRecommendationsUseCase(GetLearnLearningLibraryRecommendationsParams(forceRefresh))
        val response = getLearnLearningLibraryItemsUseCase(
            GetLearnLearningLibraryItemsParams(
                cursor = cursor,
                limit = 10,
                searchQuery = searchQuery.ifEmpty { null },
                typeFilter = typeFilter.toCollectionItemType(),
                bookmarkedOnly = true,
                completedOnly = false,
                sortBy = sortBy,
                forceRefresh = forceRefresh,
            )
        )
        val offlineContext = offlineCardStateHelper.buildContext(
            response.items.mapNotNull { it.canvasCourse?.courseImageUrl }
        )
        return response.items.map { item ->
            val courseId = item.canvasCourse?.courseId?.toLongOrNull()
            item.toUiState(resources, recommendations, offlineContext.isSynced(courseId), offlineContext.resolvedImageUrls)
        } to response.pageInfo
    }

    fun onBookmarkItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    contentCards = it.contentCards.map { itemState ->
                        if (itemState.id == itemId) itemState.copy(bookmarkLoading = true) else itemState
                    }
                )
            }

            toggleLearnLearningLibraryItemBookmarkUseCase(ToggleLearnLearningLibraryItemBookmarkParams(itemId))
            _uiState.update {
                it.copy(
                    contentCards = it.contentCards.mapNotNull { itemState ->
                        if (itemState.id == itemId) null else itemState
                    }
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    contentCards = it.contentCards.map { itemState ->
                        if (itemState.id == itemId) itemState.copy(bookmarkLoading = false) else itemState
                    },
                    loadingState = it.loadingState.copy(snackbarMessage = resources.getString(R.string.learnMyContentSavedFailedToBookmarkErrorMessage))
                )
            }
        }
    }
}
