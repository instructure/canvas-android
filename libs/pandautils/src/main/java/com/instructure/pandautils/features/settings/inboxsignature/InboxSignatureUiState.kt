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

import androidx.compose.ui.text.input.TextFieldValue

data class InboxSignatureUiState(
    val signatureEnabled: Boolean = true,
    val signatureText: TextFieldValue = TextFieldValue(""),
    val saving: Boolean = false,
    val saveEnabled: Boolean = true,
    val loading: Boolean = false,
    val error: Boolean = false
)

sealed class InboxSignatureAction {
    data object Save : InboxSignatureAction()
    data class UpdateSignature(val signature: TextFieldValue) : InboxSignatureAction()
    data class UpdateSignatureEnabled(val enabled: Boolean) : InboxSignatureAction()
    data object Refresh : InboxSignatureAction()
}

sealed class InboxSignatureViewModelAction {
    data class CloseAndUpdateSettings(val enabled: Boolean) : InboxSignatureViewModelAction()
}