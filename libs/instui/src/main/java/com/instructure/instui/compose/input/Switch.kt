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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.component.InstUIToggle
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.Switch as M3Switch

/**
 * Dummy InstUI Switch component wrapping Material3 [M3Switch] with [InstUIToggle] token colors.
 * Will be replaced with the final InstUI design.
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    M3Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = InstUIToggle.toggleBackground,
            checkedTrackColor = InstUIToggle.checkedBackgroundColor,
            checkedBorderColor = InstUIToggle.checkedBorderColor,
            uncheckedThumbColor = InstUIToggle.toggleBackground,
            uncheckedTrackColor = InstUIToggle.backgroundColor,
            uncheckedBorderColor = InstUIToggle.borderColor,
            disabledCheckedThumbColor = InstUIToggle.toggleBackground,
            disabledCheckedTrackColor = InstUIToggle.checkedBackgroundDisabledColor,
            disabledCheckedBorderColor = InstUIToggle.checkedBorderDisabledColor,
            disabledUncheckedThumbColor = InstUIToggle.toggleBackground,
            disabledUncheckedTrackColor = InstUIToggle.backgroundDisabledColor,
            disabledUncheckedBorderColor = InstUIToggle.borderDisabledColor,
        ),
    )
}

@Preview(name = "Switch — Light", showBackground = true)
@Preview(name = "Switch — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwitchPreview() {
    InstUITheme {
        var checked by remember { mutableStateOf(true) }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp)
        ) {
            Switch(checked = checked, onCheckedChange = { checked = it })
            Switch(checked = false, onCheckedChange = {})
            Switch(checked = true, onCheckedChange = {}, enabled = false)
            Switch(checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}