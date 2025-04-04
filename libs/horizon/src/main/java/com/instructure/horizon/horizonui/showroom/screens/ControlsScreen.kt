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
@file:OptIn(ExperimentalLayoutApi::class)

package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.organisms.controls.CheckboxItem
import com.instructure.horizon.horizonui.organisms.controls.CheckboxItemState
import com.instructure.horizon.horizonui.organisms.controls.ControlsContentState
import com.instructure.horizon.horizonui.organisms.controls.RadioItem
import com.instructure.horizon.horizonui.organisms.controls.RadioItemState
import com.instructure.horizon.horizonui.organisms.controls.SwitchItem
import com.instructure.horizon.horizonui.organisms.controls.SwitchItemState
import com.instructure.horizon.horizonui.organisms.controls.TriStateCheckboxItem
import com.instructure.horizon.horizonui.organisms.controls.TriStateCheckboxItemState

@Composable
fun ControlsScreen() {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val controlsContentStates = listOf(
            ControlsContentState("Title"),
            ControlsContentState("Title", "Description"),
            ControlsContentState("Title", "Description", "Error"),
            ControlsContentState("Title", "Description", "Error", true)
        )
        Text("Checkboxes", style = HorizonTypography.p2)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            controlsContentStates.forEach { state ->
                var checked by remember { mutableStateOf(false) }
                CheckboxItem(state = CheckboxItemState(state, checked, { checked = it }, true))
            }
            CheckboxItem(state = CheckboxItemState(controlsContentStates.first(), false, null, false))
            controlsContentStates.forEach { state ->
                var toggleableState by remember { mutableStateOf(ToggleableState.Indeterminate) }
                TriStateCheckboxItem(state = TriStateCheckboxItemState(state, toggleableState, {
                    if (toggleableState == ToggleableState.Indeterminate) {
                        toggleableState = ToggleableState.Off
                    } else {
                        toggleableState = ToggleableState.Indeterminate
                    }
                }, true))
            }
        }

        HorizonSpace(SpaceSize.SPACE_32)
        Text("Switches", style = HorizonTypography.p2)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            controlsContentStates.forEach { state ->
                var checked by remember { mutableStateOf(false) }
                SwitchItem(state = SwitchItemState(state, checked, { checked = it }, true))
            }
            SwitchItem(state = SwitchItemState(controlsContentStates.first(), false, null, false))
        }

        HorizonSpace(SpaceSize.SPACE_32)
        Text("Radio Buttons", style = HorizonTypography.p2)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            controlsContentStates.forEach { state ->
                var checked by remember { mutableStateOf(false) }
                RadioItem(state = RadioItemState(state, checked, { checked = !checked }, true))
            }
            RadioItem(state = RadioItemState(controlsContentStates.first(), false, null, false))
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ControlsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ControlsScreen()
}