/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.features.settings

import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsItem
import com.instructure.student.BuildConfig
import com.instructure.student.R

class StudentSettingsBehaviour(
    private val apiPrefs: ApiPrefs,
) : SettingsBehaviour {
    override val settingsItems: Map<Int, List<SettingsItem>>
        get() {
            val preferencesList = mutableListOf(
                SettingsItem.APP_THEME,
                SettingsItem.PROFILE_SETTINGS,
                SettingsItem.PUSH_NOTIFICATIONS,
                SettingsItem.EMAIL_NOTIFICATIONS
            )
            if (apiPrefs.canGeneratePairingCode == true) {
                preferencesList.add(SettingsItem.PAIR_WITH_OBSERVER)
            }
            if (apiPrefs.user?.calendar?.ics != null) {
                preferencesList.add(SettingsItem.SUBSCRIBE_TO_CALENDAR)
            }
            if (apiPrefs.canvasForElementary) {
                preferencesList.add(1, SettingsItem.HOMEROOM_VIEW)
            }
            if (BuildConfig.DEBUG) {
                preferencesList.add(SettingsItem.ACCOUNT_PREFERENCES)
                preferencesList.add(SettingsItem.FEATURE_FLAGS)
                preferencesList.add(SettingsItem.REMOTE_CONFIG)
            }

            return mapOf(
                R.string.preferences to preferencesList,
                R.string.offlineContent to listOf(SettingsItem.OFFLINE_SYNCHRONIZATION),
                R.string.legal to listOf(SettingsItem.ABOUT, SettingsItem.LEGAL)
            )
        }
}