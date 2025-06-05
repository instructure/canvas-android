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
@file:OptIn(ExperimentalGlideComposeApi::class)

package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

sealed class AvatarType {
    data class Picture(val pictureUrl: String) : AvatarType()
    data class Initials(val initials: String) : AvatarType()
    data class Icon(@DrawableRes val iconRes: Int = R.drawable.person) : AvatarType()
}

enum class AvatarSize(val size: Dp, val iconSize: Dp, val initialsTextStyle: TextStyle) {
    XXLARGE(80.dp, 48.dp, HorizonTypography.h1),
    XLARGE(64.dp, 32.dp, HorizonTypography.h1),
    LARGE(56.dp, 32.dp, HorizonTypography.h3),
    MEDUIM(48.dp, 24.dp, HorizonTypography.h3),
    SMALL(40.dp, 24.dp, HorizonTypography.labelLargeBold),
    XSMALL(32.dp, 16.dp, HorizonTypography.labelMediumBold),
    XXSMALL(24.dp, 16.dp, HorizonTypography.labelMediumBold)
}

enum class AvatarColor(val backgroundColor: Color, val contentColor: Color, val borderColor: Color) {
    NORMAL(HorizonColors.Surface.cardPrimary(), HorizonColors.Surface.institution(), HorizonColors.Surface.divider()),
    INVERSE(HorizonColors.Surface.institution(), HorizonColors.Surface.cardPrimary(), Color.Transparent)
}

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    type: AvatarType = AvatarType.Icon(),
    size: AvatarSize = AvatarSize.LARGE,
    color: AvatarColor = AvatarColor.NORMAL
) {
    var avatarModifier = modifier
        .background(
            color = color.backgroundColor,
            shape = CircleShape
        )
    if (type !is AvatarType.Picture) avatarModifier =
        avatarModifier.border(HorizonBorder.level2(color.borderColor), shape = CircleShape)
    avatarModifier = avatarModifier.size(size.size)
    Box(
        contentAlignment = Alignment.Center,
        modifier = avatarModifier
    ) {
        when (type) {
            is AvatarType.Icon -> {
                Icon(
                    painterResource(id = type.iconRes),
                    contentDescription = stringResource(R.string.avatar_contentDescription),
                    modifier = Modifier.size(size.iconSize),
                    tint = color.contentColor
                )
            }

            is AvatarType.Initials -> {
                Text(
                    text = type.initials.uppercase(),
                    color = color.contentColor,
                    style = size.initialsTextStyle,
                )
            }

            is AvatarType.Picture -> {
                GlideImage(
                    model = type.pictureUrl,
                    contentDescription = stringResource(R.string.avatar_contentDescription),
                    modifier = Modifier
                        .size(size.size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
@Preview(name = "Avatar - Icon - NORMAL - XXLARGE")
private fun AvatarIconNormalXXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XXLARGE, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Icon - NORMAL - XLARGE")
private fun AvatarIconNormalXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XLARGE, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Icon - NORMAL - LARGE")
private fun AvatarIconNormalLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.LARGE, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Icon - NORMAL - MEDIUM")
private fun AvatarIconNormalMediumPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.MEDUIM, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Icon - NORMAL - SMALL")
private fun AvatarIconNormalSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.SMALL, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Icon - NORMAL - XSMALL")
private fun AvatarIconNormalXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XSMALL, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Icon - NORMAL - XXSMALL")
private fun AvatarIconNormalXXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XXSMALL, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Initials - NORMAL - XXLARGE")
private fun AvatarInitialsNormalXXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XXLARGE, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Initials - NORMAL - XLARGE")
private fun AvatarInitialsNormalXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XLARGE, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Initials - NORMAL - LARGE")
private fun AvatarInitialsNormalLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.LARGE, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Initials - NORMAL - MEDIUM")
private fun AvatarInitialsNormalMediumPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.MEDUIM, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Initials - NORMAL - SMALL")
private fun AvatarInitialsNormalSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.SMALL, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Initials - NORMAL - XSMALL")
private fun AvatarInitialsNormalXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XSMALL, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Initials - NORMAL - XXSMALL")
private fun AvatarInitialsNormalXXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XXSMALL, color = AvatarColor.NORMAL)
}

@Composable
@Preview(name = "Avatar - Icon - INVERSE - XXLARGE")
private fun AvatarIconInverseXXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XXLARGE, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Icon - INVERSE - XLARGE")
private fun AvatarIconInverseXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XLARGE, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Icon - INVERSE - LARGE")
private fun AvatarIconInverseLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.LARGE, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Icon - INVERSE - MEDIUM")
private fun AvatarIconInverseMediumPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.MEDUIM, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Icon - INVERSE - SMALL")
private fun AvatarIconInverseSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.SMALL, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Icon - INVERSE - XSMALL")
private fun AvatarIconInverseXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XSMALL, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Icon - INVERSE - XXSMALL")
private fun AvatarIconInverseXXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Icon(), size = AvatarSize.XXSMALL, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Initials - INVERSE - XXLARGE")
private fun AvatarInitialsInverseXXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XXLARGE, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Initials - INVERSE - XLARGE")
private fun AvatarInitialsInverseXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XLARGE, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Initials - INVERSE - LARGE")
private fun AvatarInitialsInverseLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.LARGE, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Initials - INVERSE - MEDIUM")
private fun AvatarInitialsInverseMediumPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.MEDUIM, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Initials - INVERSE - SMALL")
private fun AvatarInitialsInverseSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.SMALL, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Initials - INVERSE - XSMALL")
private fun AvatarInitialsInverseXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XSMALL, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Initials - INVERSE - XXSMALL")
private fun AvatarInitialsInverseXXSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(type = AvatarType.Initials("AW"), size = AvatarSize.XXSMALL, color = AvatarColor.INVERSE)
}

@Composable
@Preview(name = "Avatar - Picture - NORMAL - XXLARGE")
private fun AvatarPictureNormalXXLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Avatar(
        type = AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        size = AvatarSize.XXLARGE,
        color = AvatarColor.NORMAL
    )
}

@Composable
@Preview(name = "Avatar - Picture - NORMAL - XLARGE")
private fun AvatarPictureNormalXLargePreview() {
    Avatar(
        type = AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        size = AvatarSize.XLARGE,
        color = AvatarColor.NORMAL
    )
}

@Composable
@Preview(name = "Avatar - Picture - NORMAL - LARGE")
private fun AvatarPictureNormalLargePreview() {
    Avatar(
        type = AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        size = AvatarSize.LARGE,
        color = AvatarColor.NORMAL
    )
}

@Composable
@Preview(name = "Avatar - Picture - NORMAL - MEDIUM")
private fun AvatarPictureNormalMediumPreview() {
    Avatar(
        type = AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        size = AvatarSize.MEDUIM,
        color = AvatarColor.NORMAL
    )
}

@Composable
@Preview(name = "Avatar - Picture - NORMAL - SMALL")
private fun AvatarPictureNormalSmallPreview() {
    Avatar(
        type = AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        size = AvatarSize.SMALL,
        color = AvatarColor.NORMAL
    )
}

@Composable
@Preview(name = "Avatar - Picture - NORMAL - XSMALL")
private fun AvatarPictureNormalXSmallPreview() {
    Avatar(
        type = AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        size = AvatarSize.XSMALL,
        color = AvatarColor.NORMAL
    )
}

@Composable
@Preview(name = "Avatar - Picture - NORMAL - XXSMALL")
private fun AvatarPictureNormalXXSmallPreview() {
    Avatar(
        type = AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        size = AvatarSize.XXSMALL,
        color = AvatarColor.NORMAL
    )
}