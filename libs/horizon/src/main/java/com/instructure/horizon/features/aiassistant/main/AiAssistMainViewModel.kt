/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.aiassistant.main

import androidx.lifecycle.ViewModel
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AiAssistMainViewModel @Inject constructor(
    private val aiAssistContextProvider: AiAssistContextProvider
): ViewModel() {
    private val originalAiAssistContext = aiAssistContextProvider.aiAssistContext

    private val _uiState = MutableStateFlow(
        AiAssistMainUiState(
            isAiContextEmpty = originalAiAssistContext.isEmpty(),
            onSetAiAssistContextMessage = ::updateAiContextMessage
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun updateAiContextMessage(message: AiAssistMessage) {
        aiAssistContextProvider.aiAssistContext = aiAssistContextProvider.aiAssistContext.copy(
            chatHistory = listOf(message)
        )
    }
}