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

package com.instructure.instui.compose.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIToggleDetails
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.ArrowOpenDown
import com.instructure.instui.token.icon.line.MiniArrowDown
import com.instructure.instui.token.primitives.InstUIColors
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

private val SectionHeaderTextStyle = TextStyle(
    fontFamily = InstUIToggleDetails.fontFamily,
    fontWeight = InstUIToggleDetails.fontWeight,
    fontSize = InstUIToggleDetails.fontSizeSmall,
)

/**
 * InstUI collapsible section header.
 *
 * Displays a label with a built-in chevron icon that rotates based on
 * expand state. Maps to the Figma SectionHeader component.
 *
 * Usage:
 * ```
 * SectionHeader(
 *     label = "Overdue Assignments",
 *     expanded = isExpanded,
 *     onClick = { isExpanded = !isExpanded },
 * )
 * ```
 */
@Composable
fun SectionHeader(
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(InstUISemanticColors.Background.base())
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
            ),
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = SectionHeaderTextStyle,
            color = InstUIToggleDetails.textColor,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = InstUIIcons.Line.ArrowOpenDown,
            size = IconSize.Medium,
            modifier = Modifier.rotate(if (expanded) 180f else 0f),
        )
    }
}

// region Previews

@Preview(name = "SectionHeader — Light", showBackground = true)
@Preview(name = "SectionHeader — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SectionHeaderPreview() {
    InstUITheme {
        Column(modifier = Modifier.background(InstUISemanticColors.Background.base())) {
            SectionHeader(label = "Overdue Assignments", expanded = true, onClick = {})
            Separator()
            SectionHeader(label = "Upcoming Assignments", expanded = false, onClick = {})
        }
    }
}

// endregion