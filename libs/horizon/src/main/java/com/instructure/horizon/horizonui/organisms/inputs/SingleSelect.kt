/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.horizonui.organisms.inputs

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.InputContainer
import com.instructure.horizon.horizonui.organisms.inputs.common.InputDropDownPopup
import com.instructure.horizon.horizonui.organisms.inputs.sizes.SingleSelectInputSize

data class SingleSelectState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val isDisabled: Boolean = false,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val size: SingleSelectInputSize,
    val options: List<String>,
    val selectedOption: String?,
    val onOptionSelected: (String) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
)

@Composable
fun SingleSelect(
    modifier: Modifier = Modifier,
    state: SingleSelectState
) {
    Column(
        modifier = modifier
    ) {
        var heightInPx by remember { mutableIntStateOf(0) }
        InputContainer(
            isFocused = state.isFocused,
            isError = state.errorText != null,
            isDisabled = state.isDisabled,
            size = state.size,
            modifier = Modifier
                .clickable { state.onMenuOpenChanged(!state.isMenuOpen) }
                .onGloballyPositioned {
                    heightInPx = it.size.height
                }
        ) {
            SingleSelectContent(state)
        }

        InputDropDownPopup(
            isMenuOpen = state.isMenuOpen,
            options = state.options,
            verticalOffsetPx = heightInPx,
            onMenuOpenChanged = state.onMenuOpenChanged,
            onOptionSelected = { selectedOption ->
                state.onOptionSelected(selectedOption)
                state.onMenuOpenChanged(false)
            }
        )
    }
}

@Composable
private fun SingleSelectContent(state: SingleSelectState) {
    var iconRotation = animateIntAsState(
        targetValue = if (state.isMenuOpen) 180 else 0,
        label = "iconRotation"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.selectedOption != null) {
            Text(
                text = state.selectedOption,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
        } else if (state.placeHolderText != null) {
            Text(
                text = state.placeHolderText,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.placeholder(),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_down),
            contentDescription = null,
            modifier = Modifier
                .rotate(iconRotation.value.toFloat())
        )
    }
}

@Composable
@Preview
fun SingleSelectCollapsedPreview() {
    SingleSelect(
        state = SingleSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        )
    )
}

@Composable
@Preview(heightDp = 150)
fun SingleSelectExpandedPreview() {
    SingleSelect(
        state = SingleSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = true,
            errorText = null,
            size = SingleSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        )
    )
}

@Composable
@Preview
fun SingleSelectFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelect(
        state = SingleSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        )
    )
}