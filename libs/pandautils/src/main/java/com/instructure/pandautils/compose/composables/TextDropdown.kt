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
 */package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDropdown(
    options: List<String>,
    onSelection: (String) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    testTag: String = "textDropdown",
    selectedOption: String? = null,
    color: Color = colorResource(R.color.textDarkest)
) {

    var expanded by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.textDarkest),
            fontSize = 16.sp
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {

            TextButton(
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = color),
                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).testTag(testTag),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    expanded = true
                }) {
                Text(
                    text = selectedOption.orEmpty(),
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorResource(R.color.textDark)
                )
            }


            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(color = colorResource(id = R.color.backgroundLightestElevated)).requiredWidth(IntrinsicSize.Min)
            ) {
                options.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            expanded = false
                            onSelection(options[index])
                        }, text = {
                            Text(
                                text = item,
                                color = colorResource(id = R.color.textDarkest)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TextDropdownPreview() {
    var selectedOption by remember { mutableStateOf("Option 1") }
    TextDropdown(
        options = listOf("Option 1", "Option 2", "Option 3"),
        onSelection = { selectedOption = it },
        title = "Select an Option",
        selectedOption = selectedOption
    )
}