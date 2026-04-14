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

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Design system agnostic IconButton size.
 * Currently only supports NGC, with placeholder for future Legacy Canvas support.
 */
sealed class DSIconButtonSize {
    abstract val ngc: NGCIconButtonSize

    data object Small : DSIconButtonSize() {
        override val ngc = NGCIconButtonSize.SMALL
    }

    data object Normal : DSIconButtonSize() {
        override val ngc = NGCIconButtonSize.NORMAL
    }
}

/**
 * Design system agnostic IconButton color.
 * Currently only supports NGC, with placeholder for future Legacy Canvas support.
 */
sealed class DSIconButtonColor {
    abstract val ngc: NGCIconButtonColor

    data object Black : DSIconButtonColor() {
        override val ngc = NGCIconButtonColor.Black
    }

    data object Inverse : DSIconButtonColor() {
        override val ngc = NGCIconButtonColor.Inverse
    }
}

/**
 * Design System agnostic IconButton.
 * Currently only supports NGC, with placeholder for future Legacy Canvas support.
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
    badge: @Composable (() -> Unit)? = null
) {
    when (LocalDesignSystem.current) {
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
        DesignSystem.LegacyCanvas -> {
            // TODO: Implement LegacyCanvas IconButton when needed
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
