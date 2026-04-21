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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.indicator.Pill
import com.instructure.instui.compose.indicator.PillVariant
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.ArrowOpenDown
import com.instructure.instui.token.icon.line.Assignment
import com.instructure.instui.token.icon.line.Quiz
import com.instructure.instui.token.icon.line.Warning
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

// ---------------------------------------------------------------------------
// Leading variants (maps to Figma ListItem.Leading modes)
// ---------------------------------------------------------------------------

/**
 * Leading content for [ListItem].
 *
 * Maps to Figma ListItem.Leading component with 4 modes:
 * Icon (24dp), Radio (24dp), Checkbox (24dp), Avatar (40dp).
 */
sealed class ListItemLeading {
    data class Icon(
        val imageVector: ImageVector,
        val color: IconColor = IconColor.Base,
    ) : ListItemLeading()

    data class Radio(
        val selected: Boolean,
        val onClick: () -> Unit,
    ) : ListItemLeading()

    data class Checkbox(
        val checked: Boolean,
        val onClick: () -> Unit,
    ) : ListItemLeading()

    data class Avatar(
        val content: @Composable () -> Unit,
    ) : ListItemLeading()
}

// ---------------------------------------------------------------------------
// Trailing variants (maps to Figma ListItem.Tailing modes)
// ---------------------------------------------------------------------------

/**
 * Trailing content for [ListItem].
 *
 * Maps to Figma ListItem.Tailing component with 5 modes:
 * Icons, Checkbox, Switch, TextOnly, Accordion.
 */
sealed class ListItemTrailing {
    data class Icons(
        val icons: List<ImageVector>,
        val data: String? = null,
        val value: String? = null,
    ) : ListItemTrailing()

    data class Checkbox(
        val checked: Boolean,
        val onClick: () -> Unit,
    ) : ListItemTrailing()

    data class Switch(
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : ListItemTrailing()

    data class TextOnly(
        val data: String? = null,
        val value: String? = null,
    ) : ListItemTrailing()

    data class Accordion(
        val expanded: Boolean,
        val onClick: () -> Unit,
    ) : ListItemTrailing()
}

// ---------------------------------------------------------------------------
// ListItem composable
// ---------------------------------------------------------------------------

/**
 * InstUI list item matching the Figma ListItem component structure.
 *
 * Content slots (all optional except [title]):
 * - [supportingText]: Small text above the title
 * - [title]: Primary heading text (required)
 * - [text]: Body text below the title (regular weight, base color)
 * - [subtext1]: First secondary text line (small, muted)
 * - [subtext2]: Second secondary text line (small, muted)
 * - [pill]: Status pill composable
 * - [score]: Score composable (e.g., "-/100" in course color)
 *
 * Usage:
 * ```
 * // Complex with icon leading and text trailing
 * ListItem(
 *     title = "Assignment name",
 *     subtext1 = "Due Oct 3, 2023 9:41",
 *     leading = ListItemLeading.Icon(InstUIIcons.Line.Assignment),
 *     trailing = ListItemTrailing.TextOnly(data = "90/100"),
 *     pill = { Pill(text = "Missing", variant = PillVariant.Error) },
 * )
 *
 * // With accordion trailing
 * ListItem(
 *     title = "Section",
 *     trailing = ListItemTrailing.Accordion(expanded = true, onClick = {}),
 * )
 *
 * // With avatar leading
 * ListItem(
 *     title = "Student Name",
 *     leading = ListItemLeading.Avatar { AsyncImage(url) },
 * )
 * ```
 */
@Composable
fun ListItem(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    text: String? = null,
    subtext1: String? = null,
    subtext2: String? = null,
    pill: (@Composable () -> Unit)? = null,
    score: (@Composable () -> Unit)? = null,
    leading: ListItemLeading? = null,
    trailing: ListItemTrailing? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        verticalAlignment = Alignment.Top,
    ) {
        if (leading != null) {
            LeadingContent(leading)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.Space2xs.space2xs),
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
                style = InstUIHeading.titleCardMini,
                color = InstUISemanticColors.Text.base(),
            )
            if (text != null) {
                Text(
                    text = text,
                    style = InstUITextTokens.content,
                    color = InstUISemanticColors.Text.base(),
                )
            }
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
            TrailingContent(trailing)
        }
    }
}

// ---------------------------------------------------------------------------
// Internal rendering
// ---------------------------------------------------------------------------

@Composable
private fun LeadingContent(leading: ListItemLeading) {
    when (leading) {
        is ListItemLeading.Icon -> {
            Icon(
                imageVector = leading.imageVector,
                color = leading.color,
            )
        }
        is ListItemLeading.Radio -> {
            RadioButton(
                selected = leading.selected,
                onClick = leading.onClick,
                modifier = Modifier.size(24.dp),
            )
        }
        is ListItemLeading.Checkbox -> {
            Checkbox(
                checked = leading.checked,
                onCheckedChange = { leading.onClick() },
                modifier = Modifier.size(24.dp),
            )
        }
        is ListItemLeading.Avatar -> {
            leading.content()
        }
    }
}

