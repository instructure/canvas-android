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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.token.component.InstUIToggleDetails
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.ArrowDown
import com.instructure.instui.token.icon.line.DropDown
import com.instructure.instui.token.icon.line.MiniArrowDown
import com.instructure.instui.token.icon.line.MoveDown
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI collapsible section header.
 *
 * Displays a title with an optional trailing element (typically a chevron icon).
 * The caller provides the trailing content — e.g., a chevron from pandares
 * (`ic_chevron_down`) rotated based on expand state.
 *
 * Usage:
 * ```
 * SectionHeader(
 *     title = "Overdue Assignments",
 *     onClick = { isExpanded = !isExpanded },
 *     trailing = {
 *         Icon(
 *             painter = painterResource(R.drawable.ic_chevron_down),
 *             contentDescription = null,
 *             modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
 *         )
 *     },
 * )
 * ```
 */
private val SectionHeaderTextStyle = TextStyle(
    fontFamily = InstUIToggleDetails.fontFamily,
    fontWeight = InstUIToggleDetails.fontWeight,
    fontSize = InstUIToggleDetails.fontSizeSmall,
)

@Composable
fun SectionHeader(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = InstUILayoutSizes.Spacing.Padding.container_sm,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = SectionHeaderTextStyle,
            color = InstUIToggleDetails.textColor,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            trailing()
        }
    }
}

@Preview(name = "SectionHeader — Light", showBackground = true)
@Preview(
    name = "SectionHeader — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SectionHeaderPreview() {
    InstUITheme {
        Column(
            modifier = Modifier.background(InstUISemanticColors.Background.base())
        ) {
            SectionHeader(title = "Overdue Assignments", onClick = {}, trailing = {
                Icon(
                    InstUIIcons.Line.MiniArrowDown,
                    contentDescription = null,
                )
            })
            Separator()
            SectionHeader(title = "Upcoming Assignments", onClick = {}, trailing = {
                Icon(
                    InstUIIcons.Line.MiniArrowDown,
                    contentDescription = null,
                )
            })
        }
    }
}