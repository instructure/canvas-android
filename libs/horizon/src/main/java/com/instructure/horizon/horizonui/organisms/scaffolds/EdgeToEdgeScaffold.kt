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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.util.DefaultBarAlpha
import com.instructure.horizon.util.HorizonEdgeToEdgeSystemBars
import com.instructure.horizon.util.horizontalSafeDrawing

/**
 * Edge-to-edge scaffold around `material3.Scaffold` with Horizon's translucent system-bar overlays.
 *
 * The body wrapper applies horizontal safe-drawing insets via `Modifier.windowInsetsPadding`
 * (consume-aware, so a nested scaffold inside `content` won't double-pad). IME is handled by
 * `Modifier.imePadding()` on the same wrapper, with the bottomBar's height consumed first so the
 * keyboard padding doesn't stack on top of the bar padding. The bar slot itself stays at the
 * screen bottom and is hidden behind the keyboard when it opens.
 */
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
    statusBarAlpha: Float = DefaultBarAlpha,
    navigationBarAlpha: Float = DefaultBarAlpha,
    content: @Composable (PaddingValues) -> Unit,
) {
    HorizonEdgeToEdgeSystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        statusBarAlpha = statusBarAlpha,
        navigationBarAlpha = navigationBarAlpha,
    ) { statusBarInsets, navigationBarInsets ->
        Scaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentColor = contentColor,
            // Pull only the bottom from nav insets — horizontal safe drawing is applied via
            // Modifier.windowInsetsPadding on the body wrapper below.
            contentWindowInsets = statusBarInsets
                .add(navigationBarInsets.only(WindowInsetsSides.Bottom)),
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .consumeWindowInsets(paddingValues)
                        .imePadding()
                        .windowInsetsPadding(WindowInsets.horizontalSafeDrawing),
                ) {
                    content(paddingValues)
                }
            },
        )
    }
}
