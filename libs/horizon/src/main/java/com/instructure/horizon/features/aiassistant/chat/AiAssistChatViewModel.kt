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

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessagePrompt
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessageRole
import com.instructure.horizon.features.aiassistant.common.model.toDisplayText
import com.instructure.horizon.features.aiassistant.navigation.AiAssistNavigationTypeMap
import com.instructure.horizon.features.aiassistant.navigation.AiAssistRoute
import com.instructure.pine.type.MessageInput
import com.instructure.pine.type.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AiAssistChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AiAssistChatRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val aiContext = savedStateHandle.toRoute<AiAssistRoute.AiAssistChat>(AiAssistNavigationTypeMap).aiContext

    private val _uiState = MutableStateFlow(AiAssistChatUiState(
        onInputTextChanged = ::onTextInputChanged,
        onInputTextSubmitted = ::onTextInputSubmitted,
        aiContext = aiContext,
        messages = aiContext.chatHistory,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        aiContext.chatHistory.lastOrNull()?.let { message ->
            executeExistingPrompt(message.prompt)
        }
    }

    private fun executeExistingPrompt(prompt: AiAssistMessagePrompt) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isLoading = true,)
            }

            val response = when(prompt) {
                is AiAssistMessagePrompt.Custom -> {
                    answerPrompt(prompt.message)
                }
                is AiAssistMessagePrompt.Summarize -> {
                    repository.summarizePrompt(
                        contextString = uiState.value.aiContext.contextString.orEmpty(),
                    )
                }
                is AiAssistMessagePrompt.TellMeMore -> {
                    tellMeMorePrompt(
                        contextString = uiState.value.aiContext.contextString.orEmpty(),
                    )
                }
                is AiAssistMessagePrompt.KeyTakeAway -> {
                    generateKeyTakeaways(
                        contextString = uiState.value.aiContext.contextString.orEmpty(),
                    )
                }
            }

            _uiState.update {
                it.copy(
                    messages = it.messages + AiAssistMessage(
                        prompt = AiAssistMessagePrompt.Custom(response),
                        role = AiAssistMessageRole.Assistant,
                    ),
                    isLoading = false
                )
            }
        } catch {
            // Error handling
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
        viewModelScope.tryLaunch {
            val prompt = _uiState.value.inputTextValue.text
            _uiState.update {
                it.copy(
                    inputTextValue = TextFieldValue(""),
                    messages = it.messages + AiAssistMessage(
                        prompt = AiAssistMessagePrompt.Custom(it.inputTextValue.text),
                        role = AiAssistMessageRole.User,
                    ),
                    isLoading = true,
                )
            }

            val response = answerPrompt(prompt)

            _uiState.update {
                it.copy(
                    messages = it.messages + AiAssistMessage(
                        prompt = AiAssistMessagePrompt.Custom(response),
                        role = AiAssistMessageRole.Assistant,
                    ),
                    isLoading = false,
                )
            }
        } catch {
            // Error handling
        }
    }

    private suspend fun answerPrompt(prompt: String): String {
        return if (uiState.value.aiContext.contextSources.isNotEmpty()) {
            repository.answerPrompt(
                messages = uiState.value.messages.map {
                    MessageInput(
                        role = if (it.role is AiAssistMessageRole.User) {
                            Role.User
                        } else {
                            Role.Assistant
                        },
                        text = it.prompt.toDisplayText(context)
                    )
                },
                context = uiState.value.aiContext.contextSources
            )
        } else {
            repository.answerPrompt(prompt, uiState.value.aiContext.contextString)
        }
    }

    private suspend fun tellMeMorePrompt(contextString: String): String {
        return repository.answerPrompt(
            prompt = "In 1-2 paragraphs, tell me more about this content.",
            contextString = contextString
        )
    }

    private suspend fun generateKeyTakeaways(contextString: String): String {
        return repository.answerPrompt(
            prompt = "Give key takeaways from this content in 3 bullet points; don't use any information besides the provided content.",
            contextString = contextString
        )
    }
}