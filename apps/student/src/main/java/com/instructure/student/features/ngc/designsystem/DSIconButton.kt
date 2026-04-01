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

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.instructure.horizon.horizonui.molecules.IconButtonColor as HorizonIconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize as HorizonIconButtonSize
import com.instructure.horizon.horizonui.molecules.IconButton as HorizonIconButton
import com.instructure.student.features.ngc.designsystem.ngc.NGCIconButton
import com.instructure.student.features.ngc.designsystem.ngc.NGCIconButtonColor
import com.instructure.student.features.ngc.designsystem.ngc.NGCIconButtonSize

/**
 * Design system agnostic IconButton size.
 * Each size maps to the corresponding design system's size implementation.
 */
sealed class DSIconButtonSize {
    abstract val horizon: HorizonIconButtonSize
    abstract val ngc: NGCIconButtonSize

    data object Small : DSIconButtonSize() {
        override val horizon = HorizonIconButtonSize.SMALL
        override val ngc = NGCIconButtonSize.SMALL
    }

    data object Normal : DSIconButtonSize() {
        override val horizon = HorizonIconButtonSize.NORMAL
        override val ngc = NGCIconButtonSize.NORMAL
    }
}

/**
 * Design system agnostic IconButton color.
 * Each color maps to the corresponding design system's color implementation.
 */
sealed class DSIconButtonColor {
    abstract val horizon: HorizonIconButtonColor
    abstract val ngc: NGCIconButtonColor

    data object Black : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.Black
        override val ngc = NGCIconButtonColor.Black
    }

    data object Inverse : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.Inverse
        override val ngc = NGCIconButtonColor.Inverse
    }

    data object Danger : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.Danger
        override val ngc = NGCIconButtonColor.Danger
    }

    data object InverseDanger : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.InverseDanger
        override val ngc = NGCIconButtonColor.InverseDanger
    }

    data object Institution : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.Institution
        override val ngc = NGCIconButtonColor.Institution
    }

    data object Beige : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.Beige
        override val ngc = NGCIconButtonColor.Beige
    }

    data object Ghost : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.Ghost
        override val ngc = NGCIconButtonColor.Ghost
    }

    data object BlackGhost : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.BlackGhost
        override val ngc = NGCIconButtonColor.BlackGhost
    }

    data object WhiteGreyOutline : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.WhiteGreyOutline
        override val ngc = NGCIconButtonColor.WhiteGreyOutline
    }

    data object DarkOutline : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.DarkOutline
        override val ngc = NGCIconButtonColor.DarkOutline
    }

    data object WhiteOutline : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.WhiteOutline
        override val ngc = NGCIconButtonColor.WhiteOutline
    }

    data object Ai : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.Ai
        override val ngc = NGCIconButtonColor.Ai
    }

    data object White : DSIconButtonColor() {
        override val horizon = HorizonIconButtonColor.White
        override val ngc = NGCIconButtonColor.White
    }
}

/**
 * Design System agnostic IconButton.
 * Uses conditional composition to render the appropriate design system's IconButton
 * based on the current [LocalDesignSystem].
 */
@Composable
fun DSIconButton(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: DSIconButtonSize = DSIconButtonSize.Normal,
    color: DSIconButtonColor = DSIconButtonColor.Black,
    elevation: Dp? = null,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit = {},
    badge: @Composable (() -> Unit)? = null,
    buttonContent: @Composable (() -> Unit)? = null
) {
    when (LocalDesignSystem.current) {
        DesignSystem.Horizon -> {
            if (buttonContent != null) {
                HorizonIconButton(
                    iconRes = iconRes,
                    modifier = modifier,
                    size = size.horizon,
                    color = color.horizon,
                    elevation = elevation,
                    enabled = enabled,
                    contentDescription = contentDescription,
                    onClick = onClick,
                    badge = badge,
                    buttonContent = buttonContent
                )
            } else {
                HorizonIconButton(
                    iconRes = iconRes,
                    modifier = modifier,
                    size = size.horizon,
                    color = color.horizon,
                    elevation = elevation,
                    enabled = enabled,
                    contentDescription = contentDescription,
                    onClick = onClick,
                    badge = badge
                )
            }
        }
        DesignSystem.NextGenCanvas -> {
            NGCIconButton(
                iconRes = iconRes,
                modifier = modifier,
                size = size.ngc,
                color = color.ngc,
                elevation = elevation,
                enabled = enabled,
                contentDescription = contentDescription,
                onClick = onClick,
                badge = badge
            )
        }
    }
}