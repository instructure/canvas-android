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
package com.instructure.pandautils.features.privacysettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.SetCookieConsentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    private val setCookieConsentUseCase: SetCookieConsentUseCase,
    private val analyticsConsentHandler: AnalyticsConsentHandler,
    consentPrefs: ConsentPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PrivacySettingsUiState(
            consentEnabled = consentPrefs.currentUserConsent == true,
            onToggleChanged = ::onToggleChanged,
            onErrorDismissed = ::clearError
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun onToggleChanged(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            try {
                setCookieConsentUseCase(SetCookieConsentUseCase.Params(enabled))
                if (enabled) analyticsConsentHandler.onConsentGranted()
                else analyticsConsentHandler.onConsentRevoked()
                _uiState.update { it.copy(saving = false, consentEnabled = enabled) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saving = false, errorMessage = e.message) }
            }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
