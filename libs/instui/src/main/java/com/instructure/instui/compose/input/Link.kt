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

package com.instructure.instui.compose.input

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUILink
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.icon.externalLink
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.TextButton as M3TextButton

/**
 * Size variants for [Link], each resolving its font size and text-icon gap from [InstUILink] tokens.
 * The icon size is scaled to the text, since InstUI has no per-size link icon token.
 */
enum class LinkSize(val fontSize: TextUnit, val gap: Dp, val iconSize: IconSize) {
    Small(InstUILink.fontSizeSm, InstUILink.gapSm, IconSize.XSmall),
    Medium(InstUILink.fontSizeMd, InstUILink.gapMd, IconSize.Small),
    Large(InstUILink.fontSizeLg, InstUILink.gapLg, IconSize.Medium),
}

/**
 * Which side of the label [Link] renders its icon on.
 */
enum class LinkIconPosition {
    Leading, Trailing
}

/**
 * InstUI link - a text-styled action rendered in the interactive link color.
 *
 * Backed by a real button, so it gets a rounded, padded ripple, a full-size touch target and button
 * accessibility semantics; use it for standalone affordances ("Show all", "Show less", "View
 * details") as well as for links inside a paragraph ([isWithinText], which adds the underline
 * [InstUILink] specifies for in-text links only).
 *
 * Usage:
 * ```
 * Link(text = "Show all", onClick = { ... }, icon = InstUIIcons.chevronDown, iconPosition = LinkIconPosition.Trailing)
 * Link(text = "Read the syllabus", onClick = { ... }, isWithinText = true)
 * Link(text = "Continue", onClick = { ... }, color = InstUILink.onColorTextColor)  // over a filled background
 * ```
 *
 * @param color the link color; defaults to [InstUILink.textColor]. Use [InstUILink.onColorTextColor]
 * over a filled/branded background, or [InstUILink.unstyledTextColor] for an unstyled link.
 * @param iconModifier applied to [icon] only - e.g. `Modifier.rotate(...)` to animate a chevron
 * between its collapsed and expanded direction.
 */
@Composable
fun Link(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: LinkSize = LinkSize.Medium,
    color: Color = InstUILink.textColor,
    disabledColor: Color = InstUILink.textDisabledColor,
    isWithinText: Boolean = false,
    icon: ImageVector? = null,
    iconPosition: LinkIconPosition = LinkIconPosition.Leading,
    iconModifier: Modifier = Modifier,
) {
    val contentColor = if (enabled) color else disabledColor
    M3TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            disabledContentColor = disabledColor,
        ),
    ) {
        if (icon != null && iconPosition == LinkIconPosition.Leading) {
            Icon(imageVector = icon, tint = contentColor, size = size.iconSize, modifier = iconModifier)
            Spacer(modifier = Modifier.size(size.gap))
        }
        Text(
            text = text,
            // InstUILink's own font size and (medium/500) weight over the shared content font family
            // and line height. Deliberately NOT InstUILink.InlineLink.*, whose presets carry the
            // heavier Body.strong weight rather than the component's own FontWeight token.
            style = InstUIText.contentImportant.copy(
                fontSize = size.fontSize,
                fontWeight = InstUILink.fontWeight,
                textDecoration = if (isWithinText) TextDecoration.Underline else TextDecoration.None,
            ),
            color = contentColor,
        )
        if (icon != null && iconPosition == LinkIconPosition.Trailing) {
            Spacer(modifier = Modifier.size(size.gap))
            Icon(imageVector = icon, tint = contentColor, size = size.iconSize, modifier = iconModifier)
        }
    }
}

@Preview(name = "Link — Light", showBackground = true)
@Preview(name = "Link — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LinkPreview() {
    InstUITheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            Link(text = "Show all", onClick = {}, icon = InstUIIcons.chevronDown, iconPosition = LinkIconPosition.Trailing)
            Link(text = "In text", onClick = {}, isWithinText = true)
            Link(text = "Open", onClick = {}, icon = InstUIIcons.externalLink, size = LinkSize.Small)
            Link(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}
