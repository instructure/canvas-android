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
package com.instructure.horizon.horizonui.organisms.inputs.text_area

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.Input
import com.instructure.horizon.horizonui.organisms.inputs.common.InputContainer
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

@Composable
fun TextArea(
    readOnly: Boolean = false,
    textStyle: TextStyle = HorizonTypography.p1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    modifier: Modifier = Modifier,
    state: TextAreaState,
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
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = state.value,
                onValueChange = state.onValueChange,
                enabled = !state.isDisabled,
                readOnly = readOnly,
                textStyle = textStyle,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                minLines = minLines,
                visualTransformation = visualTransformation,
                onTextLayout = onTextLayout,
                interactionSource = interactionSource,
                cursorBrush = cursorBrush,
                decorationBox = { TextAreaBox(state, textStyle) { it() } },
            )
        }
    }
}

@Composable
private fun TextAreaBox(state: TextAreaState, textStyle: TextStyle, innerTextField: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .background(HorizonColors.Surface.cardPrimary())
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ){
        if (state.value.text.isEmpty() && state.placeHolderText != null) {
            Text(
                text = state.placeHolderText,
                style = textStyle,
                color = HorizonColors.Text.placeholder(),
            )
        }
        innerTextField()
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun TextAreaSimplePreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
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
fun TextAreaSimpleFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
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
fun TextAreaSimpleErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
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
fun TextAreaSimpleErrorFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
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
fun TextAreaPlaceholderPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
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
fun TextAreaPlaceholderErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
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
fun TextAreaValuePreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue("Text value \n \nText Values"),
            onValueChange = {},
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
fun TextAreaValueErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue("Text value \n \nText Values"),
            onValueChange = {},
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
fun TextAreaValueDisabled() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue("Text value \n \nText Values"),
            onValueChange = {},
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
fun TextAreaPlaceholderDisabled() {
    ContextKeeper.appContext = LocalContext.current
    TextArea(
        state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
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