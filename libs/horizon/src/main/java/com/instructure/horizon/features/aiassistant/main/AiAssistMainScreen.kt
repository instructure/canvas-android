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
package com.instructure.horizon.features.aiassistant.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistResponseTextBlock
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistSuggestionTextBlock
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessagePrompt
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessageRole
import com.instructure.horizon.features.aiassistant.navigation.AiAssistRoute

@Composable
fun AiAssistMainScreen(
    navController: NavHostController,
    state: AiAssistMainUiState,
    onDismiss: () -> Unit,
) {
    var promptInput by remember { mutableStateOf(TextFieldValue("")) }
    AiAssistScaffold(
        navController = navController,
        onDismiss = { onDismiss() },
        inputTextValue = promptInput,
        onInputTextChanged = { promptInput = it },
        onInputTextSubmitted = {
            state.onSetAiAssistContextMessage(
                AiAssistMessage(
                    role = AiAssistMessageRole.User,
                    prompt = AiAssistMessagePrompt.Custom(promptInput.text)
                )
            )
            navController.navigate(AiAssistRoute.AiAssistChat.route)
       }
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            if (state.isAiContextEmpty) {
                item {
                    AiAssistResponseTextBlock(
                        text = stringResource(R.string.ai_HowCanIHelpYou)
                    )
                }
            } else {
                item {
                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_QuizMe),
                        onClick = {
                            navController.navigate(AiAssistRoute.AiAssistQuiz.route)
                        }
                    )
                }
                item {
                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_summarize),
                        onClick = {
                            state.onSetAiAssistContextMessage(
                                AiAssistMessage(
                                    role = AiAssistMessageRole.User,
                                    prompt = AiAssistMessagePrompt.Summarize
                                )
                            )
                            navController.navigate(AiAssistRoute.AiAssistChat.route)
                        }
                    )
                }
                item {
                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_giveMeKeyTakeaways),
                        onClick = {
                            state.onSetAiAssistContextMessage(
                                AiAssistMessage(
                                    role = AiAssistMessageRole.User,
                                    prompt = AiAssistMessagePrompt.KeyTakeAway
                                )
                            )
                            navController.navigate(AiAssistRoute.AiAssistChat.route)
                        }
                    )
                }
                item {
                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_tellMeMore),
                        onClick = {
                            state.onSetAiAssistContextMessage(
                                AiAssistMessage(
                                    role = AiAssistMessageRole.User,
                                    prompt = AiAssistMessagePrompt.TellMeMore
                                )
                            )
                            navController.navigate(AiAssistRoute.AiAssistChat.route)
                        }
                    )
                }
                item {
                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_generateFlashcards),
                        onClick = {
                            navController.navigate(AiAssistRoute.AiAssistFlashcard.route)
                        }
                    )
                }
            }
        }
    }
}