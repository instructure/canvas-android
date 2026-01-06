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
package com.instructure.horizon.features.aiassistant.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.AiAssistRepository
import com.instructure.horizon.features.aiassistant.common.model.toJourneyAssistChatMessages
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizAnswerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AiAssistQuizViewModel @Inject constructor(
    private val aiAssistRepository: AiAssistRepository,
    private val aiAssistContextProvider: AiAssistContextProvider
): ViewModel() {

    private val _uiState = MutableStateFlow(
        AiAssistQuizUiState(
            onClearChatHistory = ::onClearChatHistory,
            checkQuiz = ::checkQuiz,
            setSelectedIndex = ::setSelectedIndex,
            regenerateQuiz = ::generateNewQuiz,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadQuizFromContext()
    }

    private fun loadQuizFromContext() {
        val quizItems = aiAssistContextProvider.aiAssistContext.chatHistory.lastOrNull()?.quizItems

        if (quizItems != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    quizList = quizItems.map { quiz ->
                        QuizState(
                            question = quiz.question,
                            answerIndex = quiz.correctAnswerIndex,
                            options = quiz.answers.map { option ->
                                QuizAnswerState(
                                    text = option,
                                    status = AiAssistQuizAnswerStatus.UNSELECTED
                                )
                            },
                            selectedOptionIndex = null,
                            isChecked = false
                        )
                    },
                    currentQuizIndex = 0
                )
            }
        }
    }

    fun generateNewQuiz() {
        val currentIndex = _uiState.value.currentQuizIndex
        val quizList = _uiState.value.quizList

        if (currentIndex < quizList.size - 1) {
            _uiState.update {
                it.copy(currentQuizIndex = currentIndex + 1)
            }
        } else {
            viewModelScope.tryLaunch {
                _uiState.update {
                    it.copy(isLoading = true)
                }

                val response = aiAssistRepository.answerPrompt(
                    prompt = aiAssistContextProvider.aiAssistContext.chatHistory
                        .lastOrNull { it.role == JourneyAssistRole.User }?.text.orEmpty(),
                    history = aiAssistContextProvider.aiAssistContext.chatHistory.toJourneyAssistChatMessages(),
                    state = aiAssistContextProvider.aiAssistContext.state
                )

                aiAssistContextProvider.updateContextFromState(response.state)
                aiAssistContextProvider.addMessageToChatHistory(response.message)

                val quizItems = response.message.quizItems
                val newQuizzes = quizItems.map { quiz ->
                    QuizState(
                        question = quiz.question,
                        answerIndex = quiz.correctAnswerIndex,
                        options = quiz.answers.map { option ->
                            QuizAnswerState(
                                text = option,
                                status = AiAssistQuizAnswerStatus.UNSELECTED
                            )
                        },
                        selectedOptionIndex = null,
                        isChecked = false
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        quizList = it.quizList + newQuizzes,
                        currentQuizIndex = it.quizList.size
                    )
                }
            } catch {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun checkQuiz() {
        val currentIndex = _uiState.value.currentQuizIndex
        val currentQuiz = _uiState.value.quizList.getOrNull(currentIndex) ?: return

        _uiState.update {
            it.copy(
                quizList = it.quizList.mapIndexed { index, quiz ->
                    if (index == currentIndex) {
                        quiz.copy(
                            options = quiz.options.mapIndexed { optionIndex, option ->
                                if (optionIndex == currentQuiz.answerIndex) {
                                    option.copy(status = AiAssistQuizAnswerStatus.CORRECT)
                                } else if (optionIndex == currentQuiz.selectedOptionIndex) {
                                    option.copy(status = AiAssistQuizAnswerStatus.INCORRECT)
                                } else {
                                    option
                                }
                            },
                            isChecked = true
                        )
                    } else {
                        quiz
                    }
                }
            )
        }
    }

    private fun setSelectedIndex(index: Int) {
        val currentIndex = _uiState.value.currentQuizIndex

        _uiState.update {
            it.copy(
                quizList = it.quizList.mapIndexed { quizIndex, quiz ->
                    if (quizIndex == currentIndex) {
                        quiz.copy(
                            selectedOptionIndex = index,
                            options = quiz.options.mapIndexed { optionIndex, option ->
                                if (optionIndex == index) {
                                    option.copy(status = AiAssistQuizAnswerStatus.SELECTED)
                                } else {
                                    option.copy(status = AiAssistQuizAnswerStatus.UNSELECTED)
                                }
                            }
                        )
                    } else {
                        quiz
                    }
                }
            )
        }
    }

    private fun onClearChatHistory() {
        aiAssistContextProvider.aiAssistContext = aiAssistContextProvider.aiAssistContext.copy(
            chatHistory = emptyList()
        )
    }
}