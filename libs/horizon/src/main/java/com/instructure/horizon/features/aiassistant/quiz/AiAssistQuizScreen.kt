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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistSuggestionTextBlock
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistToolbar

@Composable
fun AiAssistQuizScreen(
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        AiAssistToolbar(
            onDismissPressed = { onDismiss() },
            onBackPressed = if (navController.previousBackStackEntry != null) {
                { navController.popBackStack() }
            } else {
                null
            }
        )
        AiAssistSuggestionTextBlock(
            text = "Quiz Screen",
            onClick = { }
        )
    }
}