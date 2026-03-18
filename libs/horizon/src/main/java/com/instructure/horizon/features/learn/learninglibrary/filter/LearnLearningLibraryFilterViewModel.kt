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
package com.instructure.horizon.features.learn.learninglibrary.filter

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.navigation.LearnRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryFilterViewModel @Inject constructor(
    private val resources: Resources,
    private val eventHandler: LearnEventHandler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val screenType: LearnLearningLibraryFilterScreenType = savedStateHandle
        .get<String>(LearnRoute.LearnLearningLibraryFilterScreen.screenTypeAttr)
        ?.let { LearnLearningLibraryFilterScreenType.valueOf(it) }
        ?: LearnLearningLibraryFilterScreenType.Browse

    private var currentTypeFilter: LearnLearningLibraryTypeFilter = savedStateHandle
        .get<String>(LearnRoute.LearnLearningLibraryFilterScreen.typeFilterAttr)
        ?.let { LearnLearningLibraryTypeFilter.valueOf(it) }
        ?: LearnLearningLibraryTypeFilter.All

    private var currentSortOption: LearnLearningLibrarySortOption = savedStateHandle
        .get<String>(LearnRoute.LearnLearningLibraryFilterScreen.sortOptionAttr)
        ?.let { LearnLearningLibrarySortOption.valueOf(it) }
        ?: LearnLearningLibrarySortOption.MostRecent

    private val _uiState = MutableStateFlow(buildUiState())
    val uiState = _uiState.asStateFlow()

    private fun buildUiState() = LearnLearningLibraryFilterUiState(
        sections = buildSections(),
        onClearFilters = ::clearFilters
    )

    private fun buildSections(): List<LearnLearningLibraryFilterSection> {
        val sections = mutableListOf<LearnLearningLibraryFilterSection>()

        sections.add(
            LearnLearningLibraryFilterSection(
                title = resources.getString(R.string.learnLearningLibraryFilterSortByLabel),
                items = LearnLearningLibrarySortOption.entries.map { option ->
                    LearnLearningLibraryFilterItem(
                        label = resources.getString(option.labelRes),
                        isSelected = currentSortOption == option,
                        onSelected = { updateSortOption(option) }
                    )
                }
            )
        )

        val availableTypeFilters = when (screenType) {
            LearnLearningLibraryFilterScreenType.Browse,
                LearnLearningLibraryFilterScreenType.MyContentSaved -> listOf(
                LearnLearningLibraryTypeFilter.All,
                LearnLearningLibraryTypeFilter.Assessments,
                LearnLearningLibraryTypeFilter.Courses,
                LearnLearningLibraryTypeFilter.ExternalLinks,
                LearnLearningLibraryTypeFilter.ExternalTools,
                LearnLearningLibraryTypeFilter.Files,
                LearnLearningLibraryTypeFilter.Pages
            )
            LearnLearningLibraryFilterScreenType.MyContent -> listOf(
                LearnLearningLibraryTypeFilter.All,
                LearnLearningLibraryTypeFilter.Courses
            )
        }
        sections.add(
            LearnLearningLibraryFilterSection(
                title = resources.getString(R.string.learnLearningLibraryFilterItemTypeLabel),
                items = availableTypeFilters.map { filter ->
                    LearnLearningLibraryFilterItem(
                        label = resources.getString(filter.labelRes),
                        isSelected = currentTypeFilter == filter,
                        onSelected = { updateTypeFilter(filter) }
                    )
                }
            )
        )

        return sections
    }

    private fun updateTypeFilter(value: LearnLearningLibraryTypeFilter) {
        currentTypeFilter = value
        rebuildAndEmit()
    }

    private fun updateSortOption(value: LearnLearningLibrarySortOption) {
        currentSortOption = value
        rebuildAndEmit()
    }

    private fun clearFilters() {
        currentTypeFilter = LearnLearningLibraryTypeFilter.All
        currentSortOption = LearnLearningLibrarySortOption.MostRecent
        rebuildAndEmit()
    }

    private fun rebuildAndEmit() {
        _uiState.update { buildUiState() }
        viewModelScope.launch {
            eventHandler.postEvent(
                LearnEvent.UpdateLearningLibraryFilter(
                    screenType = screenType,
                    typeFilter = currentTypeFilter,
                    sortOption = currentSortOption,
                )
            )
        }
    }
}
