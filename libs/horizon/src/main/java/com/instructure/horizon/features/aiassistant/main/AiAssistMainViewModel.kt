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
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
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
class AiAssistMainViewModel @Inject constructor(
    private val repository: AiAssistRepository,
    private val aiAssistContextProvider: AiAssistContextProvider
): ViewModel() {

    private val _uiState = MutableStateFlow(
        AiAssistMainUiState(
            sendMessage = ::sendMessage,
            onNavigateToDetails = ::onNavigateToDetails
        )
    )
    val uiState = _uiState.asStateFlow()

    private var aiAssistContextState = aiAssistContextProvider.aiAssistContext.state
    private var aiAssistMessages = aiAssistContextProvider.aiAssistContext.chatHistory.toMutableList()

    init {
        initMessage()
    }

    private fun initMessage() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoading = true) }
            val message = evaluatePrompt()
            aiAssistMessages.add(message)
            _uiState.update {
                it.copy(
                    messages = listOf(message),
                    isLoading = false
                )
            }
        } catch {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun evaluatePrompt(prompt: String = ""): AiAssistMessage {
        val response = repository.answerPrompt(
            prompt,
            aiAssistMessages.toJourneyAssistChatMessages(),
            aiAssistContextState
        )
        aiAssistContextState = response.state ?: aiAssistContextState
        return response.message
    }

    private fun sendMessage(prompt: String) {
        val userMessage = AiAssistMessage(
            text = prompt,
            role = JourneyAssistRole.User,
        )
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true
            )
        }
        aiAssistMessages = aiAssistMessages.map { it.copy(chipOptions = emptyList()) }.toMutableList()
        aiAssistMessages.add(userMessage)

        viewModelScope.tryLaunch {
            val response = evaluatePrompt(userMessage.text)
            aiAssistMessages.add(response)
            _uiState.update {
                it.copy(
                    messages = it.messages + response,
                    isLoading = false
                )
            }
        } catch {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun onNavigateToDetails() {
        _uiState.update {
            it.copy(
                messages = it.messages.take(1),
            )
        }
        aiAssistContextProvider.aiAssistContext = aiAssistContextProvider.aiAssistContext.copy(
            chatHistory = aiAssistMessages,
            contextSources = aiAssistContextState.toContextSourceList()
        )

        aiAssistMessages = uiState.value.messages.toMutableList()
        aiAssistContextState = aiAssistContextProvider.aiAssistContext.state

    }
}