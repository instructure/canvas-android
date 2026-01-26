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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize

data class TriStateCheckboxItemState(
    val controlsContentState: ControlsContentState,
    val toggleableState: ToggleableState,
    val onClick: (() -> Unit)? = null,
    val enabled: Boolean = true
)

data class CheckboxItemState(
    val controlsContentState: ControlsContentState,
    val checked: Boolean,
    val onCheckedChanged: ((Boolean) -> Unit)? = null,
    val enabled: Boolean = true
)

@Composable
fun TriStateCheckboxItem(state: TriStateCheckboxItemState, modifier: Modifier = Modifier) {
    val alphaModifier = if (state.enabled) modifier else modifier.alpha(0.5f)

    Row(
        modifier = alphaModifier
            .triStateToggleable(
                state = state.toggleableState,
                onClick = state.onClick ?: {},
                enabled = state.enabled
            )
    ) {
        val error = state.controlsContentState.error != null
        TriStateCheckbox(
            state = state.toggleableState,
            onClick = {},
            enabled = state.enabled,
            colors = horizonCheckboxColors(error),
            modifier = Modifier
                .size(20.dp)
                .clearAndSetSemantics {}
        )
        HorizonSpace(SpaceSize.SPACE_8)
        ControlsContent(state = state.controlsContentState)
    }
}

@Composable
fun CheckboxItem(state: CheckboxItemState, modifier: Modifier = Modifier) {
    val alphaModifier = if (state.enabled) modifier else modifier.alpha(0.5f)

    Row(
        modifier = alphaModifier
            .toggleable(
                value = state.checked,
                onValueChange = state.onCheckedChanged ?: {},
                enabled = state.enabled
            )
    ) {
        val error = state.controlsContentState.error != null
        Checkbox(
            checked = state.checked,
            onCheckedChange = null,
            enabled = state.enabled,
            colors = horizonCheckboxColors(error),
            modifier = Modifier
                .size(20.dp)
                .clearAndSetSemantics {}
        )
        HorizonSpace(SpaceSize.SPACE_8)
        ControlsContent(state = state.controlsContentState)
    }
}

private fun horizonCheckboxColors(error: Boolean) = CheckboxColors(
    checkedCheckmarkColor = if (error) HorizonColors.Icon.error() else HorizonColors.Icon.default(),
    uncheckedCheckmarkColor = if (error) HorizonColors.Icon.error() else HorizonColors.LineAndBorder.containerStroke(),
    checkedBoxColor = HorizonColors.Surface.pageSecondary(),
    uncheckedBoxColor = HorizonColors.Surface.pageSecondary(),
    disabledCheckedBoxColor = HorizonColors.Surface.pageSecondary(),
    disabledUncheckedBoxColor = HorizonColors.Surface.pageSecondary(),
    disabledIndeterminateBoxColor = HorizonColors.Surface.pageSecondary(),
    checkedBorderColor = if (error) HorizonColors.Icon.error() else HorizonColors.Icon.default(),
    uncheckedBorderColor = if (error) HorizonColors.Icon.error() else HorizonColors.LineAndBorder.containerStroke(),
    disabledBorderColor = HorizonColors.Icon.default(),
    disabledUncheckedBorderColor = HorizonColors.LineAndBorder.containerStroke(),
    disabledIndeterminateBorderColor = HorizonColors.Icon.default()
)

@Composable
@Preview(showBackground = true)
private fun TriStateCheckboxItemUncheckedPreview() {
    TriStateCheckboxItem(
        TriStateCheckboxItemState(
            ControlsContentState("Content", "Description"),
            ToggleableState.Off
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun TriStateCheckboxItemCheckedPreview() {
    TriStateCheckboxItem(
        TriStateCheckboxItemState(
            ControlsContentState("Content", "Description"),
            ToggleableState.On
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun TriStateCheckboxItemIndeterminatePreview() {
    TriStateCheckboxItem(
        TriStateCheckboxItemState(
            ControlsContentState("Content", "Description"),
            ToggleableState.Indeterminate
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun TriStateCheckboxItemErrorPreview() {
    TriStateCheckboxItem(
        TriStateCheckboxItemState(
            ControlsContentState("Content", "Description", "Error Text"),
            ToggleableState.Off
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun TriStateCheckboxItemErrorCheckedPreview() {
    TriStateCheckboxItem(
        TriStateCheckboxItemState(
            ControlsContentState("Content", "Description", "Error Text"),
            ToggleableState.On
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun TriStateCheckboxItemErrorIndeterminatePreview() {
    TriStateCheckboxItem(
        TriStateCheckboxItemState(
            ControlsContentState("Content", "Description", "Error Text"),
            ToggleableState.Indeterminate
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun TriStateCheckboxItemDisabledPreview() {
    TriStateCheckboxItem(
        TriStateCheckboxItemState(
            ControlsContentState("Content", "Description"),
            ToggleableState.Off,
            enabled = false
        )
    )
}

// We don't need more previews, as the CheckboxItem looks the same as TriStateCheckboxItem
@Composable
@Preview(showBackground = true)
private fun CheckboxItemPreview() {
    CheckboxItem(
        CheckboxItemState(
            ControlsContentState("Content", "Description"),
            checked = false
        )
    )
}