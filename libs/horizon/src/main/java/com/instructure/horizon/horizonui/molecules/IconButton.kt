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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors

enum class IconButtonSize(val primaryButtonSize: Dp, val secondaryButtonSize: Dp) {
    SMALL(32.dp, 40.dp),
    NORMAL(44.dp, 44.dp),
}

enum class IconButtonColor(val backgroundColor: Color, val iconColor: Color) {
    BLACK(HorizonColors.Surface.inversePrimary(), HorizonColors.Icon.surfaceColored()),
    INVERSE(HorizonColors.Surface.pageSecondary(), HorizonColors.Icon.default()),
    INVERSE_DANGER(HorizonColors.Surface.pageSecondary(), HorizonColors.Icon.error()),
    INSTITUTION(HorizonColors.Surface.institution(), HorizonColors.Icon.surfaceColored()),
    BEIGE(HorizonColors.Surface.pagePrimary(), HorizonColors.Icon.default()),
}

@Composable
fun IconButtonPrimary(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: IconButtonSize = IconButtonSize.NORMAL,
    color: IconButtonColor = IconButtonColor.BLACK,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {
    IconButton(
        iconRes = iconRes,
        size = size.primaryButtonSize,
        backgroundColor = color.backgroundColor,
        iconColor = color.iconColor,
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        contentDescription = contentDescription
    )
}

@Composable
fun IconButtonSecondary(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: IconButtonSize = IconButtonSize.NORMAL,
    color: IconButtonColor = IconButtonColor.INSTITUTION,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {
    IconButton(
        iconRes = iconRes,
        size = size.secondaryButtonSize,
        backgroundColor = color.backgroundColor,
        iconColor = color.iconColor,
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        contentDescription = contentDescription
    )
}

@Composable
fun IconButtonAi(
    modifier: Modifier = Modifier,
    size: IconButtonSize = IconButtonSize.NORMAL,
    enabled: Boolean = true,
    @DrawableRes iconRes: Int = R.drawable.ai,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {
    val buttonModifier = if (enabled) modifier else modifier.alpha(0.5f)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonModifier
            .background(
                brush = HorizonColors.Surface.aiGradient(),
                shape = CircleShape
            )
            .size(size.primaryButtonSize)
    ) {
        Icon(
            painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = HorizonColors.Icon.surfaceColored()
        )
    }
}

@Composable
private fun IconButton(
    @DrawableRes iconRes: Int,
    size: Dp,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {
    val buttonModifier = if (enabled) modifier else modifier.alpha(0.5f)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonModifier
            .background(shape = CircleShape, color = backgroundColor)
            .size(size)
    ) {
        Icon(
            painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun IconButtonPrimaryPreview() {
    IconButtonPrimary(iconRes = R.drawable.add)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonPrimaryInversePreview() {
    IconButtonPrimary(iconRes = R.drawable.add, color = IconButtonColor.INVERSE)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonPrimaryInverseDisabledPreview() {
    IconButtonPrimary(iconRes = R.drawable.add, color = IconButtonColor.INVERSE, enabled = false)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonPrimaryInverseDangerPreview() {
    IconButtonPrimary(iconRes = R.drawable.add, color = IconButtonColor.INVERSE_DANGER)
}

@Composable
@Preview(showBackground = true)
private fun IconButtonSecondaryPreview() {
    ContextKeeper.appContext = LocalContext.current
    IconButtonSecondary(iconRes = R.drawable.add)
}

@Composable
@Preview(showBackground = true)
private fun IconButtonSecondarySmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    IconButtonSecondary(iconRes = R.drawable.add, size = IconButtonSize.SMALL)
}

@Composable
@Preview(showBackground = true)
private fun IconButtonSecondaryDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    IconButtonSecondary(iconRes = R.drawable.add, enabled = false)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun IconButtonSecondaryBeigePreview() {
    IconButtonSecondary(iconRes = R.drawable.add, color = IconButtonColor.BEIGE)
}

@Composable
@Preview
private fun IconButtonAiPreview() {
    IconButtonAi()
}

@Composable
@Preview
private fun IconButtonAiSmallPreview() {
    IconButtonAi(size = IconButtonSize.SMALL)
}