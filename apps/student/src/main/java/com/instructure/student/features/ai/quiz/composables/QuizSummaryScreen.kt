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

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.features.ai.quiz.QuizSummaryQuestionUiState
import com.instructure.student.features.ai.quiz.QuizSummaryUiState


@Composable
fun QuizSummaryScreen(
    @ColorInt backgroundColor: Int,
    uiState: QuizSummaryUiState,
    onBackClicked: () -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = Color(backgroundColor),
            topBar = {
                TopAppBar(onBackClicked = onBackClicked)
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                item {
                    Text(
                        text = "Result: ${uiState.questions.count { it.correct }} / ${uiState.questions.size}",
                        color = Color(ThemePrefs.primaryTextColor),
                        fontSize = 24.sp,
                        textAlign = Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
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
fun QuizSummaryContent(uiState: QuizSummaryUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        items(uiState.questions, key = { it.hashCode() }) {
            QuizSummaryItem(
                uiState = it
            )
        }
    }
}

@Composable
private fun QuizSummaryItem(
    uiState: QuizSummaryQuestionUiState,
    modifier: Modifier = Modifier
) {
    Card(
        backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.backgroundLightestElevated))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = if (uiState.correct) R.drawable.ic_check_white_24dp else R.drawable.ic_close),
                    contentDescription = null,
                    modifier = Modifier
                        .border(
                            1.5.dp,
                            colorResource(id = if (uiState.correct) R.color.borderSuccess else R.color.borderDanger),
                            CircleShape
                        )
                        .padding(3.dp),
                    tint = colorResource(id = if (uiState.correct) R.color.borderSuccess else R.color.borderDanger)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.question,
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.answers.first(),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                textAlign = Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        shape = RoundedCornerShape(8.dp),
                        color = colorResource(getAnswerColor(uiState.answers.first() == uiState.answer, uiState.correct))
                    )
                    .background(
                        color = colorResource(R.color.backgroundLightestElevated),
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
                    .border(
                        width = 2.dp,
                        shape = RoundedCornerShape(8.dp),
                        color = colorResource(getAnswerColor(uiState.answers.last() == uiState.answer, uiState.correct))
                    )
                    .background(
                        color = colorResource(R.color.backgroundLightestElevated),
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
        R.color.borderSuccess
    } else {
        if (correctByUser) {
            R.color.borderDark
        } else {
            R.color.borderDanger
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizSummaryScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    QuizSummaryScreen(
        backgroundColor = android.graphics.Color.YELLOW,
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
                    answer = "Convocation of the Estates General in 1789",
                    explanation = "The convocation of the Estates General in 1789 marked the beginning of the French Revolution.",
                    correct = false
                ),
            )
        ),
        onBackClicked = {}
    )
}