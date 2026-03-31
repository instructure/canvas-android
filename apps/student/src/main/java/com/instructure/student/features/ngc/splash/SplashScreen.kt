/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.features.ngc.splash

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
import com.instructure.pandares.R

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
            .background(color = colorResource(id = R.color.backgroundLightest))
    ) {
        AndroidView(
            factory = {
                CanvasLoadingView(it).apply {
                    setOverrideColor(it.getColor(R.color.login_studentAppTheme))
                }
            },
            modifier = Modifier.size(120.dp)
        )
    }
}