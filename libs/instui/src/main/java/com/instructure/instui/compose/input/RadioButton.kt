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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIRadioInput
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.RadioButton as M3RadioButton

enum class RadioButtonSize(
    val controlSize: @Composable () -> androidx.compose.ui.unit.Dp,
    val fontSize: TextUnit,
) {
    Small({ InstUIRadioInput.controlSizeSm }, InstUIRadioInput.fontSizeSm),
    Medium({ InstUIRadioInput.controlSizeMd }, InstUIRadioInput.fontSizeMd),
    Large({ InstUIRadioInput.controlSizeLg }, InstUIRadioInput.fontSizeLg),
}

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RadioButtonSize = RadioButtonSize.Medium,
    accentColor: Color? = null,
) {
    val selectedColor = accentColor ?: InstUIRadioInput.borderSelectedColor
    val unselectedColor = InstUIRadioInput.borderColor

    val rowModifier = if (onClick != null) {
        Modifier.selectable(
            selected = selected,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
        )
    } else {
        Modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUIRadioInput.gap),
        modifier = modifier
            .fillMaxWidth()
            .then(rowModifier)
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
            ),
    ) {
        M3RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            modifier = Modifier.size(size.controlSize()),
            colors = RadioButtonDefaults.colors(
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                disabledSelectedColor = InstUIRadioInput.borderDisabledColor,
                disabledUnselectedColor = InstUIRadioInput.borderDisabledColor,
            ),
        )
        Text(
            text = label,
            style = InstUIText.content.copy(fontSize = size.fontSize),
            color = if (enabled) InstUIRadioInput.labelBaseColor else InstUIRadioInput.labelDisabledColor,
        )
    }
}

@Preview(name = "RadioButton — Light", showBackground = true)
@Preview(name = "RadioButton — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RadioButtonPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        var selected by remember { mutableStateOf("Due Date") }
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.container())
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        ) {
            listOf("Due Date", "Assignment Group", "Assignment Type").forEach { option ->
                RadioButton(
                    selected = selected == option,
                    onClick = { selected = option },
                    label = option,
                    accentColor = Color(0xFF00828E),
                )
            }
            RadioButton(selected = false, onClick = {}, label = "Disabled", enabled = false)
        }
    }
}
