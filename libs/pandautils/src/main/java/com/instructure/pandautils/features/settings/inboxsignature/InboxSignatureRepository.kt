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
package com.instructure.pandautils.features.settings.inboxsignature

import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class InboxSignatureRepository @Inject constructor(
    private val inboxSettingsManager: InboxSettingsManager
) {
    suspend fun getInboxSignature(): DataResult<InboxSignatureSettings> {
        return inboxSettingsManager.getInboxSignatureSettings(forceNetwork = true)
    }

    suspend fun updateInboxSignature(inboxSignatureSettings: InboxSignatureSettings): DataResult<InboxSignatureSettings> {
        return inboxSettingsManager.updateInboxSignatureSettings(inboxSignatureSettings)
    }
}