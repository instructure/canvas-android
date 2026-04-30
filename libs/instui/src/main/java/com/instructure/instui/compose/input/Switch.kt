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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.component.InstUIToggle
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.Check
import com.instructure.instui.token.icon.line.X
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.Switch as M3Switch

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
) {
    val iconColor = knobIconColor(checked, enabled, readOnly, isError)
    val thumb: @Composable () -> Unit = {
        Icon(
            imageVector = if (checked) InstUIIcons.Line.Check else InstUIIcons.Line.X,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(SwitchDefaults.IconSize),
        )
    }

    M3Switch(
        checked = checked,
        onCheckedChange = if (readOnly) null else onCheckedChange,
        enabled = enabled && !readOnly,
        thumbContent = thumb,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = InstUIToggle.toggleBackground,
            checkedTrackColor = if (readOnly) InstUIToggle.checkedBackgroundReadonlyColor else InstUIToggle.checkedBackgroundColor,
            checkedBorderColor = if (readOnly) InstUIToggle.checkedBorderReadonlyColor else Color.Transparent,
            checkedIconColor = iconColor,
            uncheckedThumbColor = InstUIToggle.toggleBackground,
            uncheckedTrackColor = when {
                readOnly -> InstUIToggle.backgroundReadonlyColor
                isError -> InstUIToggle.errorBorderColor
                else -> InstUIToggle.uncheckedIconBorderColor
            },
            uncheckedBorderColor = if (readOnly) InstUIToggle.borderReadonlyColor else Color.Transparent,
            uncheckedIconColor = iconColor,
            disabledCheckedThumbColor = InstUIToggle.toggleBackground,
            disabledCheckedTrackColor = InstUIToggle.checkedBackgroundDisabledColor,
            disabledCheckedBorderColor = Color.Transparent,
            disabledCheckedIconColor = iconColor,
            disabledUncheckedThumbColor = InstUIToggle.toggleBackground,
            disabledUncheckedTrackColor = InstUIToggle.backgroundDisabledColor,
            disabledUncheckedBorderColor = Color.Transparent,
            disabledUncheckedIconColor = iconColor,
        ),
    )
}

@Composable
private fun knobIconColor(
    checked: Boolean,
    enabled: Boolean,
    readOnly: Boolean,
    isError: Boolean,
): Color = when {
    !enabled -> if (checked) InstUIToggle.checkedBackgroundDisabledColor else InstUIToggle.backgroundDisabledColor
    readOnly -> if (checked) InstUIToggle.checkedBorderReadonlyColor else InstUIToggle.uncheckedIconBorderReadonlyColor
    checked -> InstUIToggle.checkedBackgroundColor
    isError -> InstUIToggle.errorBorderColor
    else -> InstUIToggle.uncheckedIconBorderColor
}

@Preview(name = "Switch — Light", showBackground = true)
@Preview(name = "Switch — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwitchPreview() {
    InstUITheme {
        var checkedDefault by remember { mutableStateOf(true) }
        var uncheckedDefault by remember { mutableStateOf(false) }
        Column(
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)) {
                Switch(checked = checkedDefault, onCheckedChange = { checkedDefault = it })
                Switch(checked = uncheckedDefault, onCheckedChange = { uncheckedDefault = it })
                Switch(checked = false, onCheckedChange = {}, isError = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)) {
                Switch(checked = true, onCheckedChange = {}, enabled = false)
                Switch(checked = false, onCheckedChange = {}, enabled = false)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)) {
                Switch(checked = true, onCheckedChange = null, readOnly = true)
                Switch(checked = false, onCheckedChange = null, readOnly = true)
            }
        }
    }
}
