/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.features.ai.quiz

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.instructure.student.features.ai.model.SummaryQuestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class QuizSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val questions = savedStateHandle.get<Array<Parcelable>>(QuizSummaryFragment.QUESTIONS).orEmpty().filterIsInstance<SummaryQuestions>()

    private val _uiState = MutableStateFlow(QuizSummaryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.value = QuizSummaryUiState(
            questions = questions.map {
                QuizSummaryQuestionUiState(
                    question = it.question,
                    answers = it.choices,
                    answer = it.answer,
                    explanation = it.answer,
                    correct = it.answer == it.userAnswer
                )
            }
        )
    }
}