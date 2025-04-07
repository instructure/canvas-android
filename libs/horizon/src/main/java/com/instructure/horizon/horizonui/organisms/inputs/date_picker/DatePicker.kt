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
package com.instructure.horizon.horizonui.organisms.inputs.date_picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.Input
import com.instructure.horizon.horizonui.organisms.inputs.common.InputContainer
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
import java.util.Date

@Composable
fun DatePicker(
    state: DatePickerState,
    modifier: Modifier = Modifier,
) {
    Input(
        label = state.label,
        helperText = state.helperText,
        errorText = state.errorText,
        required = state.required,
        modifier = modifier
            .onFocusChanged { state.onFocusChanged(it.isFocused) }
    ) {
        InputContainer(
            isFocused = state.isFocused,
            isError = state.errorText != null,
            isDisabled = state.isDisabled,
        ) {
            DatePickerContent(state)
        }
    }
}

@Composable
private fun DatePickerContent(state: DatePickerState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.cardPrimary())
            .clickable(enabled = !state.isDisabled) { state.onClick() }
            .padding(
                vertical = state.size.verticalPadding,
                horizontal = state.size.horizontalPadding,
            )
    ) {
        if (state.selectedDate != null) {
            Text(
                text = state.dateFormat.format.format(state.selectedDate),
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
            painter = painterResource(state.trailingIcon),
            contentDescription = null,
            tint = HorizonColors.Icon.default(),
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerSimplePreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = false,
            isDisabled = false,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerSimpleFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = true,
            isDisabled = false,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerSimpleErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = false,
            isDisabled = false,
            errorText = "Error",
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerSimpleErrorFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = true,
            isDisabled = false,
            errorText = "Error",
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerPlaceholderPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerPlaceholderErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = "Error",
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerValueFullPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            dateFormat = DatePickerFormat.Full,
            selectedDate = Date(),
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerValueNumericPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            dateFormat = DatePickerFormat.Numeric,
            selectedDate = Date(),
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerValueFullErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            dateFormat = DatePickerFormat.Full,
            selectedDate = Date(),
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = "Error",
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerValueNumericErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            dateFormat = DatePickerFormat.Numeric,
            selectedDate = Date(),
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = "Error",
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerValueFullDisabled() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            selectedDate = Date(),
            dateFormat = DatePickerFormat.Full,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = true,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerValueNumericDisabled() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            selectedDate = Date(),
            dateFormat = DatePickerFormat.Numeric,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = true,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun DatePickerPlaceholderDisabled() {
    ContextKeeper.appContext = LocalContext.current
    DatePicker(
        state = DatePickerState(
            size = DatePickerInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = true,
            errorText = null,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}