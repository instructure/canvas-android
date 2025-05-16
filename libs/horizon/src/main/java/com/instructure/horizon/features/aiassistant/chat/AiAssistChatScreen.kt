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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistInput
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistSuggestionTextBlock
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistToolbar
import com.instructure.horizon.features.aiassistant.navigation.AiAssistRoute

@Composable
fun AiAssistChatScreen(
    mainNavController: NavHostController,
    navController: NavHostController,
    state: AiAssistChatUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ){

        AiAssistToolbar(
            onDismissPressed = { mainNavController.popBackStack() },
            onBackPressed = if (navController.previousBackStackEntry != null) {
                { navController.popBackStack() }
            } else {
                null
            }
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            (0..20).forEach {
                item {
                    AiAssistSuggestionTextBlock(
                        text = "Chat Screen",
                        onClick = { navController.navigate(AiAssistRoute.AiAssistChat.route) },
                    )
                }
            }
        }

        AiAssistInput(
            value = state.inputTextValue,
            onValueChange = { state.onInputTextChanged(it) },
            onSubmitPressed = { state.onInputTextSubmitted() },
        )
    }
}