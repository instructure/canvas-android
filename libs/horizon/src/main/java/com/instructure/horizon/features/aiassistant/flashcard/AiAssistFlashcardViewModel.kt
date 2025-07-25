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
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AiAssistFlashcardViewModel @Inject constructor(
    private val repository: AiAssistFlashcardRepository,
    aiAssistContextProvider: AiAssistContextProvider
): ViewModel() {
    private val aiContext = aiAssistContextProvider.aiAssistContext

    private val _uiState = MutableStateFlow(AiAssistFlashcardUiState(
        onFlashcardClicked = ::onFlashcardClicked,
        updateCurrentCardIndex = ::updateCurrentCardIndex,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        generateNewFlashcards()
    }

    private fun generateNewFlashcards() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val flashcards = repository.generateFlashcards(aiContext.contextString.orEmpty())

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
        } catch {
            // Handle error
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

}