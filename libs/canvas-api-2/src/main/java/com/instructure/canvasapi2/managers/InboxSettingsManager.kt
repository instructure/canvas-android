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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.utils.DataResult

interface InboxSettingsManager {

    suspend fun getInboxSignatureSettings(forceNetwork: Boolean = false): DataResult<InboxSignatureSettings>

    suspend fun updateInboxSignatureSettings(inboxSignatureSettings: InboxSignatureSettings): DataResult<InboxSignatureSettings>
}

data class InboxSignatureSettings(
    val signature: String,
    val useSignature: Boolean,
    val useOutOfOffice: Boolean = false,
    val outOfOfficeMessage: String = "",
    val outOfOfficeSubject: String = "",
    val outOfOfficeFirstDate: String = "",
    val outOfOfficeLastDate: String = ""
)