/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.compose

import android.content.Context
import androidx.annotation.FontRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.instructure.pandautils.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = typography.copy(
            displayLarge = typography.displayLarge.copy(fontFamily = lato),
            displayMedium = typography.displayMedium.copy(fontFamily = lato),
            displaySmall = typography.displaySmall.copy(fontFamily = lato),
            headlineLarge = typography.headlineLarge.copy(fontFamily = lato),
            headlineMedium = typography.headlineMedium.copy(fontFamily = lato),
            headlineSmall = typography.headlineSmall.copy(fontFamily = lato),
            titleLarge = typography.titleLarge.copy(fontFamily = lato),
            titleMedium = typography.titleMedium.copy(fontFamily = lato),
            titleSmall = typography.titleSmall.copy(fontFamily = lato),
            bodyLarge = typography.bodyLarge.copy(fontFamily = lato),
            bodyMedium = typography.bodyMedium.copy(letterSpacing = TextUnit(0.0f, TextUnitType.Sp), fontFamily = lato),
            bodySmall = typography.bodySmall.copy(fontFamily = lato),
            labelLarge = typography.labelLarge.copy(fontFamily = lato),
            labelMedium = typography.labelMedium.copy(letterSpacing = TextUnit(0.5f, TextUnitType.Sp), fontFamily = lato),
            labelSmall = typography.labelSmall.copy(fontFamily = lato),

        )
    ) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(color = colorResource(id = R.color.backgroundDark), getRippleAlpha(isSystemInDarkTheme())),
            LocalTextSelectionColors provides getCustomTextSelectionColors(context = LocalContext.current),
            LocalTextStyle provides TextStyle(
                fontFamily = lato,
                letterSpacing = TextUnit(0f, TextUnitType.Sp)
            ),
            content = content
        )
    }
}

private val lato = FontFamily(
    Font(R.font.lato_regular)
)

private var typography = Typography()

fun overrideComposeFonts(@FontRes fontResource: Int) {
    val newFont = FontFamily(
        Font(fontResource)
    )

    typography = typography.copy(
        displayLarge = typography.displayLarge.copy(fontFamily = newFont),
        displayMedium = typography.displayMedium.copy(fontFamily = newFont),
        displaySmall = typography.displaySmall.copy(fontFamily = newFont),
        headlineLarge = typography.headlineLarge.copy(fontFamily = newFont),
        headlineMedium = typography.headlineMedium.copy(fontFamily = newFont),
        headlineSmall = typography.headlineSmall.copy(fontFamily = newFont),
        titleLarge = typography.titleLarge.copy(fontFamily = newFont),
        titleMedium = typography.titleMedium.copy(fontFamily = newFont),
        titleSmall = typography.titleSmall.copy(fontFamily = newFont),
        bodyLarge = typography.bodyLarge.copy(fontFamily = newFont),
        bodyMedium = typography.bodyMedium.copy(letterSpacing = TextUnit(0.0f, TextUnitType.Sp), fontFamily = newFont),
        bodySmall = typography.bodySmall.copy(fontFamily = newFont),
        labelLarge = typography.labelLarge.copy(fontFamily = newFont),
        labelMedium = typography.labelMedium.copy(letterSpacing = TextUnit(0.5f, TextUnitType.Sp), fontFamily = newFont),
        labelSmall = typography.labelSmall.copy(fontFamily = newFont),

        )
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