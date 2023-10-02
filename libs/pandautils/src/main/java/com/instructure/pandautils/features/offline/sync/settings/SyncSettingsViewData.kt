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

data class SyncSettingsViewData(
    val autoSyncEnabled: Boolean,
    val syncFrequency: String,
    val wifiOnly: Boolean
)

sealed class SyncSettingsAction {
    data class ShowFrequencySelector(
        val items: List<String>,
        val selectedItemPosition: Int,
        val onItemSelected: (Int) -> Unit
    ) : SyncSettingsAction()

    data class ShowWifiConfirmation(
        val confirmationCallback: (Boolean) -> Unit
    ) : SyncSettingsAction()
}