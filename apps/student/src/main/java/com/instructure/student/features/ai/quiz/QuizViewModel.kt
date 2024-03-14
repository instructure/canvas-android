/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.features.ai.quiz

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.student.features.ai.model.SummaryQuestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val questions = savedStateHandle.get<Array<Parcelable>>(QuizFragment.QUESTIONS).orEmpty().filterIsInstance<SummaryQuestions>().toMutableList()

    private val _uiState = MutableStateFlow(QuizUiState(emptyList()))
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<QuizViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        createNewUiState()
    }

    fun handleAction(action: QuizAction) {
        when (action) {
            is QuizAction.AnswerQuestion -> {
                answerQuestion(action)
            }

            QuizAction.ProgressCompleted -> {
                viewModelScope.launch {
                    _events.send(QuizViewModelAction.QuizFinished(questions))
                }
            }
        }
    }

    private fun answerQuestion(action: QuizAction.AnswerQuestion) {
        val question = questions[action.questionId]
        questions[action.questionId] = question.copy(userAnswer = action.answer)
        createNewUiState()
    }

    private fun createNewUiState() {
        _uiState.value = QuizUiState(
            questions = questions.mapIndexed { index, question ->
                QuizQuestionUiState(
                    questionId = index,
                    question = question.question,
                    answers = question.choices,
                    userAnswer = question.userAnswer
                )
            }
        )
    }
}