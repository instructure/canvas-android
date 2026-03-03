/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.instructure.horizon.features.learn.navigation.LearnRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(LearnUiState(
        updateSelectedTab = ::updateSelectedTab,
        updateSelectedTabIndex = ::updateSelectedTabIndex
    ))
    val state = _uiState.asStateFlow()

    init {
        val selectedTabValue = savedStateHandle.get<String>(LearnRoute.LearnScreen.selectedTabAttr)
        LearnTab.fromStringValue(selectedTabValue)?.let { selectedTab ->
            _uiState.update { it.copy(selectedTab = selectedTab) }
        }
    }

    private fun updateSelectedTabIndex(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTab = LearnTab.entries[tabIndex]
        )
    }

    private fun updateSelectedTab(tabStringValue: String) {
        LearnTab.fromStringValue(tabStringValue)?.let { tab ->
            _uiState.value = _uiState.value.copy(
                selectedTab = tab
            )
        }
    }
}