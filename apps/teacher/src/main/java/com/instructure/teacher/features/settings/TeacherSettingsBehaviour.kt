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
package com.instructure.teacher.features.settings

import com.instructure.teacher.BuildConfig
import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsItem
import com.instructure.teacher.R

class TeacherSettingsBehaviour : SettingsBehaviour {
    override val settingsItems: Map<Int, List<SettingsItem>>
        get() {
            val preferencesList = mutableListOf(
                SettingsItem.APP_THEME,
                SettingsItem.PROFILE_SETTINGS,
                SettingsItem.PUSH_NOTIFICATIONS,
                SettingsItem.EMAIL_NOTIFICATIONS,
                SettingsItem.RATE_APP
            )
            if (BuildConfig.DEBUG) {
                preferencesList.add(SettingsItem.FEATURE_FLAGS)
                preferencesList.add(SettingsItem.REMOTE_CONFIG)
            }
            return mapOf(
                R.string.preferences to preferencesList,
                R.string.legal to listOf(
                    SettingsItem.ABOUT,
                    SettingsItem.LEGAL
                )
            )
        }
}