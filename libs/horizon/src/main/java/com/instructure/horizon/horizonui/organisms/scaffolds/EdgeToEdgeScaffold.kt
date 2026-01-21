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
package com.instructure.horizon.horizonui.organisms.scaffolds

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.util.HorizonEdgeToEdgeSystemBars

@Composable
fun EdgeToEdgeScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = HorizonColors.Surface.pagePrimary(),
    contentColor: Color = contentColorFor(containerColor),
    statusBarColor: Color? = HorizonColors.Surface.pagePrimary(),
    navigationBarColor: Color? = null,
    statusBarAlpha: Float = 0.8f,
    navigationBarAlpha: Float = 0.8f,
    content: @Composable (PaddingValues) -> Unit
) {
    HorizonEdgeToEdgeSystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        statusBarAlpha = statusBarAlpha,
        navigationBarAlpha = navigationBarAlpha
    ) { statusBarWindowInsets, navigationBarWindowInsets ->
        Scaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentColor = contentColor,
            contentWindowInsets = statusBarWindowInsets.union(navigationBarWindowInsets).union(WindowInsets.ime),
            content = content
        )
    }
}