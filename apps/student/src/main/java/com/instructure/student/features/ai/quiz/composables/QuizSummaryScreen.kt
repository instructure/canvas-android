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

package com.instructure.student.features.ai.quiz.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.student.features.ai.quiz.QuizSummaryQuestionUiState
import com.instructure.student.features.ai.quiz.QuizSummaryUiState


@Composable
fun QuizSummaryScreen(
    uiState: QuizSummaryUiState
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest)
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                item {
                    Text(
                        text = "Result: ${uiState.questions.size} / ${uiState.questions.count { it.correct }}",
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 24.sp,
                        textAlign = Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                items(uiState.questions) {
                    QuizSummaryItem(
                        uiState = it
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizSummaryItem(
    uiState: QuizSummaryQuestionUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                color = colorResource(id = R.color.backgroundMedium),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.background(colorResource(id = R.color.backgroundMedium))
        ) {
            Text(
                text = uiState.question,
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.answers.first(),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                textAlign = Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorResource(getAnswerColor(uiState.answers.first() == uiState.answer, uiState.correct)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.answers.last(),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                textAlign = Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorResource(getAnswerColor(uiState.answers.last() == uiState.answer, uiState.correct)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Explanation",
                color = colorResource(id = R.color.textDark),
                fontSize = 16.sp
            )
            Text(
                text = uiState.explanation,
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
        }
    }
}

private fun getAnswerColor(correct: Boolean, correctByUser: Boolean): Int {
    return if (correct) {
        R.color.greenAnnotation
    } else {
        if (correctByUser) {
            R.color.backgroundLightestElevated
        } else {
            R.color.redAnnotation
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizSummaryScreenPreview() {
    QuizSummaryScreen(
        uiState = QuizSummaryUiState(
            questions = listOf(
                QuizSummaryQuestionUiState(
                    question = "Which event marked the beginning of the French Revolution?",
                    answers = listOf("Convocation of the Estates General in 1789", "The Storming of the Bastille on 14 July 1789"),
                    answer = "Convocation of the Estates General in 1789",
                    explanation = "The convocation of the Estates General in 1789 marked the beginning of the French Revolution.",
                    correct = true
                ),
                QuizSummaryQuestionUiState(
                    question = "Which event marked the beginning of the French Revolution?",
                    answers = listOf("Convocation of the Estates General in 1789", "The Storming of the Bastille on 14 July 1789"),
                    answer = "The Storming of the Bastille on 14 July 1789",
                    explanation = "The convocation of the Estates General in 1789 marked the beginning of the French Revolution.",
                    correct = false
                ),
            )
        )
    )
}