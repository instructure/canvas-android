/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsBehaviour: SettingsBehaviour,
    @ApplicationContext private val context: Context,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val colorKeeper: ColorKeeper,
    private val themePrefs: ThemePrefs,
    private val apiPrefs: ApiPrefs,
    private val analytics: Analytics,
    private val settingsRepository: SettingsRepository
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appTheme = themePrefs.appTheme,
            homeroomView = apiPrefs.elementaryDashboardEnabledOverride,
            actionHandler = this::actionHandler
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SettingsViewModelAction>()
    val events = _events.receiveAsFlow()

    private val offlineEnabled = savedStateHandle.get<Boolean>(OFFLINE_ENABLED) ?: false
    private val scrollValue = savedStateHandle.get<Int>("scrollValue") ?: 0

    init {
        _uiState.update { it.copy(loading = true) }

        val items = settingsBehaviour.settingsItems.filter {
            if (it.value.contains(SettingsItem.OFFLINE_SYNCHRONIZATION)) {
                offlineEnabled
            } else {
                true
            }
        }.mapValues { entry -> entry.value.map { SettingsItemUiState(it) } }
        _uiState.update {
            it.copy(
                items = items,
                scrollValue = scrollValue
            )
        }

        viewModelScope.tryLaunch {
            val inboxSignatureState = settingsRepository.getInboxSignatureState()
            if (inboxSignatureState == InboxSignatureState.HIDDEN) {
                _uiState.update { it.copy(items = it.items.minus(R.string.inboxSettingsTitle)) }
            } else {
                inboxSignatureState.textRes?.let {
                    changeSettingsItemSubtitle(SettingsItem.INBOX_SIGNATURE, it)
                }
            }
            _uiState.update { it.copy(loading = false) }
        } catch {
            _uiState.update { it.copy(loading = false) }
        }

        if (items.any { item -> item.value.any { it.item == SettingsItem.OFFLINE_SYNCHRONIZATION } }) {
            viewModelScope.launch {
                syncSettingsFacade.getSyncSettingsListenable().asFlow()
                    .collectLatest { syncSettings ->
                        val offlineSyncSubtitle = if (syncSettings?.autoSyncEnabled == true) {
                            syncSettings.syncFrequency.readable
                        } else {
                            R.string.syncSettings_manualDescription
                        }
                        changeSettingsItemSubtitle(SettingsItem.OFFLINE_SYNCHRONIZATION, offlineSyncSubtitle)
                    }
            }
        }
    }

    private fun actionHandler(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetAppTheme -> {
                viewModelScope.launch {
                    _events.send(
                        SettingsViewModelAction.AppThemeClickPosition(
                            action.xPos,
                            action.yPos,
                            action.scrollValue
                        )
                    )
                }
                setAppTheme(action.appTheme)
                when (action.appTheme) {
                    AppTheme.LIGHT -> analytics.logEvent(AnalyticsEventConstants.DARK_MODE_OFF)
                    AppTheme.DARK -> analytics.logEvent(AnalyticsEventConstants.DARK_MODE_ON)
                    AppTheme.SYSTEM -> analytics.logEvent(AnalyticsEventConstants.DARK_MODE_SYSTEM)
                }
            }

            is SettingsAction.SetHomeroomView -> {
                apiPrefs.elementaryDashboardEnabledOverride = action.homeroomView
                _uiState.update {
                    it.copy(homeroomView = action.homeroomView)
                }
            }

            is SettingsAction.ItemClicked -> {
                viewModelScope.launch {
                    _events.send(SettingsViewModelAction.Navigate(action.settingsItem))
                }
            }
        }
    }

    private fun setAppTheme(appTheme: AppTheme) {
        AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)
        themePrefs.appTheme = appTheme.ordinal

        val nightModeFlags: Int =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        colorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        themePrefs.isThemeApplied = false
        viewModelScope.launch {
            delay(100)
            settingsBehaviour.applyAppSpecificColorSettings()
        }

        _uiState.update {
            it.copy(
                appTheme = appTheme.ordinal,
            )
        }
    }

    private fun changeSettingsItemSubtitle(item: SettingsItem, @StringRes subtitle: Int) {
        _uiState.update { uiState ->
            uiState.copy(
                items = uiState.items.mapValues { entry ->
                    entry.value.map {
                        if (it.item == item) {
                            it.copy(subtitleRes = subtitle)
                        } else {
                            it
                        }
                    }
                }
            )
        }
    }

    fun updateSignatureSettings(enabled: Boolean) {
        viewModelScope.launch {
            val state = if (enabled) InboxSignatureState.ENABLED else InboxSignatureState.DISABLED
            changeSettingsItemSubtitle(SettingsItem.INBOX_SIGNATURE, state.textRes!!)
        }
    }
}