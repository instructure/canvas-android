/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun CanvasSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    color: Color = Color(ThemePrefs.brandColor),
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colorResource(R.color.switchThumbColorChecked),
            checkedTrackColor = color,
            uncheckedThumbColor = colorResource(R.color.switchThumbColor),
            uncheckedTrackColor = colorResource(R.color.switchTrackColor),
            checkedIconColor = color,
            uncheckedIconColor = colorResource(R.color.switchIconColor),
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent
        ),
        thumbContent = {
            Icon(
                modifier = Modifier.padding(4.dp),
                painter = painterResource(if (checked) R.drawable.ic_checkmark else R.drawable.ic_close),
                contentDescription = null,
                tint = if (checked) color else colorResource(R.color.switchIconColor)
            )
        },
        modifier = modifier,
        interactionSource = interactionSource
    )
}

@Preview
@Composable
fun CanvasSwitchPreviewOn() {
    ContextKeeper.appContext = LocalContext.current
    CanvasSwitch(checked = true, onCheckedChange = null)
}

@Preview
@Composable
fun CanvasSwitchPreviewOff() {
    ContextKeeper.appContext = LocalContext.current
    CanvasSwitch(checked = false, onCheckedChange = null)
}