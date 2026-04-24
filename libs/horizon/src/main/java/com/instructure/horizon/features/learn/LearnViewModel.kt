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

import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.offline.HorizonOfflineViewModel
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val repository: LearnRepository,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    getLastSyncedAtUseCase: GetLastSyncedAtUseCase
) : HorizonOfflineViewModel(networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase) {

    private val _uiState = MutableStateFlow(LearnUiState(
        updateSelectedTab = ::updateSelectedTab,
        updateSelectedTabIndex = ::updateSelectedTabIndex,
        tabs = listOf(LearnTab.MY_CONTENT)
    ))
    val state = _uiState.asStateFlow()

    init {
        loadBrowseTab()
    }

    override fun onNetworkRestored() {
        _uiState.update { it.copy(isOffline = false, lastSyncedAtMs = null) }
        loadBrowseTab()
    }

    override fun onNetworkLost() {
        viewModelScope.tryLaunch {
            val lastSyncedAt = getLastSyncTime(SyncDataType.LEARN_MY_CONTENT_IN_PROGRESS)
            _uiState.update { it.copy(isOffline = true, lastSyncedAtMs = lastSyncedAt) }
        } catch { }
    }

    private fun loadBrowseTab() {
        viewModelScope.tryLaunch {
            val enrolledLearningLibraries = repository.getEnrolledLearningLibraries()
            val hasBrowseTab = enrolledLearningLibraries.isNotEmpty()
            _uiState.update { current ->
                val tabs = if (hasBrowseTab && LearnTab.BROWSE !in current.tabs) {
                    current.tabs + LearnTab.BROWSE
                } else if (!hasBrowseTab) {
                    current.tabs - LearnTab.BROWSE
                } else {
                    current.tabs
                }
                current.copy(tabs = tabs)
            }
        } catch { }
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
