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
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessagePrompt
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessageRole
import com.instructure.horizon.features.aiassistant.navigation.AiAssistRoute

@Composable
fun AiAssistMainScreen(
    navController: NavHostController,
    aiContext: AiAssistContext,
    onDismiss: () -> Unit,
) {
    var promptInput by remember { mutableStateOf(TextFieldValue("")) }
    AiAssistScaffold(
        navController = navController,
        onDismiss = { onDismiss() },
        inputTextValue = promptInput,
        onInputTextChanged = { promptInput = it },
        onInputTextSubmitted = {
            val newContext = aiContext.copy(
                chatHistory = aiContext.chatHistory + listOf(
                    AiAssistMessage(
                        role = AiAssistMessageRole.User,
                        prompt = AiAssistMessagePrompt.Custom(promptInput.text)
                    )
                )
            )
            navController.currentBackStackEntry?.savedStateHandle?.set("aiContext", newContext)
            navController.navigate(AiAssistRoute.AiAssistChat(newContext))
       }
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            if (aiContext.isEmpty()) {
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
                            navController.navigate(AiAssistRoute.AiAssistQuiz(aiContext))
                        }
                    )
                }
                item {
                    val newContext = aiContext.copy(
                        chatHistory = aiContext.chatHistory + listOf(
                            AiAssistMessage(
                                role = AiAssistMessageRole.User,
                                prompt = AiAssistMessagePrompt.Summarize
                            )
                        )
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set("aiContext", newContext)

                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_summarize),
                        onClick = {
                            navController.navigate(
                                AiAssistRoute.AiAssistChat(newContext)
                            )
                        }
                    )
                }
                item {
                    val newContext = aiContext.copy(
                        chatHistory = aiContext.chatHistory + listOf(
                            AiAssistMessage(
                                role = AiAssistMessageRole.User,
                                prompt = AiAssistMessagePrompt.KeyTakeAway
                            )
                        )
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set("aiContext", newContext)

                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_giveMeKeyTakeaways),
                        onClick = {
                            navController.navigate(
                                AiAssistRoute.AiAssistChat(newContext)
                            )
                        }
                    )
                }
                item {
                    val newContext = aiContext.copy(
                        chatHistory = aiContext.chatHistory + listOf(
                            AiAssistMessage(
                                role = AiAssistMessageRole.User,
                                prompt = AiAssistMessagePrompt.TellMeMore
                            )
                        )
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set("aiContext", newContext)

                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_tellMeMore),
                        onClick = {
                            navController.navigate(
                                AiAssistRoute.AiAssistChat(newContext)
                            )
                        }
                    )
                }
                item {
                    AiAssistSuggestionTextBlock(
                        text = stringResource(R.string.ai_generateFlashcards),
                        onClick = {
                            navController.navigate(AiAssistRoute.AiAssistFlashcard(aiContext))
                        }
                    )
                }
            }
        }
    }
}