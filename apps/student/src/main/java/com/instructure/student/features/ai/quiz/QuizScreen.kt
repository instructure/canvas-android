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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    actionHandler: (QuizAction) -> Unit,
    courseColor: Int
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = Color(courseColor)
        ) { padding ->
            val unansweredQuestions = uiState.questions.filter { it.userAnswer == null }
            val currentQuestion = unansweredQuestions.lastOrNull()
            if (unansweredQuestions.isNotEmpty()) {
                Column {
                    Box(Modifier.padding(padding)) {
                        unansweredQuestions.forEach {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .height(500.dp),
                                backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
                                elevation = 4.dp
                            ) {
                                Text(
                                    text = it.question,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxSize(),
                                    color = colorResource(id = R.color.textDarkest),
                                    fontSize = 36.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        Card(
                            modifier = Modifier
                                .padding(24.dp)
                                .width(150.dp)
                                .clickable {
                                    actionHandler(
                                        QuizAction.AnswerQuestion(
                                            currentQuestion!!.questionId,
                                            currentQuestion.answers.first()
                                        )
                                    )
                                },
                            backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
                            elevation = 4.dp
                        ) {
                            Text(
                                text = currentQuestion!!.answers.first(),
                                modifier = Modifier.padding(16.dp),
                                color = colorResource(id = R.color.textDarkest),
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Card(
                            modifier = Modifier
                                .padding(24.dp)
                                .width(150.dp)
                                .clickable {
                                    actionHandler(
                                        QuizAction.AnswerQuestion(
                                            currentQuestion!!.questionId,
                                            currentQuestion.answers.last()
                                        )
                                    )
                                },
                            backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
                            elevation = 4.dp
                        ) {
                            Text(
                                text = currentQuestion!!.answers.last(),
                                modifier = Modifier.padding(16.dp),
                                color = colorResource(id = R.color.textDarkest),
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun QuizScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    QuizScreen(
        uiState = QuizUiState(
            questions = listOf(
                QuizQuestionUiState(
                    questionId = 1,
                    question = "What is the capital of France?",
                    answers = listOf("Paris", "London"),
//                    userAnswer = "Paris"
                ),
                QuizQuestionUiState(
                    questionId = 2,
                    question = "What is the capital of Germany?",
                    answers = listOf("Berlin", "Madrid"),
//                    userAnswer = "Berlin"
                ),
                QuizQuestionUiState(
                    questionId = 3,
                    question = "What is the capital of Italy?",
                    answers = listOf("Rome", "Athens"),
                    userAnswer = "Rome"
                ),
                QuizQuestionUiState(
                    questionId = 4,
                    question = "What is the capital of Spain?",
                    answers = listOf("Madrid", "Lisbon"),
                    userAnswer = "Madrid"
                )
            )
        ), {}, ThemePrefs.primaryColor
    )
}