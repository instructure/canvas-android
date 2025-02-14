/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.settings

import androidx.annotation.StringRes
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.pandautils.R

class SettingsRepository(private val featuresApi: FeaturesAPI.FeaturesInterface, private val inboxSettingsManager: InboxSettingsManager) {

    suspend fun getInboxSignatureState(): InboxSignatureState {
        val inboxSignatureEnabled = featuresApi.getAccountSettingsFeatures(RestParams()).dataOrNull?.enableInboxSignatureBlock ?: false
        if (inboxSignatureEnabled) {
            val inboxSignature = inboxSettingsManager.getInboxSignatureSettings(forceNetwork = true)
            return if (inboxSignature.useSignature) InboxSignatureState.ENABLED else InboxSignatureState.DISABLED
        } else {
            return InboxSignatureState.HIDDEN
        }
    }
}

enum class InboxSignatureState(@StringRes val textRes: Int? = null) {
    ENABLED(R.string.inboxSignatureEnabled),
    DISABLED(R.string.inboxSignatureNotSet),
    HIDDEN
}