/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.aiassistant

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.features.aiassistant.common.AiAssistDetailedFeedback
import com.instructure.horizon.features.aiassistant.common.AiAssistFeedback
import com.instructure.horizon.features.aiassistant.common.AiAssistFeedbackType
import com.instructure.horizon.features.aiassistant.common.AiAssistInput
import com.instructure.horizon.features.aiassistant.common.AiAssistPagination
import com.instructure.horizon.features.aiassistant.common.AiAssistResponseTextBlock
import com.instructure.horizon.features.aiassistant.common.AiAssistResponseTextBlockSource
import com.instructure.horizon.features.aiassistant.common.AiAssistSuggestionTextBlock
import com.instructure.horizon.features.aiassistant.common.AiAssistToolbar
import com.instructure.horizon.features.aiassistant.common.AiAssistUserTextBlock
import com.instructure.horizon.features.aiassistant.flashcard.AiAssistFlashCard
import com.instructure.horizon.features.aiassistant.quiz.AiAssistQuizAnswer
import com.instructure.horizon.features.aiassistant.quiz.AiAssistQuizAnswerStatus
import com.instructure.horizon.features.aiassistant.quiz.AiAssistQuizFooter
import com.instructure.horizon.features.aiassistant.quiz.AiAssistQuizRatingFooter
import com.instructure.horizon.horizonui.foundation.HorizonColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AiAssistantScreen(navHostController: NavHostController) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        containerColor = Color.White,
        onDismissRequest = { navHostController.popBackStack() },
        dragHandle = null,
        sheetState = bottomSheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = HorizonColors.Surface.aiGradient()
                )
        ) {
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                stickyHeader {
                    AiAssistToolbar({})
                }

                item {
                    var selected: AiAssistFeedbackType? by remember { mutableStateOf(null) }
                    AiAssistFeedback(selected, { selected = AiAssistFeedbackType.POSITIVE }, { selected = AiAssistFeedbackType.NEGATIVE })
                }

                item {
                    var value by remember { mutableStateOf(TextFieldValue("")) }
                    AiAssistInput(
                        value = value,
                        onValueChange = { value = it },
                        onSubmitPressed = {}
                    )
                }

                item {
                    AiAssistQuizFooter(true, {}, {})
                }

                item {
                    AiAssistQuizFooter(false, {}, {})
                }

                item {
                    var selected: AiAssistFeedbackType? by remember { mutableStateOf(null) }
                    AiAssistQuizRatingFooter(selected, { selected = AiAssistFeedbackType.POSITIVE }, { selected = AiAssistFeedbackType.NEGATIVE }, {})
                }

                item {
                    var currentPage by remember { mutableStateOf(1) }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ){
                        AiAssistPagination(currentPage, 5, { currentPage++ }, { currentPage-- })
                    }
                }

                item {
                    AiAssistSuggestionTextBlock("This is a suggestion for the AI Assist", {})
                }

                item {
                    AiAssistUserTextBlock("This is an input from the User")
                }

                item {
                    var selected: AiAssistFeedbackType? by remember { mutableStateOf(null) }
                    AiAssistResponseTextBlock(
                        "This is a response from the AI Assist",
                        listOf(
                            AiAssistResponseTextBlockSource("Source 1", "https://source1.com"),
                            AiAssistResponseTextBlockSource("Source 2", "https://source2.com")
                        ),
                        selected,
                        {},
                        { selected = AiAssistFeedbackType.POSITIVE },
                        { selected = AiAssistFeedbackType.NEGATIVE },
                    )
                }

                item {
                    AiAssistDetailedFeedback(
                        onSubmit = {},
                        onDismiss = {}
                    )
                }

                item {
                    var selectedIndex by remember { mutableStateOf(0) }
                    AiAssistQuizAnswer(
                        text = "This is a quiz answer",
                        status = AiAssistQuizAnswerStatus.entries[selectedIndex],
                        onClick = {
                            if (selectedIndex == AiAssistQuizAnswerStatus.entries.lastIndex) {
                                selectedIndex = 0
                            } else {
                                selectedIndex++
                            }
                        }
                    )
                }

                item {
                    var isFlippedToAnswer by remember { mutableStateOf(false) }

                    AiAssistFlashCard(
                        question = "What is the capital of France?",
                        answer = "Paris",
                        isFlippedToAnswer = isFlippedToAnswer,
                        onClick = { isFlippedToAnswer = !isFlippedToAnswer },
                    )
                }
            }
        }
    }
}