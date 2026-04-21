/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.instui.compose.indicator

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.component.InstUIIcon
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.Check
import com.instructure.instui.token.icon.line.Warning
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Token-based icon sizes from the InstUI design system.
 */
enum class IconSize(val dp: Dp) {
    XSmall(InstUIIcon.sizeXs),
    Small(InstUIIcon.sizeSm),
    Medium(InstUIIcon.sizeMd),
    Large(InstUIIcon.sizeLg),
    XLarge(InstUIIcon.sizeXl),
    XXLarge(InstUIIcon.size2xl),
}

/**
 * Token-based icon colors from the InstUI design system.
 */
enum class IconColor(val color: @Composable () -> Color) {
    Base({ InstUIIcon.baseColor }),
    Muted({ InstUIIcon.mutedColor }),
    Success({ InstUIIcon.successColor }),
    Error({ InstUIIcon.errorColor }),
    Warning({ InstUIIcon.warningColor }),
    Info({ InstUIIcon.infoColor }),
    OnColor({ InstUIIcon.onColor }),
    Inverse({ InstUIIcon.inverseColor }),
}

/**
 * InstUI icon component.
 *
 * Renders an icon with token-based sizing and color.
 *
 * Usage:
 * ```
 * Icon(imageVector = InstUIIcons.Line.Check)
 * Icon(imageVector = InstUIIcons.Line.Warning, color = IconColor.Error, size = IconSize.Small)
 * Icon(imageVector = InstUIIcons.Line.Lock, tint = courseColor)
 * ```
 */
@Composable
fun Icon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: IconSize = IconSize.Large,
    color: IconColor = IconColor.Base,
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size.dp),
        tint = color.color(),
    )
}

/**
 * InstUI icon component with explicit tint color.
 *
 * Use this overload when you need a color not in [IconColor],
 * such as a dynamic course color or accent color.
 */
@Composable
fun Icon(
    imageVector: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: IconSize = IconSize.Large,
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size.dp),
        tint = tint,
    )
}

// region Previews

@Preview(name = "Icon Sizes — Light", showBackground = true)
@Preview(name = "Icon Sizes — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconSizesPreview() {
    InstUITheme {
        Row(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(InstUIIcons.Line.Check, size = IconSize.XSmall)
            Icon(InstUIIcons.Line.Check, size = IconSize.Small)
            Icon(InstUIIcons.Line.Check, size = IconSize.Medium)
            Icon(InstUIIcons.Line.Check, size = IconSize.Large)
            Icon(InstUIIcons.Line.Check, size = IconSize.XLarge)
            Icon(InstUIIcons.Line.Check, size = IconSize.XXLarge)
        }
    }
}

@Preview(name = "Icon Colors — Light", showBackground = true)
@Preview(name = "Icon Colors — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconColorsPreview() {
    InstUITheme {
        Row(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(InstUIIcons.Line.Warning, color = IconColor.Base)
            Icon(InstUIIcons.Line.Warning, color = IconColor.Error)
            Icon(InstUIIcons.Line.Warning, color = IconColor.Warning)
            Icon(InstUIIcons.Line.Warning, color = IconColor.Success)
            Icon(InstUIIcons.Line.Warning, color = IconColor.Info)
            Icon(InstUIIcons.Line.Warning, color = IconColor.Muted)
        }
    }
}

// endregion