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
package com.instructure.horizon.features.learn.learninglibrary.details

import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryCollectionParams
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryCollectionUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryRecommendationsParams
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryRecommendationsUseCase
import com.instructure.horizon.domain.usecase.OfflineCardStateHelper
import com.instructure.horizon.domain.usecase.ToggleLearnLearningLibraryItemBookmarkParams
import com.instructure.horizon.domain.usecase.ToggleLearnLearningLibraryItemBookmarkUseCase
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryStatusFilter
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.offline.HorizonOfflineViewModel
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val getLearnLearningLibraryCollectionUseCase: GetLearnLearningLibraryCollectionUseCase,
    private val getLearnLearningLibraryRecommendationsUseCase: GetLearnLearningLibraryRecommendationsUseCase,
    private val toggleLearnLearningLibraryItemBookmarkUseCase: ToggleLearnLearningLibraryItemBookmarkUseCase,
    private val offlineCardStateHelper: OfflineCardStateHelper,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    getLastSyncedAtUseCase: GetLastSyncedAtUseCase,
) : HorizonOfflineViewModel(networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase) {
    private val collectionId = savedStateHandle.get<String>(LearnRoute.LearnLearningLibraryDetailsScreen.collectionIdAttr) ?: ""

    private var allItems: List<LearningLibraryCollectionItem> = emptyList()
    private val pageSize = 10

    private var recommendedItems: List<LearningLibraryRecommendation> = emptyList()

    private val _uiState = MutableStateFlow(LearnLearningLibraryDetailsUiState(
        loadingState = LoadingState(
            onRefresh = ::refreshData,
            onSnackbarDismiss = ::onDismissSnackbar
        ),
        itemsToDisplay = pageSize,
        increaseItemsToDisplay = ::increaseItemsToDisplay,
        updateSearchQuery = ::updateSearchQuery,
        updateSelectedStatusFilter = ::updateSelectedStatusFilter,
        updateTypeFilter = ::updateSelectedTypeFilter,
        onBookmarkClicked = ::toggleItemBookmarked,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    override fun onNetworkRestored() {
        refreshData()
    }

    override fun onNetworkLost() {
        // Offline banner handled at the screen level
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            val collection = fetchData()
            fetchRecommendedItems()
            applyItems(collection)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = false)) }
        } catch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true))
            }
        }
    }

    private fun refreshData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            val collection = fetchData(true)
            fetchRecommendedItems(true)
            applyItems(collection)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, isError = false)) }
        } catch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(
                        isRefreshing = false,
                        snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToLoadCollectionMessage)
                    )
                )
            }
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean = false): EnrolledLearningLibraryCollection {
        val result = getLearnLearningLibraryCollectionUseCase(
            GetLearnLearningLibraryCollectionParams(collectionId = collectionId, forceRefresh = forceRefresh)
        )
        allItems = result.items
        _uiState.update { it.copy(collectionName = result.name) }
        return result
    }

    private suspend fun fetchRecommendedItems(forceNetwork: Boolean = false) {
        recommendedItems = getLearnLearningLibraryRecommendationsUseCase(
            GetLearnLearningLibraryRecommendationsParams(forceRefresh = forceNetwork)
        )
    }

    private suspend fun applyItems(collection: EnrolledLearningLibraryCollection) {
        val filtered = collection.items.applyFilters()
        val offlineContext = offlineCardStateHelper.buildContext(
            filtered.map { it.canvasCourse?.courseImageUrl }
        )
        _uiState.update {
            it.copy(
                items = filtered.map { it.toUiState(resources, recommendedItems, offlineContext.isOffline, offlineContext.resolvedImageUrls) },
            )
        }
    }

    private fun toggleItemBookmarked(itemId: String) {
        if (isOffline()) {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage)))
            }
            return
        }
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    items = it.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(bookmarkLoading = true)
                        } else {
                            collectionItemState
                        }
                    }
                )
            }

            val newIsBookmarked = toggleLearnLearningLibraryItemBookmarkUseCase(
                ToggleLearnLearningLibraryItemBookmarkParams(itemId = itemId)
            )

            allItems = allItems.map { collectionItemState ->
                if (collectionItemState.id == itemId) {
                    collectionItemState.copy(isBookmarked = newIsBookmarked)
                } else {
                    collectionItemState
                }
            }

            val filtered = allItems.applyFilters()
            val offlineContext = offlineCardStateHelper.buildContext(
                filtered.map { it.canvasCourse?.courseImageUrl }
            )
            _uiState.update {
                it.copy(items = filtered.map { it.toUiState(resources, recommendedItems, offlineContext.isOffline, offlineContext.resolvedImageUrls) })
            }
        } catch {
            _uiState.update {
                it.copy(
                    items = it.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(
                                bookmarkLoading = false,
                            )
                        } else {
                            collectionItemState
                        }
                    },
                    loadingState = it.loadingState.copy(snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage))
                )
            }
        }
    }

    private fun List<LearningLibraryCollectionItem>.applyFilters(): List<LearningLibraryCollectionItem> {
        return this.filter {
            it.canvasCourse?.courseName.orEmpty().contains(_uiState.value.searchQuery.text.trim(), ignoreCase = true)
                    && it.isSatisfyStatusFilter(_uiState.value.selectedStatusFilter)
                    && it.isSatisfyTypeFilter(_uiState.value.selectedTypeFilter)
        }
    }

    private fun LearningLibraryCollectionItem.isSatisfyStatusFilter(statusFilter: LearnLearningLibraryStatusFilter): Boolean {
        return when (statusFilter) {
            LearnLearningLibraryStatusFilter.All -> true
            LearnLearningLibraryStatusFilter.Completed -> this.completionPercentage == 100.0
            LearnLearningLibraryStatusFilter.Bookmarked -> this.isBookmarked
        }
    }

    private fun LearningLibraryCollectionItem.isSatisfyTypeFilter(typeFilter: LearnLearningLibraryTypeFilter): Boolean {
        return when (typeFilter) {
            LearnLearningLibraryTypeFilter.All -> true
            LearnLearningLibraryTypeFilter.Courses -> this.itemType == CollectionItemType.COURSE
            LearnLearningLibraryTypeFilter.Programs -> this.itemType == CollectionItemType.PROGRAM
            LearnLearningLibraryTypeFilter.Assessments -> this.itemType == CollectionItemType.QUIZ
            LearnLearningLibraryTypeFilter.Assignments -> this.itemType == CollectionItemType.ASSIGNMENT
            LearnLearningLibraryTypeFilter.ExternalLinks -> this.itemType == CollectionItemType.EXTERNAL_URL
            LearnLearningLibraryTypeFilter.ExternalTools -> this.itemType == CollectionItemType.EXTERNAL_TOOL
            LearnLearningLibraryTypeFilter.Files -> this.itemType == CollectionItemType.FILE
            LearnLearningLibraryTypeFilter.Pages -> this.itemType == CollectionItemType.PAGE
        }
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }

    private fun increaseItemsToDisplay() {
        _uiState.update { it.copy(itemsToDisplay = it.itemsToDisplay + pageSize) }
    }

    private fun updateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
        viewModelScope.launch {
            val filtered = allItems.applyFilters()
            val offlineContext = offlineCardStateHelper.buildContext(filtered.map { it.canvasCourse?.courseImageUrl })
            _uiState.update { it.copy(items = filtered.map { it.toUiState(resources, recommendedItems, offlineContext.isOffline, offlineContext.resolvedImageUrls) }) }
        }
    }

    private fun updateSelectedStatusFilter(value: LearnLearningLibraryStatusFilter) {
        _uiState.update { it.copy(selectedStatusFilter = value) }
        viewModelScope.launch {
            val filtered = allItems.applyFilters()
            val offlineContext = offlineCardStateHelper.buildContext(filtered.map { it.canvasCourse?.courseImageUrl })
            _uiState.update { it.copy(items = filtered.map { it.toUiState(resources, recommendedItems, offlineContext.isOffline, offlineContext.resolvedImageUrls) }) }
        }
    }

    private fun updateSelectedTypeFilter(value: LearnLearningLibraryTypeFilter) {
        _uiState.update { it.copy(selectedTypeFilter = value) }
        viewModelScope.launch {
            val filtered = allItems.applyFilters()
            val offlineContext = offlineCardStateHelper.buildContext(filtered.map { it.canvasCourse?.courseImageUrl })
            _uiState.update { it.copy(items = filtered.map { it.toUiState(resources, recommendedItems, offlineContext.isOffline, offlineContext.resolvedImageUrls) }) }
        }
    }
}
