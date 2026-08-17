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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIBaseButton
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI single-select dropdown.
 *
 * Renders a bordered anchor showing the selected option and a chevron; tapping it opens
 * a dropdown menu of [options].
 *
 * Usage:
 * ```
 * Select(options = attempts, selectedIndex = selected, onSelect = { vm.onAttemptSelected(it) })
 * ```
 */
@Composable
fun Select(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(InstUIBaseButton.borderRadius)
    val selectedText = options.getOrNull(selectedIndex).orEmpty()

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = InstUILayoutSizes.Size.Interactive.height_md)
                .clip(shape)
                .background(color = InstUISemanticColors.Background.Interactive.Input.base(), shape = shape)
                .border(
                    width = InstUILayoutSizes.BorderWidth.Sm.sm,
                    color = InstUISemanticColors.Stroke.Interactive.Input.base(),
                    shape = shape,
                )
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = InstUILayoutSizes.Spacing.Padding.interactive_horizontal_lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        ) {
            Text(
                text = selectedText,
                style = InstUIText.content,
                color = InstUISemanticColors.Text.base(),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Icon(imageVector = InstUIIcons.chevronDown, color = IconColor.Base, size = IconSize.Small)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option, style = InstUIText.content) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(name = "Select — Light", showBackground = true)
@Preview(name = "Select — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SelectPreview() {
    InstUITheme {
        var selected by remember { mutableIntStateOf(0) }
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            Select(
                options = listOf("Attempt 1", "Attempt 2", "Attempt 3"),
                selectedIndex = selected,
                onSelect = { selected = it },
            )
        }
    }
}
