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
package com.instructure.horizon.horizonui.organisms.inputs.multiselect

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
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
    state: MultiSelectState,
    modifier: Modifier = Modifier
) {
    Input(
        label = state.label,
        helperText = state.helperText,
        errorText = state.errorText,
        required = state.required,
        modifier = modifier
            .onFocusChanged {
                state.onFocusChanged(it.isFocused)
            }
    ) {
        Column(
            modifier = Modifier
        ) {
            val localDensity = LocalDensity.current
            var heightInPx by remember { mutableIntStateOf(0) }
            var width by remember { mutableStateOf(0.dp) }
            InputContainer(
                isFocused = false,
                isError = state.errorText != null,
                enabled = state.enabled,
                onClick = { state.onMenuOpenChanged(!state.isMenuOpen) },
                modifier = Modifier
                    .onGloballyPositioned {
                        heightInPx = it.size.height
                        width = with(localDensity) { it.size.width.toDp() }
                    }
            ) {
                MultiSelectContent(state)
            }
            InputDropDownPopup(
                isMenuOpen = state.isMenuOpen,
                options = state.options,
                verticalOffsetPx = heightInPx,
                width = width,
                onMenuOpenChanged = state.onMenuOpenChanged,
                onOptionSelected = { selectedOption ->
                    if (state.selectedOptions.contains(selectedOption)) {
                        state.onOptionRemoved(selectedOption)
                    } else {
                        state.onOptionSelected(selectedOption)
                    }
                },
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
            tint = HorizonColors.Icon.default(),
            contentDescription = null,
            modifier = Modifier
                .rotate(iconRotation.value.toFloat())
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectSimpleCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300, heightDp = 170)
fun MultiSelectSimpleExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectSimpleCollapsedFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectSimpleCollapsedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectSelectedCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300, heightDp = 180)
fun MultiSelectSelectedExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectSelectedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300)
fun MultiSelectSelectedCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300, heightDp = 180)
fun MultiSelectSelectedExpandedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300)
fun MultiSelectSelectedErrorCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = emptyList(),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectSelectedCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelect(
        state = MultiSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}