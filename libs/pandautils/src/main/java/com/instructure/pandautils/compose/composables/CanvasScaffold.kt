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
package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Canvas Scaffold with edge-to-edge support.
 *
 * This scaffold is configured for edge-to-edge display:
 * - TopBar should handle its own status bar insets (CanvasAppBar and CanvasThemedAppBar do this automatically)
 * - Content area respects window insets via contentWindowInsets parameter
 * - Horizontal insets are handled at the Activity level, so this scaffold only applies bottom insets by default
 * - Use this as a drop-in replacement for Material Scaffold in Canvas screens
 *
 * @param contentWindowInsets Window insets to apply to content area. Defaults to bottom navigation bar only.
 *   Horizontal insets (left/right) are handled at the Activity level.
 *   For screens with text input, use: WindowInsets.ime.union(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
 *   For read-only screens, use: WindowInsets(0, 0, 0, 0) or default
 */
@Composable
fun CanvasScaffold(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    drawerShape: Shape? = null,
    drawerElevation: androidx.compose.ui.unit.Dp = androidx.compose.material.DrawerDefaults.Elevation,
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    drawerScrimColor: Color = androidx.compose.material.DrawerDefaults.scrimColor,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    contentWindowInsets: WindowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        isFloatingActionButtonDocked = isFloatingActionButtonDocked,
        drawerContent = drawerContent,
        drawerShape = drawerShape ?: MaterialTheme.shapes.large,
        drawerElevation = drawerElevation,
        drawerBackgroundColor = drawerBackgroundColor,
        drawerContentColor = drawerContentColor,
        drawerScrimColor = drawerScrimColor,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = content
    )
}