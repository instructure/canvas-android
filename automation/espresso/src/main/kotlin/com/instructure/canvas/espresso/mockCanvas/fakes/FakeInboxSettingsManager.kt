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
package com.instructure.canvas.espresso.mockCanvas.fakes

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import com.instructure.canvasapi2.utils.DataResult

class FakeInboxSettingsManager : InboxSettingsManager {
    override suspend fun getInboxSignatureSettings(forceNetwork: Boolean): DataResult<InboxSignatureSettings> =
        DataResult.Success(InboxSignatureSettings(MockCanvas.data.inboxSignature, MockCanvas.data.signatureEnabled))

    override suspend fun updateInboxSignatureSettings(inboxSignatureSettings: InboxSignatureSettings): DataResult<InboxSignatureSettings> {
        MockCanvas.data.inboxSignature = inboxSignatureSettings.signature
        MockCanvas.data.signatureEnabled = inboxSignatureSettings.useSignature
        return DataResult.Success(inboxSignatureSettings)
    }
}