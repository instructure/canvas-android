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

package com.instructure.instui.compose.container

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUIToggleDetails
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.icon.chevronUp
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI collapsible "toggle details" section (accordion).
 *
 * Renders a clickable header with a title and a chevron that expands/collapses [content].
 *
 * Usage:
 * ```
 * ToggleDetails(title = "Instructions", expanded = expanded, onExpandToggle = { expanded = !expanded }) {
 *     Text(text = "...")
 * }
 * ```
 */
@Composable
fun ToggleDetails(
    title: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle() }
                .padding(vertical = InstUIToggleDetails.togglePadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InstUIToggleDetails.iconMargin),
        ) {
            Text(
                text = title,
                style = InstUIText.contentImportant,
                color = InstUIToggleDetails.textColor,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) InstUIIcons.chevronUp else InstUIIcons.chevronDown,
                tint = InstUIToggleDetails.textColor,
                size = IconSize.Small,
            )
        }
        if (expanded) {
            Column(modifier = Modifier.padding(top = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)) {
                content()
            }
        }
    }
}

@Preview(name = "ToggleDetails — Light", showBackground = true)
@Preview(name = "ToggleDetails — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ToggleDetailsPreview() {
    InstUITheme {
        var expanded by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            ToggleDetails(
                title = "Instructions",
                expanded = expanded,
                onExpandToggle = { expanded = !expanded },
            ) {
                Text(text = "Read the prompt carefully and submit your work before the due date.")
            }
        }
    }
}
