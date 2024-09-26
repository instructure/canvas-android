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
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.R
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
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
    settingsBehaviour: SettingsBehaviour,
    @ApplicationContext private val context: Context,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val colorKeeper: ColorKeeper,
    private val themePrefs: ThemePrefs
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appTheme = themePrefs.appTheme,
            onClick = this::onItemClick,
            actionHandler = this::actionHandler
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SettingsViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        if (settingsBehaviour.settingsItems.any { it.value.contains(SettingsItem.OFFLINE_SYNCHRONIZATION) }) {
            viewModelScope.launch {
                syncSettingsFacade.getSyncSettingsListenable().asFlow()
                    .collectLatest { syncSettings ->
                        _uiState.update {
                            it.copy(
                                offlineState = if (syncSettings?.autoSyncEnabled == true) {
                                    syncSettings.syncFrequency.readable
                                } else {
                                    R.string.syncSettings_manualDescription
                                }
                            )
                        }
                    }
            }
        }
        _uiState.update {
            it.copy(
                items = settingsBehaviour.settingsItems,
            )
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        _uiState.update { it.copy(appTheme = theme.themeNameRes) }
    }

    private fun onItemClick(item: SettingsItem) {
        viewModelScope.launch {
            _events.send(SettingsViewModelAction.Navigate(item))
        }
    }

    private fun actionHandler(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetAppTheme -> {
                viewModelScope.launch {
                    _events.send(SettingsViewModelAction.AppThemeClickPosition(action.xPos, action.yPos))
                }
                setAppTheme(action.appTheme)
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

        _uiState.update {
            it.copy(
                appTheme = appTheme.ordinal,
            )
        }
    }
}