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

import androidx.annotation.StringRes
import com.instructure.horizon.R

data class OfflineSettingsUiState(
    val autoSyncEnabled: Boolean = true,
    val syncFrequency: SyncFrequency = SyncFrequency.DAILY,
    val wifiOnlyEnabled: Boolean = true,
    val onAutoSyncToggled: (Boolean) -> Unit = {},
    val onSyncFrequencySelected: (SyncFrequency) -> Unit = {},
    val onWifiOnlyToggled: (Boolean) -> Unit = {},
    val onManageOfflineContentClick: () -> Unit = {},
)

enum class SyncFrequency(@StringRes val labelRes: Int) {
    DAILY(R.string.offline_syncFrequencyDaily),
    WEEKLY(R.string.offline_syncFrequencyWeekly),
}
