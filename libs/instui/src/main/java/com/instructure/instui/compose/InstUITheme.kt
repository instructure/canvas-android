/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.instui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUISemanticColors

val LocalCourseColor = staticCompositionLocalOf<Color> {
    Color.Unspecified
}

@Composable
fun InstUITheme(
    courseColor: Color = LocalCourseColor.current,
    content: @Composable () -> Unit,
) {
    // Map the InstUI semantic tokens onto the Material3 color scheme so built-in M3 components
    // (menus, sheets, surfaces) render with InstUI neutrals instead of the default Material3
    // (purple-tinted) baseline.
    val container = InstUISemanticColors.Background.container()
    val page = InstUISemanticColors.Background.page()
    val text = InstUISemanticColors.Text.base()
    val onColor = InstUISemanticColors.Text.onColor()
    val baseScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    val colorScheme = baseScheme.copy(
        primary = courseColor,
        onPrimary = onColor,
        background = page,
        onBackground = text,
        surface = container,
        onSurface = text,
        surfaceVariant = container,
        onSurfaceVariant = text,
        surfaceContainerLowest = container,
        surfaceContainerLow = container,
        surfaceContainer = container,
        surfaceContainerHigh = container,
        surfaceContainerHighest = container,
    )

    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(
            LocalTextStyle provides InstUIText.content,
            LocalContentColor provides InstUISemanticColors.Text.base(),
            LocalCourseColor provides courseColor,
            content = content
        )
    }
}