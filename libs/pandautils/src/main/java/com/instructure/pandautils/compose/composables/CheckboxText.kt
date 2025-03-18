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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@Composable
fun CheckboxText(
    text: String,
    selected: Boolean,
    color: Color,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var checked by remember { mutableStateOf(selected) }
    Row (
        modifier = modifier.clickable { checked = !checked }.padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChanged(it)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = color,
                uncheckedColor = color
            )
        )
        Text(
            text = text,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            color = colorResource(R.color.textDarkest)
        )
    }
}

@Preview
@Composable
private fun CheckboxTextUnselectedPreview() {
    CheckboxText(
        text = "Checkbox Text",
        selected = false,
        onCheckedChanged = {},
        color = Color.Blue
    )
}

@Preview
@Composable
private fun CheckboxTextSelectedPreview() {
    CheckboxText(
        text = "Checkbox Text",
        selected = true,
        onCheckedChanged = {},
        color = Color.Blue
    )
}