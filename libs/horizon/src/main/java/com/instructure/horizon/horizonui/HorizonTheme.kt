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
 *
 *
 */

package com.instructure.horizon.horizonui

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surfaceContainerLow = HorizonColors.Surface.pageTertiary(),
        )
    ) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(
                color = HorizonColors.Surface.overlayGrey(),
                getRippleAlpha(isSystemInDarkTheme())
            ),
            LocalTextSelectionColors provides getCustomTextSelectionColors(context = LocalContext.current),
            LocalTextStyle provides HorizonTypography.p1,
            content = content
        )
    }
}

private fun getRippleAlpha(isSystemInDarkTheme: Boolean): RippleAlpha {
    return if (isSystemInDarkTheme) {
        RippleAlpha(
            pressedAlpha = 0.10f,
            focusedAlpha = 0.12f,
            draggedAlpha = 0.08f,
            hoveredAlpha = 0.04f
        )
    } else {
        RippleAlpha(
            pressedAlpha = 0.24f,
            focusedAlpha = 0.24f,
            draggedAlpha = 0.16f,
            hoveredAlpha = 0.08f
        )
    }

}

private fun getCustomTextSelectionColors(context: Context): TextSelectionColors {
    val color = Color(context.getColor(R.color.textDarkest))
    return TextSelectionColors(
        handleColor = color,
        backgroundColor = color.copy(alpha = 0.4f)
    )
}