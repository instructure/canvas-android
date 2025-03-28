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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

sealed class BadgeContent(
    val size: Dp,
    open val text: String? = null,
    open val iconRes: Int? = null,
    open val contentDescription: String? = null
) {
    data object Color : BadgeContent(16.dp)
    data object ColorSmall : BadgeContent(8.dp)
    data class Text(override val text: String) : BadgeContent(19.dp)
    data class Icon(override val iconRes: Int, override val contentDescription: String? = null) : BadgeContent(19.dp)
}

sealed class BadgeType(open val backgroundColor: Color, open val contentColor: Color) {
    data object Primary : BadgeType(HorizonColors.Surface.institution(), HorizonColors.Text.surfaceColored())
    data object PrimaryWhite : BadgeType(HorizonColors.Surface.pageSecondary(), HorizonColors.Text.body())
    data object Danger : BadgeType(HorizonColors.Surface.error(), HorizonColors.Text.surfaceColored())
    data object Success : BadgeType(HorizonColors.Surface.success(), HorizonColors.Text.surfaceColored())
    data object Inverse : BadgeType(HorizonColors.Surface.inversePrimary(), HorizonColors.Text.surfaceColored())
    data class Custom(override val backgroundColor: Color, override val contentColor: Color) : BadgeType(backgroundColor, contentColor)
}

@Composable
fun Badge(modifier: Modifier = Modifier, content: BadgeContent = BadgeContent.Color, type: BadgeType = BadgeType.Primary) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = type.backgroundColor,
                shape = RoundedCornerShape(500.dp)
            )
            .size(content.size)
    ) {
        if (content is BadgeContent.Icon) {
            Icon(
                painterResource(id = content.iconRes),
                contentDescription = content.contentDescription,
                modifier = Modifier.size(17.dp),
                tint = type.contentColor
            )
        } else if (content is BadgeContent.Text) {
            Text(
                text = content.text.uppercase(),
                color = type.contentColor,
                style = HorizonTypography.tag,
            )
        }
    }
}

@Composable
@Preview(name = "Badge - Color - PRIMARY")
private fun BadgeColorPrimaryPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Color, type = BadgeType.Primary)
}

@Composable
@Preview(name = "Badge - ColorSmall - PRIMARY")
private fun BadgeColorSmallPrimaryPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.ColorSmall, type = BadgeType.Primary)
}

@Composable
@Preview(name = "Badge - Text - PRIMARY")
private fun BadgeTextPrimaryPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Text("5"), type = BadgeType.Primary)
}

@Composable
@Preview(name = "Badge - Icon - PRIMARY")
private fun BadgeIconCameraPrimaryPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Icon(R.drawable.check, "Check"), type = BadgeType.Primary)
}

@Composable
@Preview(name = "Badge - Color - PRIMARY_WHITE")
private fun BadgeColorPrimaryWhitePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Color, type = BadgeType.PrimaryWhite)
}

@Composable
@Preview(name = "Badge - ColorSmall - PRIMARY_WHITE")
private fun BadgeColorSmallPrimaryWhitePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.ColorSmall, type = BadgeType.PrimaryWhite)
}

@Composable
@Preview(name = "Badge - Text - PRIMARY_WHITE")
private fun BadgeTextPrimaryWhitePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Text("5"), type = BadgeType.PrimaryWhite)
}

@Composable
@Preview(name = "Badge - Icon - PRIMARY_WHITE")
private fun BadgeIconCameraPrimaryWhitePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Icon(R.drawable.check, "Check"), type = BadgeType.PrimaryWhite)
}

@Composable
@Preview(name = "Badge - Color - DANGER")
private fun BadgeColorDangerPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Color, type = BadgeType.Danger)
}

@Composable
@Preview(name = "Badge - ColorSmall - DANGER")
private fun BadgeColorSmallDangerPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.ColorSmall, type = BadgeType.Danger)
}

@Composable
@Preview(name = "Badge - Text - DANGER")
private fun BadgeTextDangerPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Text("5"), type = BadgeType.Danger)
}

@Composable
@Preview(name = "Badge - Icon - DANGER")
private fun BadgeIconCameraDangerPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Icon(R.drawable.check, "Check"), type = BadgeType.Danger)
}

@Composable
@Preview(name = "Badge - Color - SUCCESS")
private fun BadgeColorSuccessPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Color, type = BadgeType.Success)
}

@Composable
@Preview(name = "Badge - ColorSmall - SUCCESS")
private fun BadgeColorSmallSuccessPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.ColorSmall, type = BadgeType.Success)
}

@Composable
@Preview(name = "Badge - Text - SUCCESS")
private fun BadgeTextSuccessPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Text("5"), type = BadgeType.Success)
}

@Composable
@Preview(name = "Badge - Icon - SUCCESS")
private fun BadgeIconCameraSuccessPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Icon(R.drawable.check, "Check"), type = BadgeType.Success)
}

@Composable
@Preview(name = "Badge - Color - INVERSE")
private fun BadgeColorInversePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Color, type = BadgeType.Inverse)
}

@Composable
@Preview(name = "Badge - ColorSmall - INVERSE")
private fun BadgeColorSmallInversePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.ColorSmall, type = BadgeType.Inverse)
}

@Composable
@Preview(name = "Badge - Text - INVERSE")
private fun BadgeTextInversePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Text("5"), type = BadgeType.Inverse)
}

@Composable
@Preview(name = "Badge - Icon - INVERSE")
private fun BadgeIconCameraInversePreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(content = BadgeContent.Icon(R.drawable.check, "Check"), type = BadgeType.Inverse)
}

@Composable
@Preview(name = "Badge - Color - Custom")
private fun BadgeColorCustomPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(
        content = BadgeContent.Color,
        type = BadgeType.Custom(HorizonColors.PrimitivesBeige.beige15(), HorizonColors.Surface.institution())
    )
}

@Composable
@Preview(name = "Badge - ColorSmall - INVERSE")
private fun BadgeColorSmallCustomPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(
        content = BadgeContent.ColorSmall,
        type = BadgeType.Custom(HorizonColors.PrimitivesBeige.beige15(), HorizonColors.Surface.institution())
    )
}

@Composable
@Preview(name = "Badge - Text - INVERSE")
private fun BadgeTextCustomPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(
        content = BadgeContent.Text("5"),
        type = BadgeType.Custom(HorizonColors.PrimitivesBeige.beige15(), HorizonColors.Surface.institution())
    )
}

@Composable
@Preview(name = "Badge - Icon - INVERSE")
private fun BadgeIconCameraCustomPreview() {
    ContextKeeper.appContext = LocalContext.current
    Badge(
        content = BadgeContent.Icon(R.drawable.check, "Check"),
        type = BadgeType.Custom(HorizonColors.PrimitivesBeige.beige15(), HorizonColors.Surface.institution())
    )
}