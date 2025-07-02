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
package com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
fun MultiSelectSearch(
    state: MultiSelectSearchState,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
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
                isFocused = state.isFocused || state.isMenuOpen,
                isError = state.errorText != null,
                enabled = state.enabled,
                onClick = {
                    focusManager.clearFocus()
                    state.onMenuOpenChanged(!state.isMenuOpen)
              },
                modifier = Modifier
                    .onGloballyPositioned {
                        heightInPx = it.size.height
                        width = with(localDensity) { it.size.width.toDp() }
                    }
            ) {
                Column {
                    if (state.selectedOptions.isNotEmpty()) { MultiSelectContent(state) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MultiSelectSearchContent(state, Modifier.weight(1f))

                        if (state.selectedOptions.isEmpty()) {
                            DropDownIcon(
                                state,
                                Modifier.padding(horizontal = state.size.horizontalContentPadding)
                            )
                        }
                    }
                }
            }
            InputDropDownPopup(
                isMenuOpen = state.isMenuOpen,
                options = state.options,
                isLoading = state.isOptionListLoading,
                isFocusable = false,
                verticalOffsetPx = heightInPx,
                width = width,
                onMenuOpenChanged = {},
                onOptionSelected = { selectedOption ->
                    if (state.selectedOptions.contains(selectedOption)) {
                        state.onOptionRemoved(selectedOption)
                    } else {
                        state.onOptionSelected(selectedOption)
                    }
                },
                modifier = Modifier.heightIn(max = 200.dp)
            )
        }
    }
}

@Composable
private fun MultiSelectSearchContent(state: MultiSelectSearchState, modifier: Modifier = Modifier) {
    BasicTextField(
        value = state.searchQuery,
        enabled = state.enabled,
        singleLine = true,
        decorationBox = { TextFieldBox(state, HorizonTypography.p1) { it() } },
        onValueChange = { newValue ->
            state.onSearchQueryChanged(newValue)
            state.onMenuOpenChanged(true)
        },
        modifier = modifier
            .padding(
                vertical = state.size.verticalTextPadding,
                horizontal = state.size.horizontalTextPadding
            )
            .onFocusChanged {
                if (it.isFocused) {
                    state.onFocusChanged(true)
                }
            }
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun MultiSelectContent(state: MultiSelectSearchState) {
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
                            MultiSelectSearchInputSize.Small -> TagSize.SMALL
                            MultiSelectSearchInputSize.Medium -> TagSize.MEDIUM
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

        DropDownIcon(state)
    }
}

@Composable
private fun DropDownIcon(state: MultiSelectSearchState, modifier: Modifier = Modifier) {
    val iconRotation = animateIntAsState(
        targetValue = if (state.isMenuOpen) 180 else 0,
        label = "iconRotation"
    )
    Icon(
        painter = painterResource(R.drawable.keyboard_arrow_down),
        tint = HorizonColors.Icon.default(),
        contentDescription = null,
        modifier = modifier
            .rotate(iconRotation.value.toFloat())
    )
}

@Composable
private fun TextFieldBox(state: MultiSelectSearchState, textStyle: TextStyle, innerTextField: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier.fillMaxWidth()
    ){
        innerTextField()
        if (state.searchQuery.text.isEmpty() && state.searchPlaceHolder != null) {
            Text(
                text = state.searchPlaceHolder,
                style = textStyle,
                color = HorizonColors.Text.placeholder(),
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun MultiSelectSearchSimpleCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSimpleExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSimpleCollapsedFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSimpleCollapsedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSelectedCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSelectedExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSelectedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSelectedCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSelectedExpandedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSelectedErrorCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectSearchInputSize.Medium,
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
fun MultiSelectSearchSelectedCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    MultiSelectSearch(
        state = MultiSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = MultiSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOptions = listOf("Option 1", "Option 3"),
            onOptionSelected = {},
            onMenuOpenChanged = {},
            onOptionRemoved = {}
        ),
        modifier = Modifier.padding(4.dp)
    )
}