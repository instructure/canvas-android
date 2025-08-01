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
package com.instructure.horizon.horizonui.organisms.inputs.singleselectsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.Input
import com.instructure.horizon.horizonui.organisms.inputs.common.InputContainer
import com.instructure.horizon.horizonui.organisms.inputs.common.InputDropDownPopup
import com.instructure.pandautils.compose.modifiers.conditional

@Composable
fun SingleSelectSearch(
    state: SingleSelectSearchState,
    modifier: Modifier = Modifier
) {
    var isReset by remember { mutableStateOf(false) }
    LaunchedEffect(state.selectedOption) {
        state.selectedOption?.let {
            state.onSearchQueryChanged(TextFieldValue(it, TextRange(it.length)))
        }
    }

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
                modifier = Modifier
                    .onGloballyPositioned {
                        heightInPx = it.size.height
                        width = with(localDensity) { it.size.width.toDp() }
                    }
            ) {
                SingleSelectSearchContent(state, isReset) {
                    isReset = false
                }
            }

            InputDropDownPopup(
                isMenuOpen = state.isMenuOpen,
                options = state.options.filter { it.contains(state.searchQuery.text, ignoreCase = true) },
                width = width,
                verticalOffsetPx = heightInPx,
                isFocusable = false,
                onMenuOpenChanged = { open ->
                    state.onMenuOpenChanged(open)
                },
                onOptionSelected = { selectedOption ->
                    isReset = true
                    state.onOptionSelected(selectedOption)
                    state.onMenuOpenChanged(false)
                    state.onSearchQueryChanged(TextFieldValue(selectedOption, TextRange(selectedOption.length)))
                },
                item = { option ->
                    SingleSelectSearchItem(option, state)
                },
                modifier = Modifier.heightIn(max = 200.dp)
            )
        }
    }
}

@Composable
private fun <T>SingleSelectSearchItem(option: T, state: SingleSelectSearchState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .conditional(option == state.selectedOption && state.isMenuOpen) {
                background(HorizonColors.Surface.institution())
            }
    ) {
        Text(
            text = option.toString(),
            style = HorizonTypography.p1,
            color = if (option == state.selectedOption) HorizonColors.Surface.pageSecondary() else HorizonColors.Text.body(),
            modifier = Modifier
                .padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SingleSelectSearchContent(state: SingleSelectSearchState, isReset: Boolean, resetHandled: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
    ) {
        BasicTextField(
            value = state.searchQuery,
            enabled = state.enabled,
            singleLine = true,
            decorationBox = { TextFieldBox(state, HorizonTypography.p1) { it() } },
            onValueChange = { newValue ->
                state.onSearchQueryChanged(newValue)
                if (!isReset) {
                    state.onMenuOpenChanged(true)
                } else {
                    resetHandled()
                }
            },
            modifier = Modifier
                .padding(
                    vertical = state.size.verticalPadding,
                    horizontal = state.size.horizontalPadding
                )
                .onFocusChanged {
                    if (it.isFocused) {
                        state.onFocusChanged(true)
                    }
                }
        )
    }
}

@Composable
private fun TextFieldBox(state: SingleSelectSearchState, textStyle: TextStyle, innerTextField: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier.fillMaxWidth()
    ){
        innerTextField()
        if (state.searchQuery.text.isEmpty() && state.placeHolderText != null) {
            Text(
                text = state.placeHolderText,
                style = textStyle,
                color = HorizonColors.Text.placeholder(),
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSearchSimpleCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300, heightDp = 170)
fun SingleSelectSearchSimpleExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSearchSimpleCollapsedFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSearchSimpleCollapsedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSearchSelectedCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300, heightDp = 180)
fun SingleSelectSearchSelectedExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSearchSelectedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300)
fun SingleSelectSearchSelectedCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300, heightDp = 180)
fun SingleSelectSearchSelectedExpandedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300)
fun SingleSelectSearchSelectedErrorCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSearchCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSearchSelectedCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelectSearch(
        state = SingleSelectSearchState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectSearchInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}