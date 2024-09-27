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

import com.instructure.pandautils.utils.AppTheme

data class SettingsUiState(
    val appTheme: Int,
    val homeroomView: Boolean,
    val items: Map<Int, List<SettingsItem>> = emptyMap(),
    val offlineState: Int? = null,
    val actionHandler: (SettingsAction) -> Unit
)

sealed class SettingsViewModelAction {
    data class Navigate(val item: SettingsItem) : SettingsViewModelAction()
    data class AppThemeClickPosition(val xPos: Int, val yPos: Int) : SettingsViewModelAction()
}

sealed class SettingsAction {
    data class SetAppTheme(val appTheme: AppTheme, val xPos: Int, val yPos: Int) : SettingsAction()

    data class SetHomeroomView(val homeroomView: Boolean) : SettingsAction()

    data class ItemClicked(val settingsItem: SettingsItem) : SettingsAction()
}