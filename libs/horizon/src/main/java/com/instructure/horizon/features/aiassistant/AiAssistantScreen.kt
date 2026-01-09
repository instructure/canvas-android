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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.features.aiassistant.navigation.AiAssistNavigation
import com.instructure.horizon.horizonui.foundation.HorizonColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    mainNavController: NavHostController,
    onDismiss: () -> Unit,
) {
    val navController = rememberNavController()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        containerColor = Color.White,
        onDismissRequest = { onDismiss() },
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
            AiAssistNavigation(mainNavController, navController, onDismiss)
        }
    }
}