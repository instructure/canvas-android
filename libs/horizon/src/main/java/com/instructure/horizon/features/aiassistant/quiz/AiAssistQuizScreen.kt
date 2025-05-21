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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistResponseTextBlock
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.features.aiassistant.quiz.composable.AiAssistQuizAnswer
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            if (state.isLoading) {
                Spinner(
                    color = HorizonColors.Surface.cardPrimary(),
                )
            } else {
                if (state.quizState != null) {
                    Column {
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
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}