/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.pandautils.features.offline.sync.settings

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.analytics.OfflineAnalyticsManager
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncSettingsViewModel @Inject constructor(
    private val syncSettingsFacade: SyncSettingsFacade,
    private val offlineSyncHelper: OfflineSyncHelper,
    private val resources: Resources,
    private val offlineAnalyticsManager: OfflineAnalyticsManager
) : ViewModel() {

    val data: LiveData<SyncSettingsViewData>
        get() = _data
    private val _data = MutableLiveData<SyncSettingsViewData>()

    val events: LiveData<Event<SyncSettingsAction>>
        get() = _events
    private val _events = MutableLiveData<Event<SyncSettingsAction>>()

    private lateinit var syncSettings: SyncSettingsEntity

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            syncSettings = syncSettingsFacade.getSyncSettings()
            _data.postValue(
                SyncSettingsViewData(
                    syncSettings.autoSyncEnabled,
                    resources.getString(syncSettings.syncFrequency.readable),
                    syncSettings.wifiOnly
                )
            )
        }
    }

    fun onAutoSyncChanged(checked: Boolean) {
        viewModelScope.launch {
            val updated = syncSettings.copy(
                autoSyncEnabled = checked
            )
            syncSettingsFacade.update(updated)
            if (checked) {
                offlineSyncHelper.scheduleWork()
            } else {
                offlineSyncHelper.cancelWork()
            }
            loadData()
            offlineAnalyticsManager.reportOfflineAutoSyncSwitchChanged(checked)
        }
    }

    fun onWifiOnlyChanged(checked: Boolean) {
        if (checked) {
            updateWifiOnly(checked)
        } else {
            _events.postValue(Event(SyncSettingsAction.ShowWifiConfirmation { confirmed ->
                if (confirmed) {
                    updateWifiOnly(checked)
                } else {
                    loadData()
                }
            }))
        }
    }

    private fun updateWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            val updated = syncSettings.copy(
                wifiOnly = enabled
            )
            syncSettingsFacade.update(updated)
            offlineSyncHelper.updateWork()
            loadData()
        }
    }

    fun showFrequencySelector() {
        val items = SyncFrequency.values()
        val selectedItem = items.indexOf(syncSettings.syncFrequency)

        _events.postValue(
            Event(SyncSettingsAction.ShowFrequencySelector(
                items.map { resources.getString(it.readable) },
                selectedItem,
            ) {
                updateSyncFrequency(items[it])
            })
        )
    }

    private fun updateSyncFrequency(syncFrequency: SyncFrequency) {
        viewModelScope.launch {
            val updated = syncSettings.copy(
                syncFrequency = syncFrequency
            )
            syncSettingsFacade.update(updated)
            offlineSyncHelper.updateWork()
            loadData()
        }
    }
}
