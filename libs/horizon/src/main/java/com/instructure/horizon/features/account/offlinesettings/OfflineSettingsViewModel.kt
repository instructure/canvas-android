/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.offlinesettings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class OfflineSettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        OfflineSettingsUiState(
            onAutoSyncToggled = ::onAutoSyncToggled,
            onSyncFrequencySelected = ::onSyncFrequencySelected,
            onWifiOnlyToggled = ::onWifiOnlyToggled,
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun onAutoSyncToggled(enabled: Boolean) {
        _uiState.update { it.copy(autoSyncEnabled = enabled) }
    }

    private fun onSyncFrequencySelected(frequency: SyncFrequency) {
        _uiState.update { it.copy(syncFrequency = frequency) }
    }

    private fun onWifiOnlyToggled(enabled: Boolean) {
        _uiState.update { it.copy(wifiOnlyEnabled = enabled) }
    }
}
