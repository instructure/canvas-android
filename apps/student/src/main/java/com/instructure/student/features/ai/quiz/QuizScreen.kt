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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.ThemePrefs
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sign

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    actionHandler: (QuizAction) -> Unit,
    backgroundColor: Int,
    closeClicked: () -> Unit
) {
    val scope = rememberCoroutineScope()

    CanvasTheme {
        Scaffold(
            backgroundColor = Color(backgroundColor),
            topBar = {
                TopAppBar(
                    onBackClicked = { closeClicked() }
                )
            }
        ) { padding ->
            val unansweredQuestions = uiState.questions.filter { it.userAnswer == null }
            val currentQuestion = unansweredQuestions.lastOrNull()

            val screenWidth = LocalConfiguration.current.screenWidthDp
            val offsetX = remember { Animatable(0f) }
            val offsetY = remember { Animatable(0f) }
            var dragging by remember { mutableStateOf(false) }

            if (unansweredQuestions.isNotEmpty()) {
                Column {
                    Box(
                        Modifier
                            .padding(padding)
                            .weight(1f)
                    ) {
                        unansweredQuestions.forEach {
                            var modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                            if (it == currentQuestion) {
                                modifier = modifier
                                    .pointerInput(Unit) {
                                        detectDragGestures(onDragStart = {
                                            dragging = true
                                        },
                                            onDragEnd = {
                                                dragging = false
                                                val targetOffset = if (offsetX.value.absoluteValue > screenWidth / 3) {
                                                    offsetX.value.sign * screenWidth * 1.2f // Add some extra room because of rotation
                                                } else {
                                                    0f
                                                }
                                                scope.launch {
                                                    async {
                                                        offsetX.animateTo(targetOffset)
                                                        if (targetOffset.sign == -1.0f) {
                                                            actionHandler(
                                                                QuizAction.AnswerQuestion(
                                                                    currentQuestion.questionId,
                                                                    currentQuestion.answers.first()
                                                                )
                                                            )
                                                            offsetX.snapTo(0f)
                                                        } else if (targetOffset.sign == 1.0f) {
                                                            actionHandler(
                                                                QuizAction.AnswerQuestion(
                                                                    currentQuestion.questionId,
                                                                    currentQuestion.answers.last()
                                                                )
                                                            )
                                                            offsetX.snapTo(0f)
                                                            offsetY.snapTo(0f)
                                                        }
                                                    }
                                                    async {
                                                        val newY = if (targetOffset == 0f) 0f else offsetY.value * 2
                                                        offsetY.animateTo(newY)
                                                        offsetY.snapTo(0f)
                                                    }
                                                }
                                            }) { change, dragAmount ->
                                            change.consume()
                                            scope.launch {
                                                offsetX.snapTo(offsetX.value + dragAmount.x.toDp().value)
                                                offsetY.snapTo(offsetY.value + dragAmount.y.toDp().value)
                                            }
                                        }
                                    }
                                    .offset(
                                        offsetX.value.dp,
                                        offsetY.value.dp
                                    )
                                    .rotate((offsetX.value / screenWidth) * 15)
                            }
                            Card(
                                modifier = modifier,
                                backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
                                elevation = 4.dp
                            ) {
                                Text(
                                    text = it.question,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxSize()
                                        .wrapContentHeight(align = Alignment.CenterVertically),
                                    color = colorResource(id = R.color.textDarkest),
                                    fontSize = 28.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.height(IntrinsicSize.Max)) {
                        val selectionRatio = (offsetX.value.absoluteValue / (screenWidth)).coerceAtLeast(0.0f).coerceAtMost(1.0f)
                        QuizAnswerCard(
                            questionId = currentQuestion!!.questionId,
                            answer = currentQuestion.answers.first(),
                            actionHandler = actionHandler,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                                .weight(0.5f),
                            selectionRatio = if (offsetX.value < 0) selectionRatio / 6 else 0f
                        )
                        QuizAnswerCard(
                            questionId = currentQuestion.questionId,
                            answer = currentQuestion.answers.last(),
                            actionHandler = actionHandler,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                                .weight(0.5f),
                            selectionRatio = if (offsetX.value > 0) selectionRatio / 6 else 0f
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit
) {
    Column(modifier = modifier.padding(start = 8.dp, top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackClicked() }) {
                Icon(
                    painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.a11y_closeProgress),
                    tint = colorResource(id = R.color.white)
                )
            }

        }
    }
}

@Composable
fun QuizAnswerCard(
    questionId: Int,
    answer: String,
    actionHandler: (QuizAction) -> Unit,
    modifier: Modifier = Modifier,
    selectionRatio: Float = 0.5f
) {
    Card(
        modifier = modifier
            .scale(selectionRatio + 1)
            .clickable {
                actionHandler(
                    QuizAction.AnswerQuestion(
                        questionId,
                        answer
                    )
                )
            },
        backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = SolidColor(colorResource(id = R.color.backgroundDark)), alpha = selectionRatio),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = answer,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .heightIn(min = 80.dp)
                    .fillMaxHeight()
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 3
            )
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
                ),
                QuizQuestionUiState(
                    questionId = 2,
                    question = "What is the capital of Germany?",
                    answers = listOf("Berlin", "Madrid"),
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
        ), {}, ThemePrefs.primaryColor, {}
    )
}