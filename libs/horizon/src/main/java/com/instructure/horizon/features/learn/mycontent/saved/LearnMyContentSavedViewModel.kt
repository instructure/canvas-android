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
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentRepository
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnMyContentSavedViewModel @Inject constructor(
    private val resources: Resources,
    myContentRepository: LearnMyContentRepository,
    private val savedContentRepository: LearnMyContentSavedRepository,
) : LearnMyContentViewModel<LearnLearningLibraryCollectionItemState>(myContentRepository) {

    override val errorMessage: String
        get() = resources.getString(R.string.learnMyContentProgramErrorMessage)

    override suspend fun fetchPage(
        cursor: String?,
        searchQuery: String,
        sortBy: CollectionItemSortOption,
        typeFilter: LearnLearningLibraryTypeFilter,
        forceNetwork: Boolean,
    ): Pair<List<LearnLearningLibraryCollectionItemState>, LearningLibraryPageInfo> {
        val recommendations = savedContentRepository.getLearningLibraryRecommendedItems(forceNetwork)
        val response = repository.getBookmarkedLearningLibraryItems(
            afterCursor = cursor,
            searchQuery = searchQuery.ifEmpty { null },
            sortBy = sortBy,
            types = typeFilter.toCollectionItemType()?.let { listOf(it) },
            forceNetwork = forceNetwork,
        )
        return response.items.map { it.toUiState(resources, recommendations) } to response.pageInfo
    }

    fun onBookmarkItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update { 
                it.copy(
                    contentCards = it.contentCards.map { itemState ->
                        if (itemState.id == itemId) {
                            itemState.copy(bookmarkLoading = true)
                        } else {
                            itemState
                        }
                    }
                )
            }

            savedContentRepository.toggleLearningLibraryItemIsBookmarked(itemId)
            _uiState.update {
                it.copy(
                    contentCards = it.contentCards.mapNotNull { itemState ->
                        if (itemState.id == itemId) {
                            null
                        } else {
                            itemState
                        }
                    }
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    contentCards = it.contentCards.map { itemState ->
                        if (itemState.id == itemId) {
                            itemState.copy(bookmarkLoading = false,)
                        } else {
                            itemState
                        }
                    },
                    loadingState = it.loadingState.copy(snackbarMessage = resources.getString(R.string.learnMyContentSavedFailedToBookmarkErrorMessage))
                )
            }
        }
    }
}
