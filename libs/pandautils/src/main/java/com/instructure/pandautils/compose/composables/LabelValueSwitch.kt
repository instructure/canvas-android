/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun LabelValueSwitch(
    label: String,
    value: String?,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(isChecked) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                checked = !checked
                onCheckedChange(checked)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                modifier = Modifier.testTag("label"),
                text = label,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest)
            )
            value?.let {
                Text(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("value"),
                    text = it,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.textDark)
                )
            }
        }
        Switch(
            checked = checked, onCheckedChange = {
                checked = it
                onCheckedChange(checked)
            }, colors = SwitchDefaults.colors(
                checkedThumbColor = Color(ThemePrefs.brandColor),
            )
        )
    }

}

@Preview
@Composable
fun LabelValueSwitchPreview() {
    ContextKeeper.appContext = LocalContext.current
    LabelValueSwitch(label = "Label", value = "Value", isChecked = true, onCheckedChange = {})
}