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
package com.instructure.horizon.horizonui.organisms.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

data class ControlsContentState(
    val title: String,
    val description: String? = null,
    val error: String? = null,
    val required: Boolean = false
)

@Composable
fun ControlsContent(state: ControlsContentState, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        val content = if (state.required) "${state.title} *" else state.title
        Text(text = content, style = HorizonTypography.p1)
        if (state.description != null) Text(text = state.description, style = HorizonTypography.p1, color = HorizonColors.Text.dataPoint())
        if (state.error != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.error),
                    tint = HorizonColors.Text.error(),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = state.error, style = HorizonTypography.p1, color = HorizonColors.Text.error())
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ControlsContentPreview() {
    ControlsContent(ControlsContentState("Content"))
}

@Composable
@Preview(showBackground = true)
fun ControlsContentDescriptionPreview() {
    ControlsContent(ControlsContentState("Content", "Description"))
}

@Composable
@Preview(showBackground = true)
fun ControlsContentDescriptionErrorPreview() {
    ControlsContent(ControlsContentState("Content", "Description", "Error Text"))
}

@Composable
@Preview(showBackground = true)
fun ControlsContentRequiredPreview() {
    ControlsContent(ControlsContentState("Content", "Description", "Error Text", required = true))
}


