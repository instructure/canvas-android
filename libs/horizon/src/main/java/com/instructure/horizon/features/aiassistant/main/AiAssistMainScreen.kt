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
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.features.aiassistant.navigation.AiAssistRoute
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.pandautils.utils.orDefault

@Composable
fun AiAssistMainScreen(
    navController: NavHostController,
    state: AiAssistMainUiState,
    onDismiss: () -> Unit,
) {
    var messageCount by remember { mutableStateOf(state.messages.size) }
    LaunchedEffect(Unit) {
        if (messageCount != state.messages.size) {
            if (state.messages.lastOrNull()?.flashcards?.isNotEmpty().orDefault()) {
                navController.navigate(AiAssistRoute.AiAssistFlashcard.route)
            } else if (state.messages.lastOrNull()?.quizItems?.isNotEmpty().orDefault()) {
                navController.navigate(AiAssistRoute.AiAssistQuiz.route)
            } else if (state.messages.isNotEmpty()) {
                navController.navigate(AiAssistRoute.AiAssistChat.route)
            }

            messageCount = state.messages.size
        }
    }
    var promptInput by remember { mutableStateOf(TextFieldValue("")) }
    AiAssistScaffold(
        navController = navController,
        onClearChatHistory = { },
        onDismiss = { onDismiss() },
        inputTextValue = promptInput,
        onInputTextChanged = { promptInput = it },
        onInputTextSubmitted = {
            state.addMessageToChatHistory(promptInput.text)
//            navController.navigate(AiAssistRoute.AiAssistChat.route)
       }
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            if (state.isLoading) {
                item { Spinner() }
            } else {
                items(state.messages) {
                    AiAssistMessage(
                        it,
                        { state.addMessageToChatHistory(it) }
                    )
                }
            }
        }
    }
}