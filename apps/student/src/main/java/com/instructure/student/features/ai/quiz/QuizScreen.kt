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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.toDp
import com.instructure.student.features.ai.quiz.composables.QuizSummaryContent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sign

@Composable
fun QuizScreen(
    uiState: QuizScreenUiState,
    actionHandler: (QuizAction) -> Unit,
    backgroundColor: Int,
    closeClicked: () -> Unit,
    showConfetti: () -> Unit
) {
    CanvasTheme {
        val listState = rememberLazyListState()
        Scaffold(
            backgroundColor = Color(backgroundColor),
            topBar = {
                val progressRatio = uiState.quizUiState.questions.filter { it.userAnswer != null }.size.toFloat() / uiState.quizUiState.questions.size.toFloat()
                val progress by animateFloatAsState(targetValue = progressRatio, finishedListener = {
                    if (it == 1f) {
                        actionHandler(QuizAction.ProgressCompleted)
                    }
                })
                val successRatio by animateFloatAsState(targetValue = uiState.quizSummaryUiState?.correctRatio ?: 0f, animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing))
                if (successRatio >= 0.8f) {
                    showConfetti()
                }
                val scaleModifier = if (listState.firstVisibleItemIndex > 0) 0f else {
                    1 - (listState.firstVisibleItemScrollOffset.toDp / 48f).coerceAtLeast(0.0f).coerceAtMost(1.0f)
                }
                TopAppBar(
                    onBackClicked = { closeClicked() },
                    progress = progress,
                    successRatio = successRatio,
                    scaleModifier = scaleModifier
                )
            }
        ) { padding ->
            AnimatedContent(targetState = uiState.quizSummaryUiState, transitionSpec = {
                slideInVertically(animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)) { fullHeight -> fullHeight }.togetherWith(fadeOut()).using(SizeTransform(clip = false))
            } ) {
                if (it == null) {
                    QuizContent(uiState = uiState.quizUiState, actionHandler = actionHandler, modifier = Modifier.padding(padding))
                } else {
                    QuizSummaryContent(uiState = it, modifier = Modifier.padding(padding), listState)
                }
            }
        }
    }
}

@Composable
fun TopAppBar(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    successRatio: Float = 0f,
    scaleModifier: Float = 1f
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier
        .padding(start = 8.dp, top = 8.dp)
        .height(48.dp * scaleModifier)
        .scale(scaleX = 1f, scaleY = scaleModifier)
        .alpha(scaleModifier)) {
        IconButton(onClick = { onBackClicked() }) {
            Icon(
                painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.a11y_closeProgress),
                tint = colorResource(id = R.color.white)
            )
        }
        Box(Modifier.padding(end = 16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.white),
                        shape = RoundedCornerShape(100.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(12.dp)
                    .background(colorResource(id = R.color.white), shape = RoundedCornerShape(100.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(successRatio)
                    .height(12.dp)
                    .background(colorResource(id = R.color.backgroundSuccess), shape = RoundedCornerShape(100.dp))
            )
        }
    }
}

@Composable
fun QuizContent(
    uiState: QuizUiState,
    actionHandler: (QuizAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val unansweredQuestions = uiState.questions.filter { it.userAnswer == null }
    val currentQuestion = unansweredQuestions.lastOrNull()

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }

    if (unansweredQuestions.isNotEmpty()) {
        Column(modifier = modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
            ) {
                unansweredQuestions.forEach {
                    var cardModifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                    if (it == currentQuestion) {
                        cardModifier = cardModifier
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
                        modifier = cardModifier,
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

//@Composable
//@Preview
//fun QuizScreenPreview() {
//    ContextKeeper.appContext = LocalContext.current
//    QuizScreen(
//        uiState = QuizUiState(
//            questions = listOf(
//                QuizQuestionUiState(
//                    questionId = 1,
//                    question = "What is the capital of France?",
//                    answers = listOf("Paris", "London"),
//                ),
//                QuizQuestionUiState(
//                    questionId = 2,
//                    question = "What is the capital of Germany?",
//                    answers = listOf("Berlin", "Madrid"),
//                ),
//                QuizQuestionUiState(
//                    questionId = 3,
//                    question = "What is the capital of Italy?",
//                    answers = listOf("Rome", "Athens"),
//                    userAnswer = "Rome"
//                ),
//                QuizQuestionUiState(
//                    questionId = 4,
//                    question = "What is the capital of Spain?",
//                    answers = listOf("Madrid", "Lisbon"),
//                    userAnswer = "Madrid"
//                )
//            )
//        ), {}, ThemePrefs.primaryColor, {}
//    )
//}