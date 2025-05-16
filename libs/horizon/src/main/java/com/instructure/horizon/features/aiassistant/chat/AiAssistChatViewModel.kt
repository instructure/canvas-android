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
package com.instructure.horizon.features.aiassistant.chat

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiAssistChatViewModel @Inject constructor(
    private val repository: AiAssistChatRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(AiAssistChatUiState(
        onInputTextChanged = ::onTextInputChanged,
        onInputTextSubmitted = ::onTextInputSubmitted,
    ))
    val uiState = _uiState.asStateFlow()

    fun updateContext(aiContext: AiAssistContext) {
        _uiState.update {
            it.copy(
                aiContext = aiContext,
            )
        }
    }

    private fun onTextInputChanged(newValue: TextFieldValue) {
        _uiState.update {
            it.copy(
                inputTextValue = newValue,
            )
        }
    }

    private fun onTextInputSubmitted() {
        viewModelScope.launch {
            val prompt = _uiState.value.inputTextValue.text
            _uiState.update {
                it.copy(
                    inputTextValue = TextFieldValue(""),
                    messages = it.messages + AiAssistMessage(
                        message = it.inputTextValue.text,
                        role = AiAssistMessageRole.User,
                    ),
                    isLoading = true,
                )
            }

            val response = answerPrompt(prompt)

            _uiState.update {
                it.copy(
                    messages = it.messages + AiAssistMessage(
                        message = response,
                        role = AiAssistMessageRole.Assistant,
                    ),
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun answerPrompt(prompt: String): String {
        return if (uiState.value.aiContext.contextSources.isNotEmpty()) {
            repository.answerPrompt(
                messages = uiState.value.messages,
                context = uiState.value.aiContext.contextSources
            )
        } else {
            repository.answerPrompt(prompt, uiState.value.aiContext.contextString)
        }
    }
}