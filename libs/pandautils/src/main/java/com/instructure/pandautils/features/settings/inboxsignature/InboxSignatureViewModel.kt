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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxSignatureViewModel @Inject constructor(
    private val repository: InboxSignatureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxSignatureUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<InboxSignatureViewModelAction>()
    val events = _events.receiveAsFlow()

    // We need to store, because the API doesn't allow updating only the signature,
    // so we have to store the out of office settings and send back the same settings so it wouldn't be overwritten.
    private var inboxSignatureSettings: InboxSignatureSettings? = null

    init {
        getInboxSignature()
    }

    private fun getInboxSignature() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            val signatureResult = repository.getInboxSignature()
            _uiState.update { it.copy(loading = false)}
            if (signatureResult.isSuccess) {
                val result = signatureResult.dataOrThrow
                inboxSignatureSettings = result
                _uiState.update {
                    it.copy(signatureText = TextFieldValue(result.signature), signatureEnabled = result.useSignature)
                }
            } else {
                _uiState.update { it.copy(error = true) }
            }
        }
    }

    fun handleAction(action: InboxSignatureAction) {
        when (action) {
            is InboxSignatureAction.Save -> {
                saveInboxSignature()
            }

            is InboxSignatureAction.UpdateSignature -> _uiState.update {
                it.copy(
                    signatureText = action.signature,
                    saveEnabled = isSaveEnabled(action.signature.text, it.signatureEnabled)
                )
            }
            is InboxSignatureAction.UpdateSignatureEnabled -> _uiState.update {
                it.copy(
                    signatureEnabled = action.enabled,
                    saveEnabled = isSaveEnabled(it.signatureText.text, action.enabled)
                )
            }

            InboxSignatureAction.Refresh -> getInboxSignature()
        }
    }

    private fun saveInboxSignature() {
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            val result = repository.updateInboxSignature(
                inboxSignatureSettings?.copy(
                    signature = _uiState.value.signatureText.text,
                    useSignature = _uiState.value.signatureEnabled
                ) ?: InboxSignatureSettings(_uiState.value.signatureText.text, _uiState.value.signatureEnabled)
            )
            _uiState.update { it.copy(saving = false) }
            if (result.isSuccess) {
                _events.send(InboxSignatureViewModelAction.CloseAndUpdateSettings(result.dataOrNull?.useSignature ?: false))
            } else {
                _events.send(InboxSignatureViewModelAction.ShowErrorToast)
            }
        }
    }

    private fun isSaveEnabled(signatureText: String, signatureEnabled: Boolean): Boolean {
        return signatureText.isNotBlank() || !signatureEnabled
    }
}