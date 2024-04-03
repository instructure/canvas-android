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
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
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

@Composable
fun CanvasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = typography.copy(
            button = typography.button.copy(letterSpacing = TextUnit(0.5f, TextUnitType.Sp))
        )
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides CanvasRippleTheme,
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

private var typography = Typography(
    defaultFontFamily = lato,
)

fun overrideComposeFonts(@FontRes fontResource: Int) {
    val newFont = FontFamily(
        Font(fontResource)
    )

    typography = Typography(
        defaultFontFamily = newFont,
    )
}

private object CanvasRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color = colorResource(id = R.color.backgroundDark)

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
        Color.Black,
        lightTheme = !isSystemInDarkTheme()
    )
}

private fun getCustomTextSelectionColors(context: Context): TextSelectionColors {
    val color = Color(context.getColor(R.color.textDarkest))
    return TextSelectionColors(
        handleColor = color,
        backgroundColor = color.copy(alpha = 0.4f)
    )
}