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

package com.instructure.instui.compose.indicator

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.Icon as M3Icon

private val ButtonShape = RoundedCornerShape(12.dp)

enum class IconButtonSize(val size: Dp, val badgeOffset: Dp) {
    Small(32.dp, 8.dp),
    Normal(44.dp, 5.dp)
}

sealed class IconButtonColor(
    open val backgroundColor: @Composable () -> Color,
    open val iconColor: @Composable () -> Color,
    open val borderColor: @Composable () -> Color = { Color.Transparent }
) {
    data object Default : IconButtonColor(
        backgroundColor = { InstUISemanticColors.Background.inverse() },
        iconColor = { InstUISemanticColors.Icon.onColor() }
    )

    data object Inverse : IconButtonColor(
        backgroundColor = { InstUISemanticColors.Background.container() },
        iconColor = { InstUISemanticColors.Icon.base() }
    )
}

/**
 * Dummy IconButton component. Will be replaced with the final InstUI design.
 */
@Composable
fun IconButton(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: IconButtonSize = IconButtonSize.Normal,
    color: IconButtonColor = IconButtonColor.Default,
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
                        Modifier.shadow(
                            elevation = elevation,
                            shape = ButtonShape,
                            clip = false
                        )
                    } else {
                        Modifier
                    }
                )
                .size(size.size)
                .clip(ButtonShape)
                .background(backgroundColor)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(1.dp, borderColor, ButtonShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
        ) {
            M3Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        if (badge != null) {
            Box(modifier = Modifier.offset(x = size.badgeOffset, y = -(size.badgeOffset))) {
                badge()
            }
        }
    }
}

@Preview(name = "IconButton — Light", showBackground = true)
@Preview(name = "IconButton — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconButtonPreview() {
    InstUITheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp)
        ) {
            IconButton(
                iconRes = android.R.drawable.ic_menu_edit,
                color = IconButtonColor.Default,
            )
            IconButton(
                iconRes = android.R.drawable.ic_menu_edit,
                color = IconButtonColor.Inverse,
                elevation = 4.dp,
            )
            IconButton(
                iconRes = android.R.drawable.ic_menu_edit,
                size = IconButtonSize.Small,
                color = IconButtonColor.Default,
            )
            IconButton(
                iconRes = android.R.drawable.ic_menu_edit,
                color = IconButtonColor.Default,
                enabled = false,
            )
        }
    }
}