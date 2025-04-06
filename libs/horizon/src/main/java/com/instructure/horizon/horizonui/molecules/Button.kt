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
@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

enum class ButtonHeight(val height: Dp, val textStyle: TextStyle, val verticalPadding: Dp, val horizontalPadding: Dp) {
    SMALL(32.dp, HorizonTypography.buttonTextMedium, 6.dp, 12.dp),
    NORMAL(44.dp, HorizonTypography.buttonTextLarge, 11.dp, 22.dp),
}

enum class ButtonWidth {
    RELATIVE,
    FILL
}

sealed class ButtonColor(
    open val backgroundColor: Color,
    open val contentColor: Color,
    open val outlineColor: Color = Color.Transparent,
    open val secondaryBackgroundColor: Color? = null
) {
    data object Black : ButtonColor(HorizonColors.Surface.inversePrimary(), HorizonColors.Text.surfaceColored())
    data object Inverse : ButtonColor(HorizonColors.Surface.pageSecondary(), HorizonColors.Text.title())
    data object Ai : ButtonColor(
        HorizonColors.Surface.aiGradientStart(),
        HorizonColors.Text.surfaceColored(),
        secondaryBackgroundColor = HorizonColors.Surface.aiGradientEnd()
    )

    data object WhiteWithOutline :
        ButtonColor(HorizonColors.Surface.pageSecondary(), HorizonColors.Text.title(), HorizonColors.LineAndBorder.lineStroke())

    data object BlackOutline : ButtonColor(Color.Transparent, HorizonColors.Text.title(), HorizonColors.Surface.inversePrimary())
    data object WhiteOutline : ButtonColor(Color.Transparent, HorizonColors.Text.surfaceColored(), HorizonColors.Surface.pageSecondary())
    data object Danger : ButtonColor(HorizonColors.Surface.error(), HorizonColors.Text.surfaceColored())
    data object Ghost : ButtonColor(Color.Transparent, HorizonColors.Text.title())
    data object Institution : ButtonColor(HorizonColors.Surface.institution(), HorizonColors.Text.surfaceColored())
    data object Beige : ButtonColor(HorizonColors.Surface.pagePrimary(), HorizonColors.Text.title())
    data class Custom(
        override val backgroundColor: Color,
        override val contentColor: Color,
        override val outlineColor: Color = Color.Transparent,
        override val secondaryBackgroundColor: Color? = null
    ) : ButtonColor(backgroundColor, contentColor, outlineColor, secondaryBackgroundColor)
}

sealed class ButtonIconPosition(@DrawableRes open val iconRes: Int? = null) {
    data object NoIcon : ButtonIconPosition()
    data class Start(@DrawableRes override val iconRes: Int) : ButtonIconPosition(iconRes)
    data class End(@DrawableRes override val iconRes: Int) : ButtonIconPosition(iconRes)
}

@Composable
fun Button(
    label: String,
    modifier: Modifier = Modifier,
    height: ButtonHeight = ButtonHeight.NORMAL,
    width: ButtonWidth = ButtonWidth.RELATIVE,
    color: ButtonColor = ButtonColor.Black,
    iconPosition: ButtonIconPosition = ButtonIconPosition.NoIcon,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    badge: @Composable (() -> Unit)? = null
) {
    val widthModifier = if (width == ButtonWidth.FILL) modifier.fillMaxWidth() else modifier
    val gradientModifier = color.secondaryBackgroundColor?.let {
        widthModifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(color.backgroundColor, it)
            ), shape = HorizonCornerRadius.level6, alpha = if (enabled) 1f else 0.5f
        )
    } ?: widthModifier
    val buttonBackgroundColor = if (color.secondaryBackgroundColor == null) color.backgroundColor else Color.Transparent
    Box(contentAlignment = Alignment.TopEnd) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = HorizonCornerRadius.level6,
            modifier = gradientModifier.defaultMinSize(minHeight = height.height),
            border = HorizonBorder.level1(color.outlineColor),
            contentPadding = PaddingValues(vertical = height.verticalPadding, horizontal = height.horizontalPadding),
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = buttonBackgroundColor,
                contentColor = color.contentColor,
                disabledContainerColor = buttonBackgroundColor.copy(alpha = 0.5f),
                disabledContentColor = color.contentColor
            ),
        ) {
            if (iconPosition is ButtonIconPosition.Start) {
                Icon(painter = painterResource(iconPosition.iconRes), contentDescription = null, tint = color.contentColor)
                HorizonSpace(SpaceSize.SPACE_4)
            }
            Text(text = label, style = height.textStyle, color = color.contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (iconPosition is ButtonIconPosition.End) {
                HorizonSpace(SpaceSize.SPACE_4)
                Icon(painter = painterResource(iconPosition.iconRes), contentDescription = null, tint = color.contentColor)
            }
        }
        badge?.let {
            Box(modifier = Modifier.offset(x = 4.dp, y = (-4).dp)) {
                it()
            }
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonBlackPreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.Black)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonInversePreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.Inverse)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonAiPreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.Ai)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonWhiteWithOutlinePreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.WhiteWithOutline)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonBlackOutlinePreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.BlackOutline)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonDangerPreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.Danger)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonGhostPreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.Ghost)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonInstitutionPreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.Institution)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonBeigePreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(ButtonColor.Beige)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, heightDp = 800)
private fun ButtonCustomPreviews() {
    ContextKeeper.appContext = LocalContext.current
    ButtonPreview(
        ButtonColor.Custom(
            backgroundColor = HorizonColors.PrimitivesBeige.beige15(),
            contentColor = HorizonColors.Surface.institution(),
        )
    )
}

@Composable
private fun ButtonPreview(color: ButtonColor) {
    ContextKeeper.appContext = LocalContext.current
    val heights = ButtonHeight.entries.toTypedArray()
    val widths = ButtonWidth.entries.toTypedArray()
    val iconPositions = listOf(
        ButtonIconPosition.NoIcon,
        ButtonIconPosition.Start(iconRes = R.drawable.add),
        ButtonIconPosition.End(iconRes = R.drawable.add)
    )

    Column {
        heights.forEach { height ->
            widths.forEach { width ->
                iconPositions.forEach { iconPosition ->
                    Text(text = "${height.name} ${width.name} ${color.javaClass.simpleName} ${iconPosition::class.simpleName}")
                    HorizonSpace(SpaceSize.SPACE_2)
                    Button(
                        label = "Button",
                        height = height,
                        width = width,
                        color = color,
                        iconPosition = iconPosition,
                        badge = {
                            if (iconPosition is ButtonIconPosition.NoIcon) Badge(content = BadgeContent.Text("5"), type = BadgeType.Primary)
                        }
                    )
                    HorizonSpace(SpaceSize.SPACE_8)
                }
            }
        }
    }
}