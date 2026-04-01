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
package com.instructure.student.features.ngc.designsystem.ngc

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.pandautils.compose.modifiers.conditional

private val NGCButtonShape = RoundedCornerShape(12.dp)

enum class NGCIconButtonSize(val size: Dp, val badgeOffset: Dp) {
    SMALL(32.dp, 8.dp),
    NORMAL(44.dp, 5.dp)
}

sealed class NGCIconButtonColor(
    open val backgroundColor: Color,
    open val iconColor: Color,
    open val borderColor: Color = Color.Transparent,
    open val secondaryBackgroundColor: Color? = null
) {
    data object Black : NGCIconButtonColor(
        HorizonColors.Surface.inversePrimary(),
        HorizonColors.Icon.surfaceColored()
    )

    data object Inverse : NGCIconButtonColor(
        HorizonColors.Surface.pageSecondary(),
        HorizonColors.Icon.default()
    )

    data object Danger : NGCIconButtonColor(
        HorizonColors.Icon.error(),
        HorizonColors.Surface.pageSecondary()
    )

    data object InverseDanger : NGCIconButtonColor(
        HorizonColors.Surface.pageSecondary(),
        HorizonColors.Icon.error()
    )

    data object Institution : NGCIconButtonColor(
        HorizonColors.Surface.institution(),
        HorizonColors.Icon.surfaceColored()
    )

    data object Beige : NGCIconButtonColor(
        HorizonColors.Surface.pagePrimary(),
        HorizonColors.Icon.default()
    )

    data object Ghost : NGCIconButtonColor(
        Color.Transparent,
        HorizonColors.Icon.default()
    )

    data object BlackGhost : NGCIconButtonColor(
        Color.Transparent,
        HorizonColors.Icon.surfaceColored()
    )

    data object WhiteGreyOutline : NGCIconButtonColor(
        HorizonColors.Surface.cardPrimary(),
        HorizonColors.Icon.default(),
        HorizonColors.LineAndBorder.lineStroke()
    )

    data object DarkOutline : NGCIconButtonColor(
        Color.Transparent,
        HorizonColors.Icon.default(),
        HorizonColors.Surface.inversePrimary()
    )

    data object WhiteOutline : NGCIconButtonColor(
        Color.Transparent,
        HorizonColors.Icon.surfaceColored(),
        HorizonColors.Surface.cardPrimary()
    )

    data object Ai : NGCIconButtonColor(
        HorizonColors.Surface.aiGradientStart(),
        HorizonColors.Icon.surfaceColored(),
        secondaryBackgroundColor = HorizonColors.Surface.aiGradientEnd()
    )

    data object White : NGCIconButtonColor(
        HorizonColors.Surface.pageSecondary(),
        HorizonColors.Icon.default()
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
    val buttonModifier = Modifier
        .conditional(!enabled) {
            alpha(0.5f)
        }
        .conditional(elevation != null) {
            horizonShadow(elevation = elevation!!, shape = NGCButtonShape)
        }
        .conditional(color.secondaryBackgroundColor != null) {
            background(
                brush = Brush.verticalGradient(
                    colors = listOf(color.backgroundColor, color.secondaryBackgroundColor!!)
                ),
                shape = NGCButtonShape,
                alpha = if (enabled) 1f else 0.5f
            )
        }
    val buttonBackgroundColor = if (color.secondaryBackgroundColor == null) color.backgroundColor else Color.Transparent

    Box(contentAlignment = Alignment.TopEnd, modifier = modifier) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier
                .background(shape = NGCButtonShape, color = buttonBackgroundColor)
                .border(HorizonBorder.level1(color.borderColor), shape = NGCButtonShape)
                .size(size.size)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
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