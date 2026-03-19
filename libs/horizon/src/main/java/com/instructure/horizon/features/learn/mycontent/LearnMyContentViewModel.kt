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
package com.instructure.horizon.features.learn.mycontent

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnMyContentViewModel @Inject constructor(
    private val eventHandler: LearnEventHandler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LearnMyContentUiState(
            updateSearchQuery = ::updateSearchQuery,
            onTabSelected = ::onTabSelected,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            eventHandler.events.collectLatest { event ->
                when (event) {
                    is LearnEvent.UpdateLearningLibraryFilter -> {
                        if (event.screenType == LearnLearningLibraryFilterScreenType.MyContent ||
                            event.screenType == LearnLearningLibraryFilterScreenType.MyContentSaved
                        ) {
                            _uiState.update {
                                it.copy(
                                    sortByOption = event.sortOption,
                                    typeFilter = event.typeFilter,
                                    activeFilterCount = computeActiveFilterCount(event.typeFilter),
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun computeActiveFilterCount(typeFilter: LearnLearningLibraryTypeFilter): Int {
        return if (typeFilter != LearnLearningLibraryTypeFilter.All) 1 else 0
    }

    private fun onTabSelected(tab: LearnMyContentTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                sortByOption = LearnLearningLibrarySortOption.MostRecent,
                typeFilter = LearnLearningLibraryTypeFilter.All,
                activeFilterCount = 0,
            )
        }
    }

    private fun updateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
    }
}