@Composable
private fun TrailingContent(trailing: ListItemTrailing) {
    when (trailing) {
        is ListItemTrailing.Icons -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
            ) {
                if (trailing.data != null) {
                    Text(
                        text = trailing.data,
                        style = InstUITextTokens.contentSmall,
                        color = InstUITextTokens.mutedColor,
                    )
                }
                if (trailing.value != null) {
                    Text(
                        text = trailing.value,
                        style = InstUIHeading.titleCardMini,
                        color = InstUISemanticColors.Text.base(),
                    )
                }
                for (icon in trailing.icons) {
                    Icon(imageVector = icon)
                }
            }
        }
        is ListItemTrailing.Checkbox -> {
            Checkbox(
                checked = trailing.checked,
                onCheckedChange = { trailing.onClick() },
                modifier = Modifier.size(24.dp),
            )
        }
        is ListItemTrailing.Switch -> {
            Switch(
                checked = trailing.checked,
                onCheckedChange = trailing.onCheckedChange,
            )
        }
        is ListItemTrailing.TextOnly -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
            ) {
                if (trailing.data != null) {
                    Text(
                        text = trailing.data,
                        style = InstUITextTokens.contentSmall,
                        color = InstUITextTokens.mutedColor,
                    )
                }
                if (trailing.value != null) {
                    Text(
                        text = trailing.value,
                        style = InstUIHeading.titleCardMini,
                        color = InstUISemanticColors.Text.base(),
                    )
                }
            }
        }
        is ListItemTrailing.Accordion -> {
            Icon(
                imageVector = InstUIIcons.Line.ArrowOpenDown,
                size = IconSize.Small,
            )
        }
    }
}

// region Previews

@Preview(name = "ListItem Complex — Light", showBackground = true)
@Preview(name = "ListItem Complex — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListItemComplexPreview() {
    InstUITheme {
        Column(modifier = Modifier.background(InstUISemanticColors.Background.base())) {
            ListItem(
                title = "Assignment name",
                supportingText = "Supporting Text",
                text = "Text",
                subtext1 = "Subtext 1",
                subtext2 = "Subtext 2",
                leading = ListItemLeading.Icon(InstUIIcons.Line.Assignment),
                pill = { Pill(text = "Missing", variant = PillVariant.Error, icon = InstUIIcons.Line.Warning) },
                score = {
                    Text(text = "-/100", style = InstUITextTokens.contentImportant, color = Color(0xFF00828E))
                },
                trailing = ListItemTrailing.TextOnly(data = "Data"),
            )
        }
    }
}

@Preview(name = "ListItem Simple — Light", showBackground = true)
@Preview(name = "ListItem Simple — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListItemSimplePreview() {
    InstUITheme {
        Column(modifier = Modifier.background(InstUISemanticColors.Background.base())) {
            ListItem(
                title = "Title",
                text = "Text",
                subtext1 = "Subtext 1",
                leading = ListItemLeading.Icon(InstUIIcons.Line.Quiz),
                trailing = ListItemTrailing.TextOnly(data = "Data"),
            )
        }
    }
}

@Preview(name = "ListItem Trailing Modes — Light", showBackground = true)
@Preview(name = "ListItem Trailing Modes — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListItemTrailingModesPreview() {
    InstUITheme {
        Column(modifier = Modifier.background(InstUISemanticColors.Background.base())) {
            ListItem(
                title = "With Accordion",
                trailing = ListItemTrailing.Accordion(expanded = false, onClick = {}),
            )
            Separator()
            ListItem(
                title = "With Switch",
                trailing = ListItemTrailing.Switch(checked = true, onCheckedChange = {}),
            )
            Separator()
            ListItem(
                title = "With Checkbox",
                trailing = ListItemTrailing.Checkbox(checked = true, onClick = {}),
            )
            Separator()
            ListItem(
                title = "With Icons",
                trailing = ListItemTrailing.Icons(
                    icons = listOf(InstUIIcons.Line.Assignment, InstUIIcons.Line.Quiz),
                    data = "Data",
                ),
            )
        }
    }
}

@Preview(name = "ListItem Leading Modes — Light", showBackground = true)
@Preview(name = "ListItem Leading Modes — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListItemLeadingModesPreview() {
    InstUITheme {
        Column(modifier = Modifier.background(InstUISemanticColors.Background.base())) {
            ListItem(
                title = "Icon leading",
                leading = ListItemLeading.Icon(InstUIIcons.Line.Assignment),
            )
            Separator()
            ListItem(
                title = "Radio leading",
                leading = ListItemLeading.Radio(selected = true, onClick = {}),
            )
            Separator()
            ListItem(
                title = "Checkbox leading",
                leading = ListItemLeading.Checkbox(checked = false, onClick = {}),
            )
            Separator()
            ListItem(
                title = "Avatar leading",
                leading = ListItemLeading.Avatar {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(InstUISemanticColors.Background.muted())
                    )
                },
            )
        }
    }
}

// endregion