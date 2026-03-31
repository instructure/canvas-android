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
package com.instructure.pandautils.features.cookieconsent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CookieConsentViewModel @Inject constructor(
    private val getCookieConsentUseCase: GetCookieConsentUseCase,
    private val setCookieConsentUseCase: SetCookieConsentUseCase,
    private val namespace: CookieConsentNamespace
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CookieConsentUiState(
            namespace = namespace,
            onAllow = { submitConsent(true) },
            onDecline = { submitConsent(false) },
            onErrorDismissed = ::clearError,
            onConsentResultHandled = ::clearConsentResult
        )
    )
    val uiState = _uiState.asStateFlow()

    fun checkAndShowIfNeeded() {
        viewModelScope.launch {
            try {
                val result = getCookieConsentUseCase(GetCookieConsentUseCase.Params(namespace))
                if (result.flagEnabled && result.consent == null) {
                    _uiState.update { it.copy(loading = false, showDialog = true) }
                } else {
                    _uiState.update {
                        it.copy(loading = false, consentResult = ConsentResult(
                            consentGiven = result.consent ?: false,
                            needed = false
                        ))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, consentResult = ConsentResult(consentGiven = false, needed = false))
                }
            }
        }
    }

    fun showFromSettings() {
        _uiState.update { it.copy(loading = false, showDialog = true) }
    }

    private fun submitConsent(consent: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            try {
                setCookieConsentUseCase(SetCookieConsentUseCase.Params(namespace, consent))
                _uiState.update {
                    it.copy(
                        showDialog = false,
                        saving = false,
                        consentResult = ConsentResult(consentGiven = consent, needed = true)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        saving = false,
                        errorMessage = e.message ?: "Failed to save consent"
                    )
                }
            }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun clearConsentResult() {
        _uiState.update { it.copy(consentResult = null) }
    }
}