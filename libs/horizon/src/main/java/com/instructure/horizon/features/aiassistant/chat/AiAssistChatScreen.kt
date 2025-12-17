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
package com.instructure.horizon.features.aiassistant.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.molecules.Spinner

@Composable
fun AiAssistChatScreen(
    navController: NavHostController,
    onDismiss: () -> Unit,
    state: AiAssistChatUiState
) {
    AiAssistScaffold(
        navController = navController,
        onClearChatHistory = state.onClearChatHistory,
        onDismiss = { onDismiss() },
        inputTextValue = state.inputTextValue,
        onInputTextChanged = { state.onInputTextChanged(it) },
        onInputTextSubmitted = { state.onInputTextSubmitted() },
    ) { modifier ->
        val context = LocalContext.current
        val scrollState = rememberLazyListState()
        LaunchedEffect(state.messages) {
            scrollState.animateScrollToItem(state.messages.size)
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = scrollState,
            modifier = modifier
        ) {
            items(state.messages) { message ->
                AiAssistMessage(message) { }
            }

            if (state.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Spacer(modifier = Modifier.weight(1f))
                        Spinner(color = HorizonColors.Surface.cardPrimary())
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun AssistChatScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = AiAssistChatUiState(
        messages = listOf(
            JourneyAssistChatMessage(
                id = "1",
                prompt = "Hello",
                displayText = "Hello",
                role = JourneyAssistRole.USER,
            ),
            JourneyAssistChatMessage(
                id = "2",
                prompt = "Hi there! How can I assist you today?",
                displayText = "Hi there! How can I assist you today?",
                role = JourneyAssistRole.ASSISTANT
            )
        ),
        inputTextValue = TextFieldValue("Hi,"),
        isLoading = true
    )

    AiAssistChatScreen(
        navController = NavHostController(LocalContext.current),
        onDismiss = {},
        state = state
    )
}