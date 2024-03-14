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

import com.instructure.student.features.ai.model.SummaryQuestions

data class QuizUiState(
    val questions: List<QuizQuestionUiState>
)

data class QuizQuestionUiState(
    val questionId: Int,
    val question: String,
    val answers: List<String>,
    val userAnswer: String? = null,
)

sealed class QuizAction {
    data class AnswerQuestion(val questionId: Int, val answer: String) : QuizAction()
    data object ProgressCompleted : QuizAction()
}

sealed class QuizViewModelAction {
    data class QuizFinished(val questions: List<SummaryQuestions>) : QuizViewModelAction()
}