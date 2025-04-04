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
package com.instructure.horizon.horizonui.organisms.inputs.multi_select

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Tag
import com.instructure.horizon.horizonui.molecules.TagSize
import com.instructure.horizon.horizonui.molecules.TagType
import com.instructure.horizon.horizonui.organisms.inputs.common.Input
import com.instructure.horizon.horizonui.organisms.inputs.common.InputContainer
import com.instructure.horizon.horizonui.organisms.inputs.common.InputDropDownPopup

@Composable
fun MultiSelect(
    modifier: Modifier = Modifier,
    state: MultiSelectState
) {
    Input(
        label = state.label,
        helperText = null,
        errorText = state.errorText,
        required = state.required,
        modifier = modifier
    ) {
        Column(
            modifier = modifier
        ) {
            var heightInPx by remember { mutableIntStateOf(0) }
            InputContainer(
                isFocused = state.isFocused,
                isError = state.errorText != null,
                isDisabled = state.isDisabled,
                modifier = Modifier
                    .clickable { state.onMenuOpenChanged(!state.isMenuOpen) }
                    .onGloballyPositioned {
                        heightInPx = it.size.height
                    }
            ) {
                MultiSelectContent(state)
            }

            InputDropDownPopup(
                isMenuOpen = state.isMenuOpen,
                options = state.options,
                verticalOffsetPx = heightInPx,
                onMenuOpenChanged = state.onMenuOpenChanged,
                onOptionSelected = { selectedOption ->
                    if (state.selectedOptions.contains(selectedOption)) {
                        state.onOptionRemoved(selectedOption)
                    } else {
                        state.onOptionSelected(selectedOption)
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun MultiSelectContent(state: MultiSelectState) {
    val iconRotation = animateIntAsState(
        targetValue = if (state.isMenuOpen) 180 else 0,
        label = "iconRotation"
    )
    val paddingModifier = if (state.selectedOptions.isNotEmpty()) {
        Modifier.padding(
            vertical = state.size.verticalContentPadding,
            horizontal = state.size.horizontalContentPadding
        )
    } else {
        Modifier
            .padding(
                vertical = state.size.verticalTextPadding,
                horizontal = state.size.horizontalTextPadding
            )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = paddingModifier
    ) {
        if (state.selectedOptions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(state.size.horizontalContentPadding),
                verticalArrangement = Arrangement.spacedBy(state.size.verticalContentPadding),
            ) {
                state.selectedOptions.forEach { selectedOption ->
                    Tag(
                        label = selectedOption,
                        type = TagType.STANDALONE,
                        size = when (state.size) {
                            MultiSelectInputSize.Small -> TagSize.SMALL
                            MultiSelectInputSize.Medium -> TagSize.MEDIUM
                        },
                        dismissible = true,
                        onDismiss = {
                            state.onOptionRemoved(selectedOption)
                        },
                    )
                }
            }
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
fun MultiSelectCollapsedPlaceholderPreview() {
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        )
    )
}

@Composable
@Preview
fun MultiSelectCollapsedPreview() {
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        )
    )
}

@Composable
@Preview(heightDp = 150)
fun MultiSelectExpandedPreview() {
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = true,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        )
    )
}

@Composable
@Preview
fun MultiSelectSelectedPreview() {
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        )
    )
}