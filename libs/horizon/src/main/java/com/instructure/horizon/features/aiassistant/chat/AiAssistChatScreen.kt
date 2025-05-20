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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistResponseTextBlock
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistUserTextBlock
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessageRole
import com.instructure.horizon.features.aiassistant.common.model.toDisplayText
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
        onDismiss = { onDismiss() },
        inputTextValue = state.inputTextValue,
        onInputTextChanged = { state.onInputTextChanged(it) },
        onInputTextSubmitted = { state.onInputTextSubmitted() },
    ) { modifier ->
        val context = LocalContext.current
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            items(state.messages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    when (message.role) {
                        is AiAssistMessageRole.User -> {
                            Spacer(modifier = Modifier.weight(1f))
                            AiAssistUserTextBlock(
                                text = message.prompt.toDisplayText(context),
                                modifier = Modifier.padding(start = 24.dp)
                            )
                        }

                        is AiAssistMessageRole.Assistant -> AiAssistResponseTextBlock(
                            text = message.prompt.toDisplayText(context),
                            modifier = Modifier.padding(end = 24.dp)
                        )
                    }
                }
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