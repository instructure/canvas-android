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
package com.instructure.horizon.features.learn.learninglibrary.bookmark

import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryBookmarkViewModel @Inject constructor(
    private val resources: Resources,
    private val repository: LearnLearningLibraryBookmarkRepository,
): ViewModel() {
    private val pageSize: Int = 10

    private val _uiState = MutableStateFlow(LearnLearningLibraryBookmarkUiState(
        loadingState = LoadingState(
            onRefresh = ::refreshData,
            onSnackbarDismiss = ::onDismissSnackbar
        ),
        updateSearchQuery = ::onUpdateSearchQuery,
        updateTypeFilter = ::onUpdateTypeFilter,
        onShowMoreClicked = ::onShowMoreClicked,
        onBookmarkClicked = ::toggleItemBookmarked,
        onEnrollClicked = ::onEnrollItem
    ))
    val uiState = _uiState.asStateFlow()

    private var nextCursor: String? = null

    init {
        loadData()
    }

    private fun loadData(
        cursor: String? = nextCursor,
        searchQuery: String? = uiState.value.searchQuery.text,
        filterType: CollectionItemType? = uiState.value.typeFilter?.toCollectionItemType(),
    ) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            fetchData(cursor, searchQuery, filterType)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private fun refreshData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            fetchData(cursor = null, forceNetwork = true)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, isError = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, snackbarMessage = resources.getString(
                R.string.learnLearningLibraryBookmarkFailedToLoadMessage
            ))) }
        }
    }

    private suspend fun fetchData(
        cursor: String? = nextCursor,
        searchQuery: String? = uiState.value.searchQuery.text,
        filterType: CollectionItemType? = uiState.value.typeFilter?.toCollectionItemType(),
        forceNetwork: Boolean = false
    ) {
        val response = repository.getBookmarkedLearningLibraryItems(
            afterCursor = cursor,
            limit = pageSize,
            searchQuery = searchQuery,
            typeFilter = filterType,
            forceNetwork = forceNetwork
        )

        if (response.pageInfo.hasNextPage && response.pageInfo.nextCursor != null) {
            nextCursor = response.pageInfo.nextCursor
            _uiState.update { it.copy(
                showMoreButton = true
            ) }
        } else {
            nextCursor = null
            _uiState.update { it.copy(
                showMoreButton = false
            ) }
        }

        _uiState.update {
            it.copy(
                items = if (cursor == null)
                    response.items.map { it.toUiState(resources) }
                else
                    it.items + response.items.map { it.toUiState(resources) }
            )
        }
    }

    private fun onShowMoreClicked() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isMoreButtonLoading = true) }
            fetchData(cursor = nextCursor)
            _uiState.update { it.copy(isMoreButtonLoading = false) }
        } catch {
            _uiState.update { it.copy(
                isMoreButtonLoading = false,
                loadingState = it.loadingState.copy(
                    snackbarMessage = resources.getString(R.string.learnLearningLibraryBookmarkFailedToLoadMessage)
                )
            ) }
        }
    }

    private fun onUpdateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
        loadData(cursor = null)
    }

    private fun onUpdateTypeFilter(value: LearnLearningLibraryTypeFilter?) {
        _uiState.update { it.copy(typeFilter = value) }
        loadData(cursor = null)
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }

    private fun toggleItemBookmarked(itemId: String) {
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

            val newIsBookmarked = repository.toggleLearningLibraryItemIsBookmarked(itemId)

            _uiState.update {
                it.copy(
                    items = it.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(isBookmarked = newIsBookmarked)
                        } else {
                            collectionItemState
                        }
                    }
                )
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
                    loadingState = it.loadingState.copy(errorMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage))
                )
            }
        }
    }

    private fun onEnrollItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    items = it.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(enrollLoading = true)
                        } else {
                            collectionItemState
                        }
                    }
                )
            }

            val newItem = repository.enrollLearningLibraryItem(itemId)

            _uiState.update {
                it.copy(
                    items = it.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            newItem.toUiState(resources)
                        } else {
                            collectionItemState
                        }
                    }
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    items = it.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(
                                enrollLoading = false,
                            )
                        } else {
                            collectionItemState
                        }
                    },
                    loadingState = it.loadingState.copy(errorMessage = resources.getString(R.string.learnLearningLibraryFailedToEnrollMessage))
                )
            }
        }
    }
}