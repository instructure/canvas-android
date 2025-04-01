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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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

enum class TagType(val cornerRadius: RoundedCornerShape, val backgroundColor: Color) {
    STANDALONE(HorizonCornerRadius.level4, HorizonColors.Surface.pageTertiary()),
    INLINE(HorizonCornerRadius.level1, HorizonColors.Surface.pageSecondary())
}

// Padding values are for the STANDALONE type, INLINE has fixed padding
enum class TagSize(val textStyle: TextStyle, val padding: PaddingValues, val paddingWithIcon: PaddingValues, val iconSize: Dp = 14.dp) {
    LARGE(HorizonTypography.buttonTextLarge, PaddingValues(vertical = 8.dp, horizontal = 12.dp), PaddingValues(18.dp, 8.dp, 12.dp, 8.dp)),
    MEDIUM(HorizonTypography.buttonTextMedium, PaddingValues(vertical = 4.dp, horizontal = 8.dp), PaddingValues(14.dp, 4.dp, 8.dp, 4.dp)),
    SMALL(HorizonTypography.p3, PaddingValues(vertical = 4.dp, horizontal = 8.dp), PaddingValues(12.dp, 4.dp, 8.dp, 4.dp), iconSize = 12.dp)
}

@Composable
fun Tag(
    label: String,
    modifier: Modifier = Modifier,
    type: TagType = TagType.STANDALONE,
    size: TagSize = TagSize.LARGE,
    enabled: Boolean = true,
    dismissible: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val padding = when {
        type == TagType.INLINE -> PaddingValues(vertical = 8.dp, horizontal = 12.dp)
        dismissible -> size.paddingWithIcon
        else -> size.padding
    }
    val badgePadding = if (dismissible && type == TagType.INLINE) PaddingValues(top = 8.dp, end = 8.dp) else PaddingValues()
    Box(contentAlignment = Alignment.TopEnd, modifier = modifier.padding(badgePadding)) {
        Box(
            modifier = Modifier
                .background(
                    color = if (enabled) type.backgroundColor else type.backgroundColor.copy(alpha = 0.5f),
                    shape = type.cornerRadius
                )
                .border(HorizonBorder.level1(), shape = type.cornerRadius)
                .clip(type.cornerRadius)
                .padding(padding)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, style = size.textStyle, color = HorizonColors.Text.body())
                if (dismissible && type == TagType.STANDALONE) {
                    HorizonSpace(size = SpaceSize.SPACE_4)
                    Icon(
                        painter = painterResource(R.drawable.close),
                        tint = HorizonColors.Icon.default(),
                        contentDescription = stringResource(R.string.tag_dismiss),
                        modifier = Modifier
                            .size(size.iconSize)
                            .clickable { onDismiss() })
                }
            }
        }
        if (dismissible && type == TagType.INLINE) {
            Badge(
                content = BadgeContent.Icon(
                    iconRes = R.drawable.close_small,
                    contentDescription = stringResource(R.string.tag_dismiss)
                ),
                modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
            )
        }
    }
}

@Preview
@Composable
private fun TagPreviewStandaloneLargeDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.STANDALONE, size = TagSize.LARGE, dismissible = true)
}

@Preview
@Composable
private fun TagPreviewStandaloneLargeNonDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.STANDALONE, size = TagSize.LARGE, dismissible = false)
}

@Preview
@Composable
private fun TagPreviewStandaloneMediumDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.STANDALONE, size = TagSize.MEDIUM, dismissible = true)
}

@Preview
@Composable
private fun TagPreviewStandaloneMediumNonDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.STANDALONE, size = TagSize.MEDIUM, dismissible = false)
}

@Preview
@Composable
private fun TagPreviewStandaloneSmallDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.STANDALONE, size = TagSize.SMALL, dismissible = true)
}

@Preview
@Composable
private fun TagPreviewStandaloneSmallNonDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.STANDALONE, size = TagSize.SMALL, dismissible = false)
}

@Preview
@Composable
private fun TagPreviewInlineLargeDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.INLINE, size = TagSize.LARGE, dismissible = true)
}

@Preview
@Composable
private fun TagPreviewInlineLargeNonDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.INLINE, size = TagSize.LARGE, dismissible = false)
}

@Preview
@Composable
private fun TagPreviewInlineMediumDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.INLINE, size = TagSize.MEDIUM, dismissible = true)
}

@Preview
@Composable
private fun TagPreviewInlineMediumNonDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.INLINE, size = TagSize.MEDIUM, dismissible = false)
}

@Preview
@Composable
private fun TagPreviewInlineSmallDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.INLINE, size = TagSize.SMALL, dismissible = true)
}

@Preview
@Composable
private fun TagPreviewInlineSmallNonDismissible() {
    ContextKeeper.appContext = LocalContext.current
    TagPreview(type = TagType.INLINE, size = TagSize.SMALL, dismissible = false)
}

@Composable
private fun TagPreview(type: TagType, size: TagSize, dismissible: Boolean) {
    ContextKeeper.appContext = LocalContext.current
    Tag(label = "Preview Tag", type = type, size = size, dismissible = dismissible)
}