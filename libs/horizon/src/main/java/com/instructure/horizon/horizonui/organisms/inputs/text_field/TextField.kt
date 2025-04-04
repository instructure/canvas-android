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
package com.instructure.horizon.horizonui.organisms.inputs.text_field

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
import com.instructure.horizon.horizonui.organisms.inputs.text_area.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.text_area.TextAreaState

@Composable
fun TextField(
    readOnly: Boolean = false,
    textStyle: TextStyle = HorizonTypography.p1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    modifier: Modifier = Modifier,
    state: TextAreaState,
) {
    TextArea(
        state = state,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .then(modifier),
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun TextFieldSimplePreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
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
fun TextFieldSimpleFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
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
fun TextFieldSimpleErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
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
fun TextFieldSimpleErrorFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
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
fun TextFieldPlaceholderPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
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
fun TextFieldPlaceholderErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
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
fun TextFieldValuePreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
        state = TextAreaState(
            value = TextFieldValue("Text value"),
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
fun TextFieldValueErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
        state = TextAreaState(
            value = TextFieldValue("Text value"),
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
fun TextFieldValueDisabled() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
        state = TextAreaState(
            value = TextFieldValue("Text value"),
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
fun TextFieldPlaceholderDisabled() {
    ContextKeeper.appContext = LocalContext.current
    TextField(
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