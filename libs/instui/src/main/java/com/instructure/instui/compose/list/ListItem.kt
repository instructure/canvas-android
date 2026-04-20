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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.component.InstUIList
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Pill
import com.instructure.instui.compose.indicator.PillVariant
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.Assignment
import com.instructure.instui.token.icon.line.Lock
import com.instructure.instui.token.icon.line.Quiz
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI list item with leading, content, and trailing slots.
 *
 * Follows the Figma ListItem component structure:
 * - [leading]: Icon, avatar, checkbox, or radio (optional)
 * - [title]: Primary text (required)
 * - [subtitle]: Secondary text lines (optional)
 * - [trailing]: Data text, icon, switch, or accordion chevron (optional)
 * - [bottom]: Content below the title/subtitle area (optional, e.g., pills, scores)
 *
 * Usage:
 * ```
 * ListItem(
 *     title = "Assignment name",
 *     subtitle = "Due Oct 3, 2023 9:41",
 *     leading = { Icon(...) },
 *     bottom = { Pill(text = "Missing", variant = PillVariant.Error) },
 * )
 * ```
 */
@Composable
fun ListItem(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = InstUIHeading.titleCardMini,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    bottom: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(
                horizontal = InstUIList.ListItem.spacingSmall,
                vertical = InstUIList.ListItem.spacingXSmall,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier.padding(end = InstUIList.ListItem.spacingXSmall),
                contentAlignment = Alignment.TopCenter,
            ) {
                leading()
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(InstUIList.ListItem.spacingXXXSmall),
        ) {
            Text(
                text = title,
                style = titleStyle,
                color = InstUISemanticColors.Text.base(),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = InstUITextTokens.contentSmall,
                    color = InstUITextTokens.mutedColor,
                )
            }
            if (bottom != null) {
                bottom()
            }
        }

        if (trailing != null) {
            Box(
                modifier = Modifier.padding(start = InstUIList.ListItem.spacingXSmall),
                contentAlignment = Alignment.TopEnd,
            ) {
                trailing()
            }
        }
    }
}

@Preview(name = "ListItem Simple — Light", showBackground = true)
@Preview(name = "ListItem Simple — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListItemSimplePreview() {
    InstUITheme {
        Column(
            modifier = Modifier.background(InstUISemanticColors.Background.base())
        ) {
            ListItem(title = "Assignment name", subtitle = "Due Oct 3, 2023 9:41")
            Separator()
            ListItem(title = "Another assignment", subtitle = "Due Oct 5, 2023 11:59")
        }
    }
}

@Preview(name = "ListItem Complex — Light", showBackground = true)
@Preview(name = "ListItem Complex — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListItemComplexPreview() {
    InstUITheme {
        Column(
            modifier = Modifier.background(InstUISemanticColors.Background.base())
        ) {
            ListItem(
                title = "Assignment name",
                subtitle = "Due Oct 3, 2023 9:41",
                leading = {
                    Icon(
                        InstUIIcons.Line.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = InstUISemanticColors.Icon.base(),
                    )
                },
                bottom = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Pill(text = "Missing", variant = PillVariant.Error)
                        Text(
                            text = "-/100",
                            style = InstUITextTokens.contentSmall,
                            color = InstUISemanticColors.Text.error(),
                        )
                    }
                },
                trailing = {
                    Text(
                        text = "Data",
                        style = InstUITextTokens.contentSmall,
                        color = InstUITextTokens.mutedColor,
                    )
                },
            )
            Separator()
            ListItem(
                title = "Submitted assignment",
                subtitle = "Due Oct 5, 2023 11:59",
                leading = {
                    Icon(
                        InstUIIcons.Line.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = InstUISemanticColors.Icon.base(),
                    )
                },
                bottom = {
                    Pill(text = "Submitted", variant = PillVariant.Success)
                },
            )
        }
    }
}