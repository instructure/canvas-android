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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize

data class RadioItemState(
    val controlsContentState: ControlsContentState,
    val selected: Boolean,
    val onClick: (() -> Unit)? = null,
    val enabled: Boolean = true
)

@Composable
fun RadioItem(state: RadioItemState, modifier: Modifier = Modifier) {
    val alphaModifier = if (state.enabled) modifier else modifier.alpha(0.5f)
    Row(modifier = alphaModifier) {
        RadioButton(
            selected = state.selected,
            onClick = state.onClick,
            enabled = state.enabled,
            colors = horizonRadioItemColors,
            modifier = Modifier.size(20.dp)
        )
        HorizonSpace(SpaceSize.SPACE_8)
        ControlsContent(state = state.controlsContentState)
    }
}

private val horizonRadioItemColors = RadioButtonColors(
    selectedColor = HorizonColors.Icon.default(),
    unselectedColor = HorizonColors.LineAndBorder.containerStroke(),
    disabledSelectedColor = HorizonColors.Icon.default(),
    disabledUnselectedColor = HorizonColors.LineAndBorder.containerStroke()
)

@Composable
@Preview(showBackground = true)
private fun RadioItemPreview() {
    RadioItem(
        state = RadioItemState(
            controlsContentState = ControlsContentState(
                title = "Radio Item",
                description = "This is a radio item"
            ),
            selected = true
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun RadioItemDeselectedPreview() {
    RadioItem(
        state = RadioItemState(
            controlsContentState = ControlsContentState(
                title = "Radio Item",
                description = "This is a radio item"
            ),
            selected = false
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun RadioItemDisabledPreview() {
    RadioItem(
        state = RadioItemState(
            controlsContentState = ControlsContentState(
                title = "Radio Item",
                description = "This is a radio item"
            ),
            selected = true,
            enabled = false
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun RadioItemSingleLinePreview() {
    RadioItem(
        state = RadioItemState(
            controlsContentState = ControlsContentState(title = "Radio Item"),
            selected = true
        )
    )
}