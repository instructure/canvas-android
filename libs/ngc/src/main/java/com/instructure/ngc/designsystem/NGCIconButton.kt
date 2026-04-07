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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val NGCButtonShape = RoundedCornerShape(12.dp)

enum class NGCIconButtonSize(val size: Dp, val badgeOffset: Dp) {
    SMALL(32.dp, 8.dp),
    NORMAL(44.dp, 5.dp)
}

sealed class NGCIconButtonColor(
    open val backgroundColor: @Composable () -> Color,
    open val iconColor: @Composable () -> Color,
    open val borderColor: @Composable () -> Color = { Color.Transparent }
) {
    data object Black : NGCIconButtonColor(
        backgroundColor = { NGCColors.Surface.inversePrimary() },
        iconColor = { NGCColors.Icon.surfaceColored() }
    )

    data object Inverse : NGCIconButtonColor(
        backgroundColor = { NGCColors.Surface.pageSecondary() },
        iconColor = { NGCColors.Icon.default() }
    )
}

@Composable
fun NGCIconButton(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: NGCIconButtonSize = NGCIconButtonSize.NORMAL,
    color: NGCIconButtonColor = NGCIconButtonColor.Black,
    elevation: Dp? = null,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit = {},
    badge: @Composable (() -> Unit)? = null
) {
    val backgroundColor = color.backgroundColor()
    val iconColor = color.iconColor()
    val borderColor = color.borderColor()
    val interactionSource = remember { MutableInteractionSource() }

    Box(contentAlignment = Alignment.TopEnd, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .then(if (!enabled) Modifier.alpha(0.5f) else Modifier)
                .then(
                    if (elevation != null) {
                        Modifier.shadow(elevation = elevation, shape = NGCButtonShape)
                    } else Modifier
                )
                .size(size.size)
                .clip(NGCButtonShape)
                .background(color = backgroundColor, shape = NGCButtonShape)
                .border(1.dp, borderColor, shape = NGCButtonShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )
        }
        badge?.let {
            Box(modifier = Modifier.offset(x = size.badgeOffset, y = (-size.badgeOffset))) {
                it()
            }
        }
    }
}
