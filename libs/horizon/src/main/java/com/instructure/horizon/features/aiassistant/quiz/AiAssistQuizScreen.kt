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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistResponseTextBlock
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizAnswer
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizAnswerStatus
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizFooter
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Spinner

@Composable
fun AiAssistQuizScreen(
    navController: NavHostController,
    state: AiAssistQuizUiState,
    onDismiss: () -> Unit
) {
    AiAssistScaffold(
        navController = navController,
        onDismiss = { onDismiss() },
    ) { modifier ->
            if (state.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = modifier.fillMaxSize()
                ) {
                    Spinner(
                        color = HorizonColors.Surface.cardPrimary(),
                    )
                }
            } else {
                if (state.quizState != null) {
                    Column(
                        modifier = modifier.verticalScroll(rememberScrollState())
                    ) {
                        AiAssistResponseTextBlock(
                            text = state.quizState.question
                        )

                        HorizonSpace(SpaceSize.SPACE_16)

                        state.quizState.options.forEachIndexed { index, option ->
                            AiAssistQuizAnswer(
                                text = option.text,
                                onClick = { if (!state.isChecked) { state.setSelectedIndex(index) } },
                                status = option.status,
                            )

                            if (index != state.quizState.options.lastIndex) {
                                HorizonSpace(SpaceSize.SPACE_8)
                            }
                        }

                        HorizonSpace(SpaceSize.SPACE_32)

                        AiAssistQuizFooter(
                            checkButtonEnabled = state.quizState.selectedOptionIndex != null && !state.isChecked,
                            onCheckAnswerSelected = { state.checkQuiz() },
                            onRegenerateSelected = { state.regenerateQuiz() },
                        )
                    }
                }
            }
    }
}

@Composable
@Preview
private fun AiAssistQuizScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current

    val state = AiAssistQuizUiState(
        isLoading = true,
        quizState = null,
        isChecked = false,
        setSelectedIndex = {},
        checkQuiz = {},
        regenerateQuiz = {}
    )

    AiAssistQuizScreen(
        navController = NavHostController(context = LocalContext.current),
        state = state,
        onDismiss = {}
    )
}

@Composable
@Preview
private fun AiAssistQuizScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    val state = AiAssistQuizUiState(
        isLoading = false,
        quizState = QuizState(
            question = "What is the capital of France?",
            answerIndex = 1,
            options = listOf(
                QuizAnswerState("Berlin", AiAssistQuizAnswerStatus.UNSELECTED),
                QuizAnswerState("Paris", AiAssistQuizAnswerStatus.CORRECT),
                QuizAnswerState("Madrid", AiAssistQuizAnswerStatus.INCORRECT),
                QuizAnswerState("Rome", AiAssistQuizAnswerStatus.SELECTED)
            ),
            selectedOptionIndex = null
        ),
        isChecked = true,
        setSelectedIndex = {},
        checkQuiz = {},
        regenerateQuiz = {}
    )

    AiAssistQuizScreen(
        navController = NavHostController(context = LocalContext.current),
        state = state,
        onDismiss = {}
    )
}