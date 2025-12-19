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
package com.instructure.horizon.features.aiassistant.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AiAssistFlashcardViewModel @Inject constructor(
    private val aiAssistRepository: AiAssistRepository,
    private val aiAssistContextProvider: AiAssistContextProvider
): ViewModel() {
    private val _uiState = MutableStateFlow(AiAssistFlashcardUiState(
        onFlashcardClicked = ::onFlashcardClicked,
        updateCurrentCardIndex = ::updateCurrentCardIndex,
        onClearChatHistory = ::onClearChatHistory,
        regenerateFlashcards = ::generateNewFlashcards
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadFlashcardsFromContext()
    }

    private fun loadFlashcardsFromContext() {
        val flashcards = aiAssistContextProvider.aiAssistContext.chatHistory.lastOrNull()?.flashCards

        if (flashcards != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    flashcardList = flashcards.map { card ->
                        FlashcardState(
                            question = card.question,
                            answer = card.answer,
                            isFlippedToAnswer = false
                        )
                    },
                )
            }
        }
    }

    private fun generateNewFlashcards() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val response = aiAssistRepository.answerPrompt(
                prompt = aiAssistContextProvider.aiAssistContext.chatHistory
                    .lastOrNull { it.role == JourneyAssistRole.User }?.prompt.orEmpty(),
                history = aiAssistContextProvider.aiAssistContext.chatHistory,
                state = aiAssistContextProvider.aiAssistContext.state
            )

            aiAssistContextProvider.updateContextFromState(response.state)
            aiAssistContextProvider.addMessageToChatHistory(response.message)

            loadFlashcardsFromContext()
        } catch {
            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun onFlashcardClicked(flashcard: FlashcardState) {
        _uiState.update { currentState ->
            val updatedFlashcards = currentState.flashcardList.map {
                if (it == flashcard) {
                    it.copy(isFlippedToAnswer = !it.isFlippedToAnswer)
                } else {
                    it
                }
            }
            currentState.copy(flashcardList = updatedFlashcards)
        }
    }

    private fun updateCurrentCardIndex(index: Int) {
        _uiState.update {
            it.copy(currentCardIndex = index)
        }
    }

    private fun onClearChatHistory() {
        aiAssistContextProvider.aiAssistContext = aiAssistContextProvider.aiAssistContext.copy(
            chatHistory = emptyList()
        )
    }

}