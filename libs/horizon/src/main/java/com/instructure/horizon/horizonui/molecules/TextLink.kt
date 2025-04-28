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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

enum class TextLinkSize(val iconSize: Dp, val textStyle: TextStyle) {
    SMALL(20.dp, HorizonTypography.buttonTextMedium),
    NORMAL(24.dp, HorizonTypography.buttonTextLarge),
}

sealed class TextLinkColor(
    open val textColor: Color,
) {
    data object Institution : TextLinkColor(HorizonColors.Surface.institution())
    data object Black : TextLinkColor(HorizonColors.Surface.inverseSecondary())
    data object Inverse : TextLinkColor(HorizonColors.Surface.pageSecondary())
    data object Beige : TextLinkColor(HorizonColors.Surface.pagePrimary())
    data class Custom(override val textColor: Color) : TextLinkColor(textColor)
}

sealed class TextLinkIconPosition(@DrawableRes open val iconRes: Int? = null) {
    data object NoIcon : TextLinkIconPosition()
    data class Start(@DrawableRes override val iconRes: Int) : TextLinkIconPosition(iconRes)
    data class End(@DrawableRes override val iconRes: Int) : TextLinkIconPosition(iconRes)
}

@Composable
fun TextLink(
    text: String,
    modifier: Modifier = Modifier,
    textLinkColor: TextLinkColor = TextLinkColor.Institution,
    textLinkSize: TextLinkSize = TextLinkSize.NORMAL,
    textLinkIconPosition: TextLinkIconPosition = TextLinkIconPosition.NoIcon,
    onClick: () -> Unit,
) {
    Row(modifier = modifier.clickable {
        onClick()
    }, verticalAlignment = Alignment.CenterVertically) {
        if (textLinkIconPosition is TextLinkIconPosition.Start) {
            Icon(
                painter = painterResource(id = textLinkIconPosition.iconRes),
                contentDescription = null,
                tint = textLinkColor.textColor,
                modifier = Modifier.size(textLinkSize.iconSize)
            )
            HorizonSpace(SpaceSize.SPACE_4)
        }
        Text(text = text, style = textLinkSize.textStyle.copy(textDecoration = TextDecoration.Underline), color = textLinkColor.textColor)
        if (textLinkIconPosition is TextLinkIconPosition.End) {
            HorizonSpace(SpaceSize.SPACE_4)
            Icon(
                painter = painterResource(id = textLinkIconPosition.iconRes),
                contentDescription = null,
                tint = textLinkColor.textColor,
                modifier = Modifier.size(textLinkSize.iconSize)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TextLinkPreviewSmallNoIcon() {
    ContextKeeper.appContext = LocalContext.current
    TextLink(
        text = "Small - No Icon",
        textLinkSize = TextLinkSize.SMALL,
        textLinkIconPosition = TextLinkIconPosition.NoIcon,
        onClick = {},
        textLinkColor = TextLinkColor.Institution
    )
}

@Composable
@Preview(showBackground = true)
fun TextLinkPreviewSmallStartIcon() {
    ContextKeeper.appContext = LocalContext.current
    TextLink(
        text = "Small - Start Icon",
        textLinkSize = TextLinkSize.SMALL,
        textLinkIconPosition = TextLinkIconPosition.Start(R.drawable.add),
        onClick = {},
        textLinkColor = TextLinkColor.Institution
    )
}

@Composable
@Preview(showBackground = true)
fun TextLinkPreviewSmallEndIcon() {
    ContextKeeper.appContext = LocalContext.current
    TextLink(
        text = "Small - End Icon",
        textLinkSize = TextLinkSize.SMALL,
        textLinkIconPosition = TextLinkIconPosition.End(R.drawable.add),
        onClick = {},
        textLinkColor = TextLinkColor.Institution
    )
}

@Composable
@Preview(showBackground = true)
fun TextLinkPreviewNormalNoIcon() {
    ContextKeeper.appContext = LocalContext.current
    TextLink(
        text = "Normal - No Icon",
        textLinkSize = TextLinkSize.NORMAL,
        textLinkIconPosition = TextLinkIconPosition.NoIcon,
        onClick = {},
        textLinkColor = TextLinkColor.Institution
    )
}

@Composable
@Preview(showBackground = true)
fun TextLinkPreviewNormalStartIcon() {
    ContextKeeper.appContext = LocalContext.current
    TextLink(
        text = "Normal - Start Icon",
        textLinkSize = TextLinkSize.NORMAL,
        textLinkIconPosition = TextLinkIconPosition.Start(R.drawable.add),
        onClick = {},
        textLinkColor = TextLinkColor.Institution
    )
}

@Composable
@Preview(showBackground = true)
fun TextLinkPreviewNormalEndIcon() {
    ContextKeeper.appContext = LocalContext.current
    TextLink(
        text = "Normal - End Icon",
        textLinkSize = TextLinkSize.NORMAL,
        textLinkIconPosition = TextLinkIconPosition.End(R.drawable.add),
        onClick = {},
        textLinkColor = TextLinkColor.Institution
    )
}