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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalRippleConfiguration
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RippleConfiguration
import androidx.compose.material.Typography
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.instructure.pandautils.R

val LocalCourseColor = staticCompositionLocalOf<Color> {
    Color.Unspecified
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CanvasTheme(
    courseColor: Color = LocalCourseColor.current,
    content: @Composable () -> Unit) {
    MaterialTheme(
        typography = typography.copy(
            button = typography.button.copy(letterSpacing = TextUnit(0.5f, TextUnitType.Sp)),
            body1 = typography.body1.copy(letterSpacing = TextUnit(0.0f, TextUnitType.Sp))
        )
    ) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(color = colorResource(id = R.color.backgroundDark), getRippleAlpha(isSystemInDarkTheme())),
            LocalTextSelectionColors provides getCustomTextSelectionColors(context = LocalContext.current),
            LocalTextStyle provides TextStyle(
                fontFamily = lato,
                letterSpacing = TextUnit(0f, TextUnitType.Sp)
            ),
            LocalCourseColor provides courseColor,
            content = content
        )
    }
}

private val lato = FontFamily(
    Font(R.font.lato_regular, weight = FontWeight.Normal),
    Font(R.font.lato_semibold, weight = FontWeight.SemiBold),
    Font(R.font.lato_italic, style = FontStyle.Italic),
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