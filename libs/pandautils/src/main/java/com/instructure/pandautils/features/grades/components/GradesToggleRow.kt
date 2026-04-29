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

package com.instructure.pandautils.features.grades.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.input.Switch
import com.instructure.instui.compose.text.Text as InstUIText
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.NoRippleInteractionSource
import com.instructure.pandautils.compose.composables.CanvasSwitch
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.LocalDesignSystem

@Composable
fun GradesToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contextColor: Color,
    modifier: Modifier = Modifier,
    testTagLabel: String = "",
    testTagSwitch: String = "",
) {
    when (LocalDesignSystem.current) {
        DesignSystem.Legacy -> LegacyToggleRow(
            label = label,
            checked = checked,
            onCheckedChange = onCheckedChange,
            contextColor = contextColor,
            modifier = modifier,
            testTagLabel = testTagLabel,
            testTagSwitch = testTagSwitch,
        )
        DesignSystem.InstUI -> InstUIToggleRow(
            label = label,
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun LegacyToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contextColor: Color,
    modifier: Modifier = Modifier,
    testTagLabel: String = "",
    testTagSwitch: String = "",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
            .semantics {
                role = Role.Switch
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDarkest),
            modifier = if (testTagLabel.isNotEmpty()) Modifier.semantics { } else Modifier
        )
        CanvasSwitch(
            interactionSource = NoRippleInteractionSource(),
            checked = checked,
            onCheckedChange = onCheckedChange,
            color = contextColor,
            modifier = Modifier
                .height(24.dp)
                .semantics { hideFromAccessibility() }
        )
    }
}

@Composable
private fun InstUIToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InstUIText(
            text = label,
            style = InstUITextTokens.content,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview(name = "GradesToggleRow Legacy", showBackground = true)
@Composable
private fun GradesToggleRowLegacyPreview() {
    CompositionLocalProvider(LocalDesignSystem provides DesignSystem.Legacy) {
        androidx.compose.foundation.layout.Column {
            GradesToggleRow(
                label = "Based on graded assignments",
                checked = true,
                onCheckedChange = {},
                contextColor = Color(0xFF00828E),
            )
            GradesToggleRow(
                label = "Show what-if score",
                checked = false,
                onCheckedChange = {},
                contextColor = Color(0xFF00828E),
            )
        }
    }
}

@Preview(name = "GradesToggleRow InstUI — Light", showBackground = true)
@Preview(name = "GradesToggleRow InstUI — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradesToggleRowInstUIPreview() {
    InstUITheme {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            androidx.compose.foundation.layout.Column {
                GradesToggleRow(
                    label = "Based on graded assignments",
                    checked = true,
                    onCheckedChange = {},
                    contextColor = Color(0xFF00828E),
                )
                GradesToggleRow(
                    label = "Show what-if score",
                    checked = false,
                    onCheckedChange = {},
                    contextColor = Color(0xFF00828E),
                )
            }
        }
    }
}