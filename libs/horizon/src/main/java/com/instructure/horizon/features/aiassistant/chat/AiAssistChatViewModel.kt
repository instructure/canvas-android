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
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiAssistChatViewModel @Inject constructor(
    private val repository: AiAssistRepository,
    private val aiAssistContextProvider: AiAssistContextProvider,
): ViewModel() {
    private val _uiState = MutableStateFlow(AiAssistChatUiState(
        onInputTextChanged = ::onTextInputChanged,
        onInputTextSubmitted = ::onTextInputSubmitted,
        onClearChatHistory = ::onClearChatHistory,
        onChipClicked = ::onChipClicked,
        messages = aiAssistContextProvider.aiAssistContext.chatHistory,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        aiAssistContextProvider.aiAssistContext.chatHistory.lastOrNull()?.let { evaluatePrompt(it) }
    }

    private fun onTextInputChanged(newValue: TextFieldValue) {
        _uiState.update {
            it.copy(
                inputTextValue = newValue,
            )
        }
    }

    private fun onTextInputSubmitted() {
        val prompt = _uiState.value.inputTextValue.text
        val message = addMessageToChatHistory(prompt)
        _uiState.update {
            it.copy(
                inputTextValue = TextFieldValue(""),
                messages = it.messages + message,
            )
        }

        evaluatePrompt(message)
    }

    private fun evaluatePrompt(message: JourneyAssistChatMessage) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val response = answerPrompt(message.prompt)
            aiAssistContextProvider.addMessageToChatHistory(response)

            _uiState.update {
                it.copy(
                    messages = it.messages + response,
                    isLoading = false,
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun answerPrompt(prompt: String): JourneyAssistChatMessage {
        val response = repository.answerPrompt(
            prompt,
            aiAssistContextProvider.aiAssistContext.chatHistory,
            aiAssistContextProvider.aiAssistContext.state
        )
        aiAssistContextProvider.updateContextFromState(response.state)
        return response.message
    }

    private fun addMessageToChatHistory(prompt: String): JourneyAssistChatMessage {
        val message = JourneyAssistChatMessage(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            displayText = prompt,
            role = JourneyAssistRole.USER,
        )
        aiAssistContextProvider.addMessageToChatHistory(message)
        return message
    }

    private fun onClearChatHistory() {
        aiAssistContextProvider.aiAssistContext = aiAssistContextProvider.aiAssistContext.copy(
            chatHistory = emptyList()
        )
    }

    private fun onChipClicked(prompt: String) {
        val message = addMessageToChatHistory(prompt)
        _uiState.update {
            it.copy(messages = it.messages + message)
        }
        evaluatePrompt(message)
    }

}