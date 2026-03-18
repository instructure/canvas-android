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
package com.instructure.horizon.features.learn.mycontent.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.navigation.MainNavigationRoute
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class LearnMyContentViewModel<T>(
    protected val repository: LearnMyContentRepository,
) : ViewModel() {

    private data class Filters(
        val searchQuery: String = "",
        val sortBy: LearnLearningLibrarySortOption = LearnLearningLibrarySortOption.MostRecent,
        val typeFilter: LearnLearningLibraryTypeFilter = LearnLearningLibraryTypeFilter.All,
    )

    private var nextCursor: String? = null
    private var currentFilters = Filters()
    private val filtersFlow = MutableSharedFlow<Filters>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    protected val _uiState = MutableStateFlow(
        LearnMyContentUiState<T>(
            loadingState = LoadingState(
                onRefresh = ::refresh,
                onSnackbarDismiss = ::onSnackbarDismiss,
            ),
            increaseTotalItemCount = ::loadMore,
        )
    )
    val uiState = _uiState.asStateFlow()

    protected abstract val errorMessage: String

    init {
        viewModelScope.launch {
            filtersFlow.collectLatest { filters ->
                val isSearchChange = currentFilters.searchQuery != filters.searchQuery
                currentFilters = filters
                if (isSearchChange) delay(300)
                load()
            }
        }
    }

    fun onFiltersChanged(searchQuery: String, sortBy: LearnLearningLibrarySortOption, typeFilter: LearnLearningLibraryTypeFilter) {
        filtersFlow.tryEmit(Filters(searchQuery, sortBy, typeFilter))
    }

    private fun load() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            nextCursor = null
            fetchAndUpdate(cursor = null)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            nextCursor = null
            fetchAndUpdate(cursor = null, forceNetwork = true)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, isError = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, snackbarMessage = errorMessage)) }
        }
    }

    private fun loadMore() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isMoreLoading = true) }
            fetchAndUpdate(cursor = nextCursor, append = true)
            _uiState.update { it.copy(isMoreLoading = false) }
        } catch {
            _uiState.update { it.copy(isMoreLoading = false) }
        }
    }

    private suspend fun fetchAndUpdate(cursor: String?, forceNetwork: Boolean = false, append: Boolean = false) {
        val (items, pageInfo) = fetchPage(
            cursor = cursor,
            searchQuery = currentFilters.searchQuery,
            sortBy = currentFilters.sortBy.toCollectionItemSortOption(),
            typeFilter = currentFilters.typeFilter,
            forceNetwork = forceNetwork,
        )
        nextCursor = if (pageInfo.hasNextPage) pageInfo.nextCursor else null
        _uiState.update { state ->
            state.copy(
                contentCards = if (append) state.contentCards + items else items,
                totalItemCount = pageInfo.totalCount ?: 0,
                showMoreButton = nextCursor != null,
            )
        }
    }

    protected abstract suspend fun fetchPage(
        cursor: String?,
        searchQuery: String,
        sortBy: CollectionItemSortOption,
        typeFilter: LearnLearningLibraryTypeFilter,
        forceNetwork: Boolean,
    ): Pair<List<T>, LearningLibraryPageInfo>

    protected suspend fun fetchNextModuleItemRoute(courseId: Long?, forceNetwork: Boolean): Any? {
        if (courseId == null) return null
        val modules = repository.getFirstPageModulesWithItems(courseId, forceNetwork = forceNetwork)
        val nextModuleItem = modules.flatMap { module -> module.items }.firstOrNull()
        if (nextModuleItem == null) {
            return null
        }

        return MainNavigationRoute.ModuleItemSequence(
            courseId,
            nextModuleItem.id
        )
    }

    private fun onSnackbarDismiss() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }
}
