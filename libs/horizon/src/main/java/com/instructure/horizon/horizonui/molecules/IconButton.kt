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
package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.pandautils.compose.modifiers.conditional

enum class IconButtonSize(val size: Dp, val badgeOffset: Dp) {
    SMALL(32.dp, 8.dp),
    NORMAL(44.dp, 5.dp),
}

enum class IconButtonColor(
    val backgroundColor: Color,
    val iconColor: Color,
    val borderColor: Color = Color.Transparent,
    val secondaryBackgroundColor: Color? = null
) {
    BLACK(HorizonColors.Surface.inversePrimary(), HorizonColors.Icon.surfaceColored()),
    INVERSE(HorizonColors.Surface.pageSecondary(), HorizonColors.Icon.default()),
    INVERSE_DANGER(HorizonColors.Surface.pageSecondary(), HorizonColors.Icon.error()),
    INSTITUTION(HorizonColors.Surface.institution(), HorizonColors.Icon.surfaceColored()),
    BEIGE(HorizonColors.Surface.pagePrimary(), HorizonColors.Icon.default()),
    GHOST(Color.Transparent, HorizonColors.Icon.default()),
    BLACK_GHOST(Color.Transparent, HorizonColors.Icon.surfaceColored()),
    WHITE_GREY_OUTLINE(
        HorizonColors.Surface.pagePrimary(),
        HorizonColors.Icon.default(),
        HorizonColors.LineAndBorder.lineStroke()
    ),
    AI(
        HorizonColors.Surface.aiGradientStart(),
        HorizonColors.Icon.surfaceColored(),
        secondaryBackgroundColor = HorizonColors.Surface.aiGradientEnd()
    ),
}

@Composable
fun IconButton(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: IconButtonSize = IconButtonSize.NORMAL,
    color: IconButtonColor = IconButtonColor.BLACK,
    elevation: Dp? = null,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit = {},
    badge: @Composable (() -> Unit)? = null
) {
    val buttonModifier = Modifier
        .conditional(!enabled) {
            alpha(0.5f)
        }
        .conditional(elevation != null) {
            shadow(elevation = elevation!!, shape = CircleShape)
        }
        .conditional(color.secondaryBackgroundColor != null) {
            background(
                brush = Brush.verticalGradient(
                    colors = listOf(color.backgroundColor, color.secondaryBackgroundColor!!)
                ), shape = HorizonCornerRadius.level6, alpha = if (enabled) 1f else 0.5f
            )
        }
    val buttonBackgroundColor = if (color.secondaryBackgroundColor == null) color.backgroundColor else Color.Transparent
    Box(contentAlignment = Alignment.TopEnd, modifier = modifier) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier
                .background(shape = CircleShape, color = buttonBackgroundColor)
                .border(HorizonBorder.level1(color.borderColor), shape = CircleShape)
                .size(size.size)
        ) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = color.iconColor
            )
        }
        badge?.let {
            Box(modifier = Modifier.offset(x = size.badgeOffset, y = (-size.badgeOffset))) {
                it()
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun IconButtonPreview() {
    ContextKeeper.appContext = LocalContext.current
    IconButton(iconRes = R.drawable.add)
}

@Composable
@Preview(showBackground = true, widthDp = 54, heightDp = 54)
private fun IconButtonWithBadgePreview() {
    ContextKeeper.appContext = LocalContext.current
    Box(contentAlignment = Alignment.Center) {
        IconButton(iconRes = R.drawable.add, badge = {
            Badge(
                content = BadgeContent.Text("5"),
                type = BadgeType.Primary
            )
        })
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonInversePreview() {
    IconButton(iconRes = R.drawable.add, color = IconButtonColor.INVERSE)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonInverseDisabledPreview() {
    IconButton(iconRes = R.drawable.add, color = IconButtonColor.INVERSE, enabled = false)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonInverseDangerPreview() {
    IconButton(iconRes = R.drawable.add, color = IconButtonColor.INVERSE_DANGER)
}

@Composable
@Preview(showBackground = true)
private fun IconButtonSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    IconButton(iconRes = R.drawable.add, size = IconButtonSize.SMALL)
}

@Composable
@Preview(showBackground = true)
private fun IconButtonDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    IconButton(iconRes = R.drawable.add, enabled = false)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonBeigePreview() {
    IconButton(iconRes = R.drawable.add, color = IconButtonColor.BEIGE)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonInstitutionPreview() {
    IconButton(iconRes = R.drawable.add, color = IconButtonColor.INSTITUTION)
}

@Composable
@Preview(showBackground = true)
private fun IconButtonOutlinePreview() {
    IconButton(iconRes = R.drawable.add, color = IconButtonColor.WHITE_GREY_OUTLINE)
}

@Composable
@Preview(showBackground = true)
private fun IconButtonGhostPreview() {
    IconButton(iconRes = R.drawable.add, color = IconButtonColor.GHOST)
}

@Composable
@Preview
private fun IconButtonAiPreview() {
    IconButton(iconRes = R.drawable.ai, color = IconButtonColor.AI)
}

@Composable
@Preview
private fun IconButtonAiSmallPreview() {
    IconButton(iconRes = R.drawable.ai, color = IconButtonColor.AI, size = IconButtonSize.SMALL)
}