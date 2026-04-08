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
package com.instructure.horizon.features.aiassistant.common.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.features.aiassistant.aiinformation.AiInformationScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.organisms.scaffolds.EdgeToEdgeScaffold

@Composable
fun AiAssistScaffold(
    navController: NavHostController,
    onClearChatHistory: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    var showAiInformation by rememberSaveable { mutableStateOf(false) }

    EdgeToEdgeScaffold(
        statusBarColor = HorizonColors.Surface.aiGradientStart(),
        statusBarAlpha = 0f,
        navigationBarColor = HorizonColors.Surface.aiGradientEnd(),
        navigationBarAlpha = 0f,
        containerColor = Color.Transparent,
        modifier = modifier.background(HorizonColors.Surface.aiGradient())
    ) { contentPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            AiAssistToolbar(
                onDismissPressed = {
                    onClearChatHistory()
                    onDismiss()
                },
                onBackPressed = if (navController.previousBackStackEntry != null) {
                    {
                        onClearChatHistory()
                        navController.popBackStack()
                    }
                } else {
                    null
                },
                onInfoPressed = { showAiInformation = true },
                modifier = Modifier
            )

            content(Modifier
                .weight(1f)
                .padding(horizontal = 24.dp))
        }
    }

    if (showAiInformation) {
        AiInformationScreen(
            onDismiss = { showAiInformation = false },
        )
    }
}
