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
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistRole
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.model.toContextSourceList
import com.instructure.horizon.features.aiassistant.common.model.toJourneyAssistChatMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        onNavigateToCards = ::onNavigateToCards,
        messages = aiAssistContextProvider.aiAssistContext.chatHistory,
    ))
    val uiState = _uiState.asStateFlow()

    private var aiAssistContextState = aiAssistContextProvider.aiAssistContext.state
    private var aiAssistMessages = aiAssistContextProvider.aiAssistContext.chatHistory.toMutableList()

    private fun onTextInputChanged(newValue: TextFieldValue) {
        _uiState.update {
            it.copy(
                inputTextValue = newValue,
            )
        }
    }

    private fun onTextInputSubmitted() {
        val prompt = _uiState.value.inputTextValue.text
        val message = addMessage(prompt)
        _uiState.update {
            it.copy(
                inputTextValue = TextFieldValue(""),
                messages = it.messages + message,
            )
        }

        evaluatePrompt(message)
    }

    private fun evaluatePrompt(message: AiAssistMessage) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val response = answerPrompt(message.text)
            aiAssistMessages.add(response)

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

    private suspend fun answerPrompt(prompt: String): AiAssistMessage {
        val response = repository.answerPrompt(
            prompt,
            aiAssistMessages.toJourneyAssistChatMessages(),
            aiAssistContextState
        )
        aiAssistContextState = response.state ?: aiAssistContextState
        return response.message
    }

    private fun addMessage(prompt: String): AiAssistMessage {
        val message = AiAssistMessage(
            text = prompt,
            role = JourneyAssistRole.User,
        )
        aiAssistMessages.add(message)
        return message
    }

    private fun onClearChatHistory() {
        aiAssistContextProvider.aiAssistContext = aiAssistContextProvider.aiAssistContext.copy(
            chatHistory = emptyList()
        )
    }

    private fun onChipClicked(prompt: String) {
        val message = addMessage(prompt)
        _uiState.update {
            it.copy(messages = it.messages.map { it.copy(chipOptions = emptyList()) } + message)
        }
        evaluatePrompt(message)
    }

    private fun onNavigateToCards() {
        aiAssistContextProvider.aiAssistContext = aiAssistContextProvider.aiAssistContext.copy(
            chatHistory = aiAssistMessages,
            contextSources = aiAssistContextState.toContextSourceList(),
        )
        aiAssistMessages = aiAssistMessages.dropLast(1).toMutableList()
        _uiState.update {
            it.copy(messages = aiAssistMessages)
        }
    }

}