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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@Composable
fun CheckboxText(
    text: String,
    selected: Boolean,
    color: Color,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    testTag: String = "checkboxText"
) {
    val fullContentDescription = if (subtitle != null) {
        "$text, $subtitle"
    } else {
        text
    }

    val stateDescriptionText = if (selected) {
        stringResource(R.string.a11y_buttonSelectionSelected)
    } else {
        stringResource(R.string.a11y_buttonSelectionNotSelected)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onCheckedChanged(!selected) }
            .semantics(mergeDescendants = true) {
                contentDescription = fullContentDescription
                stateDescription = stateDescriptionText
            }
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = {
                onCheckedChanged(it)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = color,
                uncheckedColor = color
            ),
            modifier = Modifier.testTag(testTag)
        )
        Column {
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = colorResource(R.color.textDark)
                )
            }
            Text(
                text = text,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                color = colorResource(R.color.textDarkest)
            )
        }
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