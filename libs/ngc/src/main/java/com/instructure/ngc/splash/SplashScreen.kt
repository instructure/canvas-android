/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.ngc.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.views.CanvasLoadingView
import com.instructure.pandautils.R as PandaR

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onThemeApplied: () -> Unit,
    onInitialDataLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.themeToApply) {
        uiState.themeToApply?.let { theme ->
            ThemePrefs.applyCanvasTheme(theme, context)
            onThemeApplied()
        }
    }

    LaunchedEffect(uiState.initialDataLoaded) {
        if (uiState.initialDataLoaded) {
            onInitialDataLoaded()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = PandaR.color.backgroundLightest))
    ) {
        AndroidView(
            factory = {
                CanvasLoadingView(it).apply {
                    setOverrideColor(it.getColor(PandaR.color.login_studentAppTheme))
                }
            },
            modifier = Modifier.size(120.dp)
        )
    }
}
