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
 */
package com.instructure.pandautils.compose

import androidx.annotation.FontRes
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.instructure.pandautils.R

@Composable
fun CanvasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = typography,
        content = content
    )
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