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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.CheckboxDefaults
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
import com.instructure.instui.token.component.InstUICheckbox
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.Checkbox as M3Checkbox

enum class CheckboxSize(
    val controlSize: @Composable () -> androidx.compose.ui.unit.Dp,
    val fontSize: TextUnit,
) {
    Small({ InstUICheckbox.controlSizeSm }, InstUICheckbox.fontSizeSm),
    Medium({ InstUICheckbox.controlSizeMd }, InstUICheckbox.fontSizeMd),
    Large({ InstUICheckbox.controlSizeLg }, InstUICheckbox.fontSizeLg),
}

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CheckboxSize = CheckboxSize.Medium,
    accentColor: Color? = null,
) {
    val checkedColor = accentColor ?: InstUICheckbox.borderCheckedColor
    val uncheckedColor = InstUICheckbox.borderColor

    val rowModifier = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        )
    } else {
        Modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUICheckbox.gap),
        modifier = modifier
            .fillMaxWidth()
            .then(rowModifier)
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
            ),
    ) {
        M3Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier.size(size.controlSize()),
            colors = CheckboxDefaults.colors(
                checkedColor = checkedColor,
                uncheckedColor = uncheckedColor,
                disabledCheckedColor = InstUICheckbox.borderDisabledColor,
                disabledUncheckedColor = InstUICheckbox.borderDisabledColor,
            ),
        )
        Text(
            text = label,
            style = InstUIText.content.copy(fontSize = size.fontSize),
            color = if (enabled) InstUICheckbox.labelBaseColor else InstUICheckbox.labelDisabledColor,
        )
    }
}

@Preview(name = "Checkbox — Light", showBackground = true)
@Preview(name = "Checkbox — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CheckboxPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        var a by remember { mutableStateOf(true) }
        var b by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.container())
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        ) {
            Checkbox(checked = a, onCheckedChange = { a = it }, label = "Default selected color")
            Checkbox(
                checked = b,
                onCheckedChange = { b = it },
                label = "Course-color accent",
                accentColor = Color(0xFF00828E),
            )
            Checkbox(checked = true, onCheckedChange = {}, label = "Disabled checked", enabled = false)
        }
    }
}
