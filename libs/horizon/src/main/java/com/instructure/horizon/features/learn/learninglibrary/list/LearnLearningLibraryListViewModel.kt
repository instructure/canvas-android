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
package com.instructure.horizon.features.learn.learninglibrary.list

import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibrariesParams
import com.instructure.horizon.domain.usecase.GetLearnLearningLibrariesUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryItemsParams
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryItemsUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryRecommendationsParams
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryRecommendationsUseCase
import com.instructure.horizon.domain.usecase.OfflineCardStateHelper
import com.instructure.horizon.domain.usecase.ToggleLearnLearningLibraryItemBookmarkParams
import com.instructure.horizon.domain.usecase.ToggleLearnLearningLibraryItemBookmarkUseCase
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.offline.HorizonOfflineViewModel
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class LearnLearningLibraryListViewModel @Inject constructor(
    private val resources: Resources,
    private val getLearnLearningLibrariesUseCase: GetLearnLearningLibrariesUseCase,
    private val getLearnLearningLibraryItemsUseCase: GetLearnLearningLibraryItemsUseCase,
    private val getLearnLearningLibraryRecommendationsUseCase: GetLearnLearningLibraryRecommendationsUseCase,
    private val toggleLearnLearningLibraryItemBookmarkUseCase: ToggleLearnLearningLibraryItemBookmarkUseCase,
    private val offlineCardStateHelper: OfflineCardStateHelper,
    private val eventHandler: LearnEventHandler,
    private val apiPrefs: ApiPrefs,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    getLastSyncedAtUseCase: GetLastSyncedAtUseCase
) : HorizonOfflineViewModel(networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase) {

    private var currentTypeFilter: LearnLearningLibraryTypeFilter = LearnLearningLibraryTypeFilter.All
    private var currentSortOption: LearnLearningLibrarySortOption = LearnLearningLibrarySortOption.MostRecent

    private var allCollections: List<LearnLearningLibraryCollectionState> = emptyList()
    private val collectionPageSize: Int = 3

    private var itemNextCursor: String? = null
    private val itemPageSize: Int = 10
    private val searchQueryFlow = MutableStateFlow("")

    private val _uiState = MutableStateFlow(LearnLearningLibraryListUiState(
        updateSearchQuery = ::updateSearchQuery,
        collectionState = LearnLearningLibraryListCollectionUiState(
            loadingState = LoadingState(
                onRefresh = ::refreshCollections,
                onSnackbarDismiss = ::onDismissSnackbar
            ),
            userName = apiPrefs.user?.shortName ?: resources.getString(R.string.learnLEarningLibraryListYou),
            collections = allCollections,
            itemsToDisplay = collectionPageSize,
            increaseItemsToDisplay = ::increaseCollectionsToDisplay,
            onBookmarkClicked = ::onCollectionBookmarkItem,
        ),
        itemState = LearnLearningLibraryListItemUiState(
            loadingState = LoadingState(
                onRefresh = ::refreshItems,
                onSnackbarDismiss = ::onDismissSnackbar
            ),
            items = allCollections.flatMap { it.items },
            onShowMoreClicked = ::increaseItemsToDisplay,
            onBookmarkClicked = ::onBookmarkItem,
            isMoreButtonLoading = false
        )
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadCollections()

        viewModelScope.launch {
            searchQueryFlow
                .drop(1)
                .debounce(300)
                .collectLatest { loadItems(cursor = null) }
        }

        viewModelScope.launch {
            eventHandler.events.collectLatest {
                when (it) {
                    LearnEvent.RefreshLearningLibraryList -> {
                        if (uiState.value.isEmptyFilter()) {
                            refreshCollections()
                        } else {
                            refreshItems()
                        }
                    }
                    is LearnEvent.UpdateLearningLibraryFilter -> {
                        if (it.screenType == LearnLearningLibraryFilterScreenType.Browse) {
                            currentTypeFilter = it.typeFilter
                            currentSortOption = it.sortOption
                            _uiState.update { state ->
                                state.copy(
                                    typeFilter = currentTypeFilter,
                                    sortOption = currentSortOption,
                                    activeFilterCount = computeActiveFilterCount()
                                )
                            }
                            loadItems(cursor = null)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onNetworkRestored() {
        if (uiState.value.isEmptyFilter()) refreshCollections() else refreshItems()
    }

    override fun onNetworkLost() {
        // Offline banner is handled at the screen level; no action needed here
    }

    private suspend fun fetchRecommendedItems(forceRefresh: Boolean = false): List<LearningLibraryRecommendation> {
        val recommendations = getLearnLearningLibraryRecommendationsUseCase(
            GetLearnLearningLibraryRecommendationsParams(forceRefresh = forceRefresh)
        )
        val offlineContext = offlineCardStateHelper.buildContext(
            recommendations.map { it.item.canvasCourse?.courseImageUrl }
        )
        _uiState.update {
            it.copy(collectionState = it.collectionState.copy(
                recommendedItems = recommendations.map { it.toUiState(resources, offlineContext.isOffline, offlineContext.resolvedImageUrls) }
            ))
        }
        return recommendations
    }

    private fun loadCollections() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isLoading = true))) }
            val result = fetchCollections(forceRefresh = false)
            val recommendedItems = fetchRecommendedItems(forceRefresh = false)
            val offlineContext = offlineCardStateHelper.buildContext(
                result.flatMap { collection -> collection.items.map { it.canvasCourse?.courseImageUrl } }
            )
            allCollections = result.toUiState(resources, recommendedItems, offlineContext.isOffline, offlineContext.resolvedImageUrls)
            _uiState.update { it.copy(collectionState = it.collectionState.copy(collections = allCollections)) }
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isLoading = false))) }
        } catch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isLoading = false, isError = true))) }
        }
    }

    private fun loadItems(
        cursor: String? = itemNextCursor,
        searchQuery: String? = uiState.value.searchQuery.text,
        typeFilter: CollectionItemType? = currentTypeFilter.toCollectionItemType(),
        sortBy: CollectionItemSortOption? = currentSortOption.toCollectionItemSortOption(),
    ) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isLoading = true))) }
            fetchItems(cursor, searchQuery, typeFilter, sortBy = sortBy, forceRefresh = false)
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isLoading = false))) }
        } catch {
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isLoading = false, isError = true))) }
        }
    }

    private fun refreshCollections() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isRefreshing = true))) }
            val result = fetchCollections(forceRefresh = true)
            val recommendedItems = fetchRecommendedItems(forceRefresh = true)
            val offlineContext = offlineCardStateHelper.buildContext(
                result.flatMap { collection -> collection.items.map { it.canvasCourse?.courseImageUrl } }
            )
            allCollections = result.toUiState(resources, recommendedItems, offlineContext.isOffline, offlineContext.resolvedImageUrls)
            _uiState.update { it.copy(collectionState = it.collectionState.copy(collections = allCollections)) }
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isRefreshing = false, isError = false))) }
        } catch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isRefreshing = false, snackbarMessage = resources.getString(
                R.string.learnLearningLibraryFailedToLoadMessage
            )))) }
        }
    }

    private suspend fun fetchCollections(forceRefresh: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return getLearnLearningLibrariesUseCase(
            GetLearnLearningLibrariesParams(itemLimitPerCollection = itemLimitPerCollection, forceRefresh = forceRefresh)
        )
    }

    private suspend fun fetchItems(
        cursor: String? = itemNextCursor,
        searchQuery: String? = uiState.value.searchQuery.text,
        filterType: CollectionItemType? = currentTypeFilter.toCollectionItemType(),
        sortBy: CollectionItemSortOption? = currentSortOption.toCollectionItemSortOption(),
        forceRefresh: Boolean = false,
    ) {
        val response = getLearnLearningLibraryItemsUseCase(
            GetLearnLearningLibraryItemsParams(
                cursor = cursor,
                limit = itemPageSize,
                searchQuery = searchQuery,
                typeFilter = filterType,
                bookmarkedOnly = false,
                completedOnly = false,
                sortBy = sortBy,
                forceRefresh = forceRefresh,
            )
        )
        val recommendedItemsList = fetchRecommendedItems()
        val offlineContext = offlineCardStateHelper.buildContext(
            response.items.map { it.canvasCourse?.courseImageUrl }
        )

        if (response.pageInfo.hasNextPage && response.pageInfo.nextCursor != null) {
            itemNextCursor = response.pageInfo.nextCursor
            _uiState.update { it.copy(itemState = it.itemState.copy(showMoreButton = true)) }
        } else {
            itemNextCursor = null
            _uiState.update { it.copy(itemState = it.itemState.copy(showMoreButton = false)) }
        }

        _uiState.update {
            it.copy(
                itemState = it.itemState.copy(
                    items = if (cursor == null)
                        response.items.map { item -> item.toUiState(resources, recommendedItemsList, offlineContext.isOffline, offlineContext.resolvedImageUrls) }
                    else
                        it.itemState.items + response.items.map { item -> item.toUiState(resources, recommendedItemsList, offlineContext.isOffline, offlineContext.resolvedImageUrls) }
                ),
            )
        }
    }

    private fun refreshItems() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isRefreshing = true))) }
            fetchItems(cursor = null, forceRefresh = true)
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isRefreshing = false, isError = false))) }
        } catch {
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isRefreshing = false, snackbarMessage = resources.getString(
                R.string.learnLearningLibraryFailedToLoadMessage
            )))) }
        }
    }

    private fun toggleBookmark(itemId: String): suspend () -> Boolean = {
        toggleLearnLearningLibraryItemBookmarkUseCase(ToggleLearnLearningLibraryItemBookmarkParams(itemId))
    }

    private fun onCollectionBookmarkItem(itemId: String) {
        if (isOffline()) {
            _uiState.update {
                it.copy(collectionState = it.collectionState.copy(
                    loadingState = it.collectionState.loadingState.copy(
                        snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage)
                    )
                ))
            }
            return
        }
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(
                collections = it.collectionState.collections.map { collectionState ->
                    collectionState.copy(
                        items = collectionState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = true)
                            else collectionItemState
                        }
                    )
                },
                recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                    if (recommendedItemState.id == itemId) recommendedItemState.copy(bookmarkLoading = true)
                    else recommendedItemState
                }
            ))}

            val newIsBookmarked = toggleBookmark(itemId).invoke()

            _uiState.update { it.copy(collectionState = it.collectionState.copy(
                collections = it.collectionState.collections.map { collectionState ->
                    collectionState.copy(
                        items = collectionState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = false, isBookmarked = newIsBookmarked)
                            else collectionItemState
                        }
                    )
                },
                recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                    if (recommendedItemState.id == itemId) recommendedItemState.copy(bookmarkLoading = false, isBookmarked = newIsBookmarked)
                    else recommendedItemState
                }
            ))}
        } catch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(
                collections = it.collectionState.collections.map { collectionState ->
                    collectionState.copy(
                        items = collectionState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = false)
                            else collectionItemState
                        }
                    )
                },
                recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                    if (recommendedItemState.id == itemId) recommendedItemState.copy(bookmarkLoading = false)
                    else recommendedItemState
                },
                loadingState = it.collectionState.loadingState.copy(snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage)))) }
        }
    }

    private fun onBookmarkItem(itemId: String) {
        if (isOffline()) {
            _uiState.update {
                it.copy(itemState = it.itemState.copy(
                    loadingState = it.itemState.loadingState.copy(
                        snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage)
                    )
                ))
            }
            return
        }
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    itemState = it.itemState.copy(
                        items = it.itemState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = true)
                            else collectionItemState
                        }
                    ),
                    collectionState = it.collectionState.copy(
                        collections = it.collectionState.collections.map { collectionState ->
                            collectionState.copy(
                                items = collectionState.items.map { collectionItemState ->
                                    if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = true)
                                    else collectionItemState
                                }
                            )
                        },
                        recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                            if (recommendedItemState.id == itemId) recommendedItemState.copy(bookmarkLoading = true)
                            else recommendedItemState
                        }
                    )
                )
            }

            val newIsBookmarked = toggleBookmark(itemId).invoke()

            _uiState.update {
                it.copy(
                    itemState = it.itemState.copy(
                        items = it.itemState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) collectionItemState.copy(isBookmarked = newIsBookmarked, bookmarkLoading = false)
                            else collectionItemState
                        }
                    ),
                    collectionState = it.collectionState.copy(
                        collections = it.collectionState.collections.map { collectionState ->
                            collectionState.copy(
                                items = collectionState.items.map { collectionItemState ->
                                    if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = false, isBookmarked = newIsBookmarked)
                                    else collectionItemState
                                }
                            )
                        },
                        recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                            if (recommendedItemState.id == itemId) recommendedItemState.copy(bookmarkLoading = false, isBookmarked = newIsBookmarked)
                            else recommendedItemState
                        }
                    )
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    itemState = it.itemState.copy(
                        items = it.itemState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = false)
                            else collectionItemState
                        },
                        loadingState = it.itemState.loadingState.copy(snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage))
                    ),
                    collectionState = it.collectionState.copy(
                        collections = it.collectionState.collections.map { collectionState ->
                            collectionState.copy(
                                items = collectionState.items.map { collectionItemState ->
                                    if (collectionItemState.id == itemId) collectionItemState.copy(bookmarkLoading = false)
                                    else collectionItemState
                                }
                            )
                        },
                        recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                            if (recommendedItemState.id == itemId) recommendedItemState.copy(bookmarkLoading = false)
                            else recommendedItemState
                        }
                    )
                )
            }
        }
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(
            itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(snackbarMessage = null)),
            collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(snackbarMessage = null))
        ) }
    }

    private fun increaseCollectionsToDisplay() {
        _uiState.update { it.copy(collectionState = it.collectionState.copy(itemsToDisplay = it.collectionState.itemsToDisplay + collectionPageSize)) }
    }

    private fun increaseItemsToDisplay() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(itemState = it.itemState.copy(isMoreButtonLoading = true)) }
            fetchItems(cursor = itemNextCursor)
            _uiState.update { it.copy(itemState = it.itemState.copy(isMoreButtonLoading = false)) }
        } catch {
            _uiState.update { it.copy(itemState = it.itemState.copy(isMoreButtonLoading = false)) }
        }
    }

    private fun updateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
        searchQueryFlow.tryEmit(value.text)
    }

    private fun computeActiveFilterCount(): Int {
        return if (currentTypeFilter != LearnLearningLibraryTypeFilter.All) 1 else 0
    }

    companion object {
        private const val itemLimitPerCollection = 4
    }
}
