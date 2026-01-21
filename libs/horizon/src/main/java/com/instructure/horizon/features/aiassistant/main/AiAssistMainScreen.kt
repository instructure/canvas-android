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

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.features.aiassistant.navigation.AiAssistRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.molecules.Spinner

@Composable
fun AiAssistMainScreen(
    mainNavController: NavHostController,
    navController: NavHostController,
    state: AiAssistMainUiState,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(state.messages) {
        val lastMessage = state.messages.lastOrNull()

        if (lastMessage != null && state.messages.size > 2) {
            state.onNavigateToDetails()
            when {
                lastMessage.flashCards.isNotEmpty() -> {
                    navController.navigate(AiAssistRoute.AiAssistFlashcard.route)
                }
                lastMessage.quizItems.isNotEmpty() -> {
                    navController.navigate(AiAssistRoute.AiAssistQuiz.route)
                }
                else -> {
                    navController.navigate(AiAssistRoute.AiAssistChat.route)
                }
            }
        }
    }
    var promptInput by remember { mutableStateOf(TextFieldValue("")) }

    val loadingFocusRequester = remember { FocusRequester() }
    val lastMessageFocusRequester = remember { FocusRequester() }
    val scrollState = rememberLazyListState()

    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            loadingFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            scrollState.animateScrollToItem(state.messages.size)
            if (!state.isLoading) {
                lastMessageFocusRequester.requestFocus()
            }
        }
    }
    AiAssistScaffold(
        navController = navController,
        onClearChatHistory = { },
        onDismiss = { onDismiss() },
        inputTextValue = promptInput,
        onInputTextChanged = { promptInput = it },
        onInputTextSubmitted = {
            state.sendMessage(promptInput.text)
            promptInput = TextFieldValue("")
        }
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = scrollState,
            modifier = modifier
        ) {
            itemsIndexed(state.messages) { index, message ->
                val isLastMessage = index == state.messages.lastIndex
                AiAssistMessage(
                    message = message,
                    onSendPrompt = { state.sendMessage(it) },
                    onSourceSelected = {
                        val request = NavDeepLinkRequest.Builder
                            .fromUri(it.toUri())
                            .build()
                        mainNavController.navigate(request)
                    },
                    focusRequester = if (isLastMessage) lastMessageFocusRequester else null
                )
            }
            if (state.isLoading) {
                item {
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = context.getString(R.string.a11y_igniteAiLoadingContentDescription)
                            }
                            .focusRequester(loadingFocusRequester)
                            .focusable()
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