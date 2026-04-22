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

package com.instructure.ngc.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle

/**
 * Design system agnostic typography interface.
 * Implementations provide concrete TextStyles for each typography token.
 */
@Stable
interface DSTypography {
    // Headings
    val h1: TextStyle
    val h2: TextStyle
    val h3: TextStyle
    val h4: TextStyle

    // Subheadings
    val sh1: TextStyle
    val sh2: TextStyle
    val sh3: TextStyle
    val sh4: TextStyle

    // Paragraphs
    val p1: TextStyle
    val p2: TextStyle
    val p3: TextStyle

    // Labels
    val tag: TextStyle
    val labelLargeBold: TextStyle
    val labelMediumBold: TextStyle
    val labelSmallBold: TextStyle
    val labelSmall: TextStyle

    // Button text
    val buttonTextLarge: TextStyle
    val buttonTextMedium: TextStyle
}

/**
 * Next Gen Canvas design system typography implementation.
 * Uses standalone [NGCTypography] values.
 */
@Stable
object NGCDSTypography : DSTypography {
    override val h1: TextStyle get() = NGCTypography.h1
    override val h2: TextStyle get() = NGCTypography.h2
    override val h3: TextStyle get() = NGCTypography.h3
    override val h4: TextStyle get() = NGCTypography.h4

    override val sh1: TextStyle get() = NGCTypography.sh1
    override val sh2: TextStyle get() = NGCTypography.sh2
    override val sh3: TextStyle get() = NGCTypography.sh3
    override val sh4: TextStyle get() = NGCTypography.sh4

    override val p1: TextStyle get() = NGCTypography.p1
    override val p2: TextStyle get() = NGCTypography.p2
    override val p3: TextStyle get() = NGCTypography.p3

    override val tag: TextStyle get() = NGCTypography.tag
    override val labelLargeBold: TextStyle get() = NGCTypography.labelLargeBold
    override val labelMediumBold: TextStyle get() = NGCTypography.labelMediumBold
    override val labelSmallBold: TextStyle get() = NGCTypography.labelSmallBold
    override val labelSmall: TextStyle get() = NGCTypography.labelSmall

    override val buttonTextLarge: TextStyle get() = NGCTypography.buttonTextLarge
    override val buttonTextMedium: TextStyle get() = NGCTypography.buttonTextMedium
}

/**
 * Returns the appropriate [DSTypography] based on the current [LocalDesignSystem].
 * Currently only supports NGC, with placeholder for future Legacy Canvas support.
 */
val currentTypography: DSTypography
    @Composable
    get() = when (LocalDesignSystem.current) {
        DesignSystem.NextGenCanvas -> NGCDSTypography
        DesignSystem.LegacyCanvas -> NGCDSTypography  // TODO: Implement LegacyCanvas typography when needed
    }
