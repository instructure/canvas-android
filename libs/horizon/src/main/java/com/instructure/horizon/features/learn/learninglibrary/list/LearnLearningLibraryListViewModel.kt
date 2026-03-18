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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.horizonui.platform.LoadingState
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
    private val repository: LearnLearningLibraryListRepository,
    private val eventHandler: LearnEventHandler,
    private val apiPrefs: ApiPrefs,
): ViewModel() {

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

    private suspend fun fetchRecommendedItems(forceNetwork: Boolean = false): List<LearningLibraryRecommendation> {
        val recommendations = repository.getLearningLibraryRecommendedItems(forceNetwork)
        _uiState.update {
            it.copy(collectionState = it.collectionState.copy(
                recommendedItems = recommendations.map { it.toUiState(resources) }
            ))
        }
        return recommendations
    }

    private fun loadCollections() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isLoading = true))) }
            val result = fetchCollections()
            val recommendedItems = fetchRecommendedItems()
            allCollections = result.toUiState(resources, recommendedItems)
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
            fetchItems(cursor, searchQuery, typeFilter, sortBy = sortBy)
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isLoading = false))) }
        } catch {
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isLoading = false, isError = true))) }
        }
    }

    private fun refreshCollections() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isRefreshing = true))) }
            val result = fetchCollections(true)
            val recommendedItems = fetchRecommendedItems(true)
            allCollections = result.toUiState(resources, recommendedItems)
            _uiState.update { it.copy(collectionState = it.collectionState.copy(collections = allCollections)) }
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isRefreshing = false, isError = false))) }
        } catch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(loadingState = it.collectionState.loadingState.copy(isRefreshing = false, snackbarMessage = resources.getString(
                R.string.learnLearningLibraryFailedToLoadMessage
            )))) }
        }
    }

    private suspend fun fetchCollections(forceNetwork: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return repository.getEnrolledLearningLibraries(forceNetwork)
    }

    private suspend fun fetchItems(
        cursor: String? = itemNextCursor,
        searchQuery: String? = uiState.value.searchQuery.text,
        filterType: CollectionItemType? = currentTypeFilter.toCollectionItemType(),
        sortBy: CollectionItemSortOption? = currentSortOption.toCollectionItemSortOption(),
        forceNetwork: Boolean = false
    ) {
        val response = repository.getLearningLibraryItems(
            afterCursor = cursor,
            limit = itemPageSize,
            searchQuery = searchQuery,
            typeFilter = filterType,
            sortBy = sortBy,
            forceNetwork = forceNetwork
        )
        val recommendedItemsList = fetchRecommendedItems()

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
                        response.items.map { item -> item.toUiState(resources, recommendedItemsList) }
                    else
                        it.itemState.items + response.items.map { item -> item.toUiState(resources, recommendedItemsList) }
                ),
            )
        }
    }

    private fun refreshItems() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isRefreshing = true))) }
            fetchItems(cursor = null, forceNetwork = true)
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isRefreshing = false, isError = false))) }
        } catch {
            _uiState.update { it.copy(itemState = it.itemState.copy(loadingState = it.itemState.loadingState.copy(isRefreshing = false, snackbarMessage = resources.getString(
                R.string.learnLearningLibraryFailedToLoadMessage
            )))) }
        }
    }

    private fun onCollectionBookmarkItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(
                collections = it.collectionState.collections.map { collectionState ->
                    collectionState.copy(
                        items = collectionState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) {
                                collectionItemState.copy(bookmarkLoading = true)
                            } else {
                                collectionItemState
                            }
                        }
                    )
                },
                recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                    if (recommendedItemState.id == itemId) {
                        recommendedItemState.copy(bookmarkLoading = true)
                    } else {
                        recommendedItemState
                    }
                }
            ))}

            val newIsBookmarked = repository.toggleLearningLibraryItemIsBookmarked(itemId)

            _uiState.update { it.copy(collectionState = it.collectionState.copy(
                collections = it.collectionState.collections.map { collectionState ->
                    collectionState.copy(
                        items = collectionState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) {
                                collectionItemState.copy(
                                    bookmarkLoading = false,
                                    isBookmarked = newIsBookmarked
                                )
                            } else {
                                collectionItemState
                            }
                        }
                    )
                },
                recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                    if (recommendedItemState.id == itemId) {
                        recommendedItemState.copy(
                            bookmarkLoading = false,
                            isBookmarked = newIsBookmarked
                        )
                    } else {
                        recommendedItemState
                    }
                }
            ))}
        } catch {
            _uiState.update { it.copy(collectionState = it.collectionState.copy(
                collections = it.collectionState.collections.map { collectionState ->
                    collectionState.copy(
                        items = collectionState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) {
                                collectionItemState.copy(
                                    bookmarkLoading = false,
                                )
                            } else {
                                collectionItemState
                            }
                        }
                    )
                },
                recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                    if (recommendedItemState.id == itemId) {
                        recommendedItemState.copy(
                            bookmarkLoading = false,
                        )
                    } else {
                        recommendedItemState
                    }
                },
                loadingState = it.collectionState.loadingState.copy(snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage)))) }
        }
    }

    private fun onBookmarkItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    itemState = it.itemState.copy(
                        items = it.itemState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) {
                                collectionItemState.copy(bookmarkLoading = true)
                            } else {
                                collectionItemState
                            }
                        }
                    ),
                    collectionState = it.collectionState.copy(
                        collections = it.collectionState.collections.map { collectionState ->
                            collectionState.copy(
                                items = collectionState.items.map { collectionItemState ->
                                    if (collectionItemState.id == itemId) {
                                        collectionItemState.copy(
                                            bookmarkLoading = true,
                                        )
                                    } else {
                                        collectionItemState
                                    }
                                }
                            )
                        },
                        recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                            if (recommendedItemState.id == itemId) {
                                recommendedItemState.copy(bookmarkLoading = true)
                            } else {
                                recommendedItemState
                            }
                        }
                    )
                )
            }

            val newIsBookmarked = repository.toggleLearningLibraryItemIsBookmarked(itemId)

            _uiState.update {
                it.copy(
                    itemState = it.itemState.copy(
                        items = it.itemState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) {
                                collectionItemState.copy(
                                    isBookmarked = newIsBookmarked,
                                    bookmarkLoading = false
                                )
                            } else {
                                collectionItemState
                            }
                        }
                    ),
                    collectionState = it.collectionState.copy(
                        collections = it.collectionState.collections.map { collectionState ->
                            collectionState.copy(
                                items = collectionState.items.map { collectionItemState ->
                                    if (collectionItemState.id == itemId) {
                                        collectionItemState.copy(
                                            bookmarkLoading = false,
                                            isBookmarked = newIsBookmarked
                                        )
                                    } else {
                                        collectionItemState
                                    }
                                }
                            )
                        },
                        recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                            if (recommendedItemState.id == itemId) {
                                recommendedItemState.copy(
                                    bookmarkLoading = false,
                                    isBookmarked = newIsBookmarked
                                )
                            } else {
                                recommendedItemState
                            }
                        }
                    )
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    itemState = it.itemState.copy(
                        items = it.itemState.items.map { collectionItemState ->
                            if (collectionItemState.id == itemId) {
                                collectionItemState.copy(bookmarkLoading = false)
                            } else {
                                collectionItemState
                            }
                        },
                        loadingState = it.itemState.loadingState.copy(snackbarMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage))
                    ),
                    collectionState = it.collectionState.copy(
                        collections = it.collectionState.collections.map { collectionState ->
                            collectionState.copy(
                                items = collectionState.items.map { collectionItemState ->
                                    if (collectionItemState.id == itemId) {
                                        collectionItemState.copy(
                                            bookmarkLoading = false,
                                        )
                                    } else {
                                        collectionItemState
                                    }
                                }
                            )
                        },
                        recommendedItems = it.collectionState.recommendedItems.map { recommendedItemState ->
                            if (recommendedItemState.id == itemId) {
                                recommendedItemState.copy(bookmarkLoading = false)
                            } else {
                                recommendedItemState
                            }
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
}