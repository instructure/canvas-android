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
package com.instructure.horizon.horizonui.foundation

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instructure.horizon.R

private const val LINE_HEIGHT_140_PERCENT = 1.4f

private val manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

private val figtree = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_semibold, FontWeight.SemiBold)
)

object HorizonTypography {
    val h1 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        letterSpacing = 0.sp,
        lineHeight = 28.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val h2 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        lineHeight = 24.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val h3 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp,
        lineHeight = 20.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val h4 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        lineHeight = 16.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val sh1 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        letterSpacing = 0.sp,
        lineHeight = 28.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val sh2 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        lineHeight = 24.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val sh3 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        letterSpacing = 0.sp,
        lineHeight = 20.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val sh4 = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        lineHeight = 16.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val p1 = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        lineHeight = 16.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val p2 = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.sp,
        lineHeight = 14.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val p3 = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.sp,
        lineHeight = 12.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val tag = TextStyle(
        fontFamily = manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        color = HorizonColors.Text.title()
    )

    val labelLargeBold = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        lineHeight = 16.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val labelMediumBold = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.sp,
        lineHeight = 14.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val labelSmallBold = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.25.sp,
        lineHeight = 12.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val labelSmall = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.25.sp,
        lineHeight = 12.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val buttonTextLarge = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        lineHeight = 16.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )

    val buttonTextMedium = TextStyle(
        fontFamily = figtree,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        lineHeight = 16.sp * LINE_HEIGHT_140_PERCENT,
        color = HorizonColors.Text.title()
    )
}