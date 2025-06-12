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

import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizAnswerStatus

data class AiAssistQuizUiState(
    val isLoading: Boolean = false,
    val aiContext: AiAssistContext = AiAssistContext(),
    val quizState: QuizState? = null,
    val isChecked: Boolean = false,
    val checkQuiz: () -> Unit = {},
    val regenerateQuiz: () -> Unit = {},
    val setSelectedIndex: (Int) -> Unit = {},
)

data class QuizState(
    val question: String,
    val answerIndex: Int,
    val options: List<QuizAnswerState>,
    val selectedOptionIndex: Int? = null,
)

data class QuizAnswerState(
    val text: String,
    val status: AiAssistQuizAnswerStatus
)