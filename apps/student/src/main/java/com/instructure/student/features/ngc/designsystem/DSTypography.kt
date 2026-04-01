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
package com.instructure.student.features.ngc.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import com.instructure.horizon.horizonui.foundation.HorizonTypography

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
 * Horizon design system typography implementation.
 * Delegates to [HorizonTypography] for all values.
 */
@Stable
object HorizonDSTypography : DSTypography {
    override val h1: TextStyle get() = HorizonTypography.h1
    override val h2: TextStyle get() = HorizonTypography.h2
    override val h3: TextStyle get() = HorizonTypography.h3
    override val h4: TextStyle get() = HorizonTypography.h4

    override val sh1: TextStyle get() = HorizonTypography.sh1
    override val sh2: TextStyle get() = HorizonTypography.sh2
    override val sh3: TextStyle get() = HorizonTypography.sh3
    override val sh4: TextStyle get() = HorizonTypography.sh4

    override val p1: TextStyle get() = HorizonTypography.p1
    override val p2: TextStyle get() = HorizonTypography.p2
    override val p3: TextStyle get() = HorizonTypography.p3

    override val tag: TextStyle get() = HorizonTypography.tag
    override val labelLargeBold: TextStyle get() = HorizonTypography.labelLargeBold
    override val labelMediumBold: TextStyle get() = HorizonTypography.labelMediumBold
    override val labelSmallBold: TextStyle get() = HorizonTypography.labelSmallBold
    override val labelSmall: TextStyle get() = HorizonTypography.labelSmall

    override val buttonTextLarge: TextStyle get() = HorizonTypography.buttonTextLarge
    override val buttonTextMedium: TextStyle get() = HorizonTypography.buttonTextMedium
}

/**
 * Next Gen Canvas design system typography implementation.
 * Currently uses the same values as Horizon as a placeholder.
 * TODO: Replace with actual NGC typography values when available.
 */
@Stable
object NGCTypography : DSTypography {
    override val h1: TextStyle get() = HorizonTypography.h1
    override val h2: TextStyle get() = HorizonTypography.h2
    override val h3: TextStyle get() = HorizonTypography.h3
    override val h4: TextStyle get() = HorizonTypography.h4

    override val sh1: TextStyle get() = HorizonTypography.sh1
    override val sh2: TextStyle get() = HorizonTypography.sh2
    override val sh3: TextStyle get() = HorizonTypography.sh3
    override val sh4: TextStyle get() = HorizonTypography.sh4

    override val p1: TextStyle get() = HorizonTypography.p1
    override val p2: TextStyle get() = HorizonTypography.p2
    override val p3: TextStyle get() = HorizonTypography.p3

    override val tag: TextStyle get() = HorizonTypography.tag
    override val labelLargeBold: TextStyle get() = HorizonTypography.labelLargeBold
    override val labelMediumBold: TextStyle get() = HorizonTypography.labelMediumBold
    override val labelSmallBold: TextStyle get() = HorizonTypography.labelSmallBold
    override val labelSmall: TextStyle get() = HorizonTypography.labelSmall

    override val buttonTextLarge: TextStyle get() = HorizonTypography.buttonTextLarge
    override val buttonTextMedium: TextStyle get() = HorizonTypography.buttonTextMedium
}

/**
 * Returns the appropriate [DSTypography] based on the current [LocalDesignSystem].
 */
val currentTypography: DSTypography
    @Composable
    get() = when (LocalDesignSystem.current) {
        DesignSystem.Horizon -> HorizonDSTypography
        DesignSystem.NextGenCanvas -> NGCTypography
    }