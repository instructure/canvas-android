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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.instructure.horizon.features.aiassistant.navigation.AiAssistNavigationTypeMap
import com.instructure.horizon.features.aiassistant.navigation.AiAssistRoute
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizAnswerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiAssistQuizViewModel @Inject constructor(
    private val repository: AiAssistQuizRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val aiContext = savedStateHandle.toRoute<AiAssistRoute.AiAssistQuiz>(
        AiAssistNavigationTypeMap
    ).aiContext

    private val _uiState = MutableStateFlow(
        AiAssistQuizUiState(
            aiContext = aiContext,
            checkQuiz = ::checkQuiz,
            setSelectedIndex = ::setSelectedIndex,
            regenerateQuiz = ::generateNewQuiz,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        generateNewQuiz()
    }

    fun generateNewQuiz() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val quiz = repository.generateQuiz(
                contextString = aiContext.contextString.orEmpty()
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    quizState = QuizState(
                        question = quiz.question,
                        answerIndex = quiz.result,
                        options = quiz.options.mapIndexed { index, option ->
                            QuizAnswerState(
                                text = option,
                                status = AiAssistQuizAnswerStatus.UNSELECTED
                            )
                        },
                        selectedOptionIndex = null
                    ),
                    isChecked = false
                )
            }
        }
    }

    private fun checkQuiz() {
        val selectedOptionIndex = _uiState.value.quizState?.selectedOptionIndex
        val answerOptionIndex = _uiState.value.quizState?.answerIndex

        _uiState.update {
            it.copy(
                quizState = it.quizState?.copy(
                    options = it.quizState.options.mapIndexed { index, option ->
                        if (index == answerOptionIndex) {
                            option.copy(status = AiAssistQuizAnswerStatus.CORRECT)
                        } else if (index == selectedOptionIndex) {
                            option.copy(status = AiAssistQuizAnswerStatus.INCORRECT)
                        } else {
                            option//.copy(status = AiAssistQuizAnswerStatus.INCORRECT)
                        }
                    }
                ),
                isChecked = true,
            )
        }
    }

    private fun setSelectedIndex(index: Int) {
        _uiState.update {
            it.copy(
                quizState = it.quizState?.copy(
                    selectedOptionIndex = index,
                    options = it.quizState.options.mapIndexed { i, option ->
                        if (i == index) {
                            option.copy(status = AiAssistQuizAnswerStatus.SELECTED)
                        } else {
                            option.copy(status = AiAssistQuizAnswerStatus.UNSELECTED)
                        }
                    }
                ),
            )
        }
    }
}