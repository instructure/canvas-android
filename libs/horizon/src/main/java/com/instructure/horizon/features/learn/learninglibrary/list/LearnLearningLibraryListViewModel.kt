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
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryListViewModel @Inject constructor(
    private val resources: Resources,
    private val repository: LearnLearningLibraryListRepository
): ViewModel() {

    private var allCollections: List<LearnLearningLibraryCollectionState> = emptyList()
    private val pageSize: Int = 3

    private val _uiState = MutableStateFlow(LearnLearningLibraryListUiState(
        loadingState = LoadingState(
            onRefresh = ::refreshData,
            onSnackbarDismiss = ::onDismissSnackbar
        ),
        itemsToDisplays = pageSize,
        increaseItemsToDisplay = ::increaseItemsToDisplay,
        updateSearchQuery = ::updateSearchQuery,
        onBookmarkClicked = ::onBookmarkItem,
        onEnrollClicked = ::onEnrollItem
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            val result = fetchData()
            allCollections = result.toUiState(resources)
            _uiState.update { it.copy(collections = allCollections.applyFilters()) }
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private fun refreshData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            val result = fetchData(true)
            allCollections = result.toUiState(resources)
            _uiState.update { it.copy(collections = allCollections.applyFilters()) }
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, isError = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, snackbarMessage = "Failed to load Learning Library")) }
        }
    }

    private suspend fun fetchData(forceNetwork: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return repository.getEnrolledLearningLibraries(forceNetwork)
    }

    private fun updateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
        _uiState.update { it.copy(collections = allCollections.applyFilters()) }
    }

    private fun onBookmarkItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(bookmarkLoading = true)
                        } else {
                            collectionItemState
                        }
                    }
                )
            })}

            val newIsBookmarked = repository.toggleLearningLibraryItemIsBookmarked(itemId)

            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
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
            })}
        } catch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
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
            }, loadingState = it.loadingState.copy(errorMessage = resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage)))}
        }
    }

    private fun onEnrollItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(enrollLoading = true)
                        } else {
                            collectionItemState
                        }
                    }
                )
            })}

            val newItem = repository.enrollLearningLibraryItem(itemId)

            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            newItem.toUiState(resources)
                        } else {
                            collectionItemState
                        }
                    }
                )
            })}
        } catch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(
                                enrollLoading = false,
                            )
                        } else {
                            collectionItemState
                        }
                    }
                )
            }, loadingState = it.loadingState.copy(errorMessage = resources.getString(R.string.learnLearningLibraryFailedToEnrollMessage)))}
        }
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }

    private fun increaseItemsToDisplay() {
        _uiState.update { it.copy(itemsToDisplays = it.itemsToDisplays + pageSize) }
    }

    private fun List<LearnLearningLibraryCollectionState>.applyFilters(): List<LearnLearningLibraryCollectionState> {
        return this.filter {
            it.name.contains(_uiState.value.searchQuery.text.trim(), ignoreCase = true)
        }
    }
}