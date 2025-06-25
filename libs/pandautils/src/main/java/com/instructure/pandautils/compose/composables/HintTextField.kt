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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BasicTextFieldWithHintDecoration(
    hintColor: Color,
    textColor: Color,
    value: String?,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    decorationText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = LocalTextStyle.current
) {
    BasicTextField(
        value = value.orEmpty(),
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle.copy(
            color = textColor,
            fontSize = 16.sp,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold
        ),
        cursorBrush = SolidColor(textColor),
        decorationBox = { innerTextField ->
            Box {
                if (value.isNullOrEmpty()) {
                    Text(
                        text = hint,
                        fontSize = 16.sp,
                        color = hintColor,
                        textAlign = TextAlign.End
                    )
                    innerTextField()
                } else {
                    Row {
                        innerTextField()
                        decorationText?.let {
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = it,
                                fontSize = 16.sp,
                                color = textColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun BasicTextFieldWithHintDecorationPreview() {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("Some input") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("BasicTextField with Hint (DecorationBox - Empty):")
            BasicTextFieldWithHintDecoration(
                value = text1,
                onValueChange = { text1 = it },
                hint = "Type something here...",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                textColor = Color.Black,
                hintColor = Color.Gray
            )

            Text("BasicTextField with Hint (DecorationBox - With Text):")
            BasicTextFieldWithHintDecoration(
                value = text2,
                onValueChange = { text2 = it },
                hint = "This won't be visible",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                textColor = Color.Black,
                hintColor = Color.Gray,
                decorationText = "pts"
            )
        }
    }
}