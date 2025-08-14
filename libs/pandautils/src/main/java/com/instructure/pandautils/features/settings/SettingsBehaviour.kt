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

import com.instructure.canvasapi2.models.EnvironmentSettings
import com.instructure.pandautils.R

interface SettingsBehaviour {

    val settingsItems: Map<Int, List<SettingsItem>>

    suspend fun applyAppSpecificColorSettings() = Unit

    fun isInboxSignatureEnabledForRole(settings: EnvironmentSettings?) = true
}

enum class SettingsItem(val res: Int, val availableOffline: Boolean = false) {
    APP_THEME(R.string.appThemeSettingsTitle, true),
    PROFILE_SETTINGS(R.string.profileSettings),
    PUSH_NOTIFICATIONS(R.string.pushNotifications),
    EMAIL_NOTIFICATIONS(R.string.emailNotifications),
    PAIR_WITH_OBSERVER(R.string.pairWithObserver),
    SUBSCRIBE_TO_CALENDAR(R.string.subscribeToCalendar, true),
    INBOX_SIGNATURE(R.string.inboxSettingsInboxSignature),
    OFFLINE_SYNCHRONIZATION(R.string.offlineSyncSettingsTitle, true),
    ABOUT(R.string.about, true),
    LEGAL(R.string.legal, true),
    RATE_APP(R.string.rateOnThePlayStore),
    FEATURE_FLAGS(R.string.featureFlags, true),
    REMOTE_CONFIG(R.string.remoteConfigParamsTitle, true),
    ACCOUNT_PREFERENCES(R.string.accountPreferences, true),
    HOMEROOM_VIEW(R.string.settingsHomeroomView, true),
    SWITCH_EXPERIENCE(R.string.settingsSwitchExperience),
}