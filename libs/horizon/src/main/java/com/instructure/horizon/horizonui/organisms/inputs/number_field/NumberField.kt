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
package com.instructure.horizon.horizonui.organisms.inputs.number_field

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.Input
import com.instructure.horizon.horizonui.organisms.inputs.common.InputContainer
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

@Composable
fun NumberField(
    readOnly: Boolean = false,
    textStyle: TextStyle = HorizonTypography.p1,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    modifier: Modifier = Modifier,
    state: NumberFieldState,
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
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                keyboardActions = keyboardActions,
                singleLine = true,
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
private fun TextAreaBox(state: NumberFieldState, textStyle: TextStyle, innerTextField: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .background(HorizonColors.Surface.cardPrimary())
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .weight(1f)
                    .padding(
                        horizontal = state.size.horizontalPadding,
                        vertical = state.size.verticalPadding
                    )
            ) {
                if (state.value.text.isEmpty() && state.placeHolderText != null) {
                    Text(
                        text = state.placeHolderText,
                        style = textStyle,
                        color = HorizonColors.Text.placeholder(),
                    )
                }
                innerTextField()
            }

            if (state.showIncreaseDecreaseButtons) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(HorizonColors.PrimitivesGrey.grey11())
                            .clickable {
                                state.onIncreaseButtonClick()
                            }
                            .padding(
                                vertical = state.size.increaseDecreaseButtonVerticalPadding,
                                horizontal = state.size.increaseDecreaseButtonHorizontalPadding
                            )
                            .weight(1f),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.keyboard_arrow_up),
                            contentDescription = stringResource(R.string.a11y_increaseValue),
                            tint = HorizonColors.Icon.default(),
                            modifier = Modifier
                                .size(16.dp)
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(HorizonColors.PrimitivesGrey.grey11())
                            .clickable {
                                state.onDecreaseButtonClick()
                            }
                            .padding(
                                vertical = state.size.increaseDecreaseButtonVerticalPadding,
                                horizontal = state.size.increaseDecreaseButtonHorizontalPadding
                            )
                            .weight(1f),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.keyboard_arrow_down),
                            contentDescription = stringResource(R.string.a11y_decreaseValue),
                            tint = HorizonColors.Icon.default(),
                            modifier = Modifier
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldSimplePreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldSimpleFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldSimpleErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldSimpleErrorFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldPlaceholderPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldPlaceholderErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldValuePreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue("Text value"),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldValueErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue("Text value"),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldValueDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue("Text value"),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldPlaceholderDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
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
fun NumberFieldSimpleWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = false,
            isDisabled = false,
            errorText = null,
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldSimpleFocusedWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            size = NumberFieldInputSize.Medium,
            onValueChange = {},
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = true,
            isDisabled = false,
            errorText = null,
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldSimpleErrorWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = false,
            isDisabled = false,
            errorText = "Error",
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldSimpleErrorFocusedWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = null,
            helperText = null,
            placeHolderText = null,
            isFocused = true,
            isDisabled = false,
            errorText = "Error",
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldPlaceholderWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = null,
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldPlaceholderErrorWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = "Error",
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldValueWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue("Text value"),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = null,
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldValueErrorWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue("Text value"),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            errorText = "Error",
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldValueDisabledWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue("Text value"),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = true,
            errorText = null,
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun NumberFieldPlaceholderDisabledWithButtonsPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberField(
        state = NumberFieldState(
            value = TextFieldValue(""),
            onValueChange = {},
            size = NumberFieldInputSize.Medium,
            label = "Label",
            helperText = "Helper text",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = true,
            errorText = null,
            showIncreaseDecreaseButtons = true,
            required = InputLabelRequired.Regular,
        ),
        modifier = Modifier.padding(4.dp)
    )
}