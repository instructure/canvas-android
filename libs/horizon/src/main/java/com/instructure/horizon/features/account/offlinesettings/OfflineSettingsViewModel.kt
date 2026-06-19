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
import androidx.lifecycle.viewModelScope
import com.instructure.horizon.database.dao.HorizonSyncSettingsDao
import com.instructure.horizon.database.entity.HorizonSyncSettingsEntity
import com.instructure.horizon.offline.sync.HorizonAggregateProgressObserver
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class OfflineSettingsViewModel @Inject constructor(
    private val syncSettingsDao: HorizonSyncSettingsDao,
    private val syncHelper: HorizonOfflineSyncHelper,
    private val aggregateProgressObserver: HorizonAggregateProgressObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OfflineSettingsUiState(
            onAutoSyncToggled = ::onAutoSyncToggled,
            onSyncFrequencySelected = ::onSyncFrequencySelected,
            onWifiOnlyToggled = ::onWifiOnlyToggled,
            onWifiOnlyConfirmationConfirmed = ::onWifiOnlyConfirmationConfirmed,
            onWifiOnlyConfirmationDismissed = ::onWifiOnlyConfirmationDismissed,
        )
    )
    val uiState = _uiState.asStateFlow()

    private val pendingSettings = MutableSharedFlow<HorizonSyncSettingsEntity>(extraBufferCapacity = 16)
    private val persistMutex = Mutex()

    init {
        loadSettings()
        observePendingSettings()
        observeSyncProgress()
    }

    private fun observeSyncProgress() {
        aggregateProgressObserver.progressData
            .onEach { data ->
                _uiState.update { it.copy(isSyncActive = data.isActive) }
            }
            .launchIn(viewModelScope)
    }

    private fun observePendingSettings() {
        viewModelScope.launch {
            pendingSettings
                .debounce(DEBOUNCE_MS)
                .collectLatest { settings ->
                    persistMutex.withLock {
                        syncSettingsDao.upsert(settings)
                        syncHelper.applyPeriodicSchedule()
                    }
                }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = syncSettingsDao.getSettingsOnce() ?: HorizonSyncSettingsEntity()
            _uiState.update {
                it.copy(
                    autoSyncEnabled = settings.autoSyncEnabled,
                    syncFrequency = settings.syncFrequency,
                    wifiOnlyEnabled = settings.wifiOnly,
                )
            }
        }
    }

    private fun onAutoSyncToggled(enabled: Boolean) {
        _uiState.update { it.copy(autoSyncEnabled = enabled) }
        emitPending()
    }

    private fun onSyncFrequencySelected(frequency: SyncFrequency) {
        _uiState.update { it.copy(syncFrequency = frequency) }
        emitPending()
    }

    private fun onWifiOnlyToggled(enabled: Boolean) {
        if (enabled) {
            _uiState.update { it.copy(wifiOnlyEnabled = true) }
            emitPending()
        } else {
            _uiState.update { it.copy(showWifiOnlyConfirmation = true) }
        }
    }

    private fun onWifiOnlyConfirmationConfirmed() {
        _uiState.update { it.copy(wifiOnlyEnabled = false, showWifiOnlyConfirmation = false) }
        emitPending()
    }

    private fun onWifiOnlyConfirmationDismissed() {
        _uiState.update { it.copy(showWifiOnlyConfirmation = false) }
    }

    private fun emitPending() {
        val state = _uiState.value
        pendingSettings.tryEmit(
            HorizonSyncSettingsEntity(
                autoSyncEnabled = state.autoSyncEnabled,
                syncFrequency = state.syncFrequency,
                wifiOnly = state.wifiOnlyEnabled,
            )
        )
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
