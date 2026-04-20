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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.instructure.instui.token.icon.line.Quiz
import com.instructure.instui.token.icon.line.Warning
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI list item with leading, content, and trailing slots.
 *
 * Follows the Figma ListItem component structure with explicit content slots:
 * - [leading]: Icon, avatar, checkbox, or radio
 * - [supportingText]: Small text above the title (e.g., course name, category)
 * - [title]: Primary text (required)
 * - [subtext1]: First secondary text line below the title (e.g., due date)
 * - [subtext2]: Second secondary text line (optional)
 * - [pill]: Status pill (e.g., Missing, Submitted)
 * - [score]: Score or data below the pill (e.g., "-/100")
 * - [trailing]: Data text, icon, switch, or accordion chevron
 *
 * Usage:
 * ```
 * // Complex (Grades)
 * ListItem(
 *     title = "Assignment name",
 *     subtext1 = "Due Oct 3, 2023 9:41",
 *     leading = { Icon(InstUIIcons.Line.Assignment, ...) },
 *     pill = { Pill(text = "Missing", variant = PillVariant.Error) },
 *     score = { Text("-/100", color = courseColor) },
 * )
 *
 * // Simple
 * ListItem(
 *     title = "Title",
 *     subtext1 = "Text",
 *     trailing = { Text("Data") },
 * )
 * ```
 */
@Composable
fun ListItem(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = InstUIHeading.titleCardMini,
    supportingText: String? = null,
    subtext1: String? = null,
    subtext2: String? = null,
    pill: (@Composable () -> Unit)? = null,
    score: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
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
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = InstUITextTokens.contentSmall,
                    color = InstUITextTokens.mutedColor,
                )
            }
            Text(
                text = title,
                style = titleStyle,
                color = InstUISemanticColors.Text.base(),
            )
            if (subtext1 != null) {
                Text(
                    text = subtext1,
                    style = InstUITextTokens.contentSmall,
                    color = InstUITextTokens.mutedColor,
                )
            }
            if (subtext2 != null) {
                Text(
                    text = subtext2,
                    style = InstUITextTokens.contentSmall,
                    color = InstUITextTokens.mutedColor,
                )
            }
            if (pill != null) {
                pill()
            }
            if (score != null) {
                score()
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

// region Previews

@Preview(name = "ListItem Complex — Light", showBackground = true)
@Preview(
    name = "ListItem Complex — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ListItemComplexPreview() {
    InstUITheme {
        Column(
            modifier = Modifier.background(InstUISemanticColors.Background.base())
        ) {
            ListItem(
                title = "Assignment name",
                supportingText = "Supporting Text",
                subtext1 = "Subtext 1",
                subtext2 = "Subtext 2",
                leading = {
                    Icon(
                        InstUIIcons.Line.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = InstUISemanticColors.Icon.base(),
                    )
                },
                pill = {
                    Pill(text = "Missing", variant = PillVariant.Error, icon = InstUIIcons.Line.Warning)
                },
                score = {
                    Text(
                        text = "-/100",
                        style = InstUITextTokens.contentImportant,
                        color = Color(0xFF00828E),
                    )
                },
                trailing = {
                    Text(
                        text = "Data",
                        style = InstUITextTokens.contentSmall,
                        color = InstUITextTokens.mutedColor,
                    )
                },
            )
        }
    }
}

@Preview(name = "ListItem Simple — Light", showBackground = true)
@Preview(
    name = "ListItem Simple — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ListItemSimplePreview() {
    InstUITheme {
        Column(
            modifier = Modifier.background(InstUISemanticColors.Background.base())
        ) {
            ListItem(
                title = "Title",
                subtext1 = "Text",
                subtext2 = "Subtext 1",
                leading = {
                    Icon(
                        InstUIIcons.Line.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = InstUISemanticColors.Icon.base(),
                    )
                },
                trailing = {
                    Text(
                        text = "Data",
                        style = InstUITextTokens.contentSmall,
                        color = InstUITextTokens.mutedColor,
                    )
                },
            )
        }
    }
}

// endregion