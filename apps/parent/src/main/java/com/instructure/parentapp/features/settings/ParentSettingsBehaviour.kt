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
 */    package com.instructure.parentapp.features.settings

import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsItem
import com.instructure.parentapp.R
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder

class ParentSettingsBehaviour(private val selectedStudentHolder: SelectedStudentHolder) : SettingsBehaviour {
    override val settingsItems: Map<Int, List<SettingsItem>>
        get() = mapOf(
            R.string.preferences to listOf(SettingsItem.APP_THEME),
            R.string.legal to listOf(SettingsItem.ABOUT, SettingsItem.LEGAL)
        )

    override suspend fun applyAppSpecificColorSettings() {
        selectedStudentHolder.selectedStudentColorChanged()
    }
}