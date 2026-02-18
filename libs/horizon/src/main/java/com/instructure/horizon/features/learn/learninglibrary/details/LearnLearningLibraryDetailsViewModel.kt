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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val repository: LearnLearningLibraryDetailsRepository
): ViewModel() {
    private val collectionId = savedStateHandle.get<String>(LearnRoute.LearnLearningLibraryDetailsScreen.collectionIdIdAttr) ?: ""

    private var allItems: List<LearnLearningLibraryCollectionItemState> = emptyList()
    private val pageSize = 10

    private val _uiState = MutableStateFlow(LearnLearningLibraryDetailsUiState(
        loadingState = LoadingState(
            onRefresh = ::refreshData,
            onSnackbarDismiss = ::onDismissSnackbar
        ),
        itemsToDisplays = pageSize,
        increaseItemsToDisplay = ::increaseItemsToDisplay,
        updateSearchQuery = ::updateSearchQuery,
        updateSelectedStatusFilter = ::updateSelectedStatusFilter,
        updateTypeFilter = ::updateSelectedTypeFilter,
        onBookmarkClicked = ::toggleItemBookmarked,
        onEnrollClicked = ::onEnrollItem
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            val collection = fetchData()
            _uiState.update {
                it.copy(
                    collectionName = collection.name,
                    items = collection.items.map { it.toUiState(resources) }.applyFilters(),
                )
            }
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
            _uiState.update {
                it.copy(
                    collectionName = collection.name,
                    items = collection.items.map { it.toUiState(resources) }.applyFilters(),
                )
            }
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, isError = false)) }
        } catch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(
                        isRefreshing = false,
                        snackbarMessage = resources.getString(R.string.learnLarningLibraryFailedToLoadCollectionMessage)
                    )
                )
            }
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean = false): EnrolledLearningLibraryCollection {
        val result = repository.getLearningLibraryItems(collectionId, forceRefresh)
        allItems = result.items.map { it.toUiState(resources) }

        return result
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

            allItems = allItems.map { collectionItemState ->
                if (collectionItemState.id == itemId) {
                    collectionItemState.copy(isBookmarked = newIsBookmarked)
                } else {
                    collectionItemState
                }
            }

            _uiState.update {
                it.copy(items = allItems.applyFilters())
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

            allItems = allItems.map { collectionItemState ->
                if (collectionItemState.id == itemId) {
                    newItem.toUiState(resources)
                } else {
                    collectionItemState
                }
            }

            _uiState.update {
                it.copy(items = allItems.applyFilters())
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

    private fun List<LearnLearningLibraryCollectionItemState>.applyFilters(): List<LearnLearningLibraryCollectionItemState> {
        return this.filter {
            it.name.contains(_uiState.value.searchQuery.text.trim(), ignoreCase = true)
                    && it.isSatisfyStatusFilter(_uiState.value.selectedStatusFilter)
                    && it.isSatisfyTypeFilter(_uiState.value.selectedTypeFilter)
        }
    }

    private fun LearnLearningLibraryCollectionItemState.isSatisfyStatusFilter(statusFilter: LearnLearningLibraryDetailsStatusFilter): Boolean {
        return when (statusFilter) {
            LearnLearningLibraryDetailsStatusFilter.All -> true
            LearnLearningLibraryDetailsStatusFilter.Completed -> this.isCompleted
            LearnLearningLibraryDetailsStatusFilter.Bookmarked -> this.isBookmarked
        }
    }

    private fun LearnLearningLibraryCollectionItemState.isSatisfyTypeFilter(typeFilter: LearnLearningLibraryDetailsTypeFilter): Boolean {
        return when (typeFilter) {
            LearnLearningLibraryDetailsTypeFilter.All -> true
            LearnLearningLibraryDetailsTypeFilter.Assessments -> false
            LearnLearningLibraryDetailsTypeFilter.Assignments -> this.type == CollectionItemType.ASSIGNMENT
            LearnLearningLibraryDetailsTypeFilter.ExternalLinks -> this.type == CollectionItemType.EXTERNAL_URL
            LearnLearningLibraryDetailsTypeFilter.ExternalTools -> this.type == CollectionItemType.EXTERNAL_TOOL
            LearnLearningLibraryDetailsTypeFilter.Files -> this.type == CollectionItemType.FILE
            LearnLearningLibraryDetailsTypeFilter.Pages -> this.type == CollectionItemType.PAGE
        }
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }

    private fun increaseItemsToDisplay() {
        _uiState.update { it.copy(itemsToDisplays = it.itemsToDisplays + pageSize) }
    }

    private fun updateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
        _uiState.update { it.copy(items = allItems.applyFilters()) }
    }

    private fun updateSelectedStatusFilter(value: LearnLearningLibraryDetailsStatusFilter) {
        _uiState.update { it.copy(selectedStatusFilter = value) }
        _uiState.update { it.copy(items = allItems.applyFilters()) }
    }

    private fun updateSelectedTypeFilter(value: LearnLearningLibraryDetailsTypeFilter) {
        _uiState.update { it.copy(selectedTypeFilter = value) }
        _uiState.update { it.copy(items = allItems.applyFilters()) }
    }
}