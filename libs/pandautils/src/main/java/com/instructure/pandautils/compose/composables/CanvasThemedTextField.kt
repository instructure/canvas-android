package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.IconOpacity
import androidx.compose.material.TextFieldDefaults.UnfocusedIndicatorLineOpacity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.pandautils.R

@Composable
fun CanvasThemedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation. None,
    keyboardOptions: KeyboardOptions = KeyboardOptions. Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small.copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize),
    colors: TextFieldColors = getDefaultCanvasTextFieldColors()
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions =keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors
    )
}

@Composable
private fun getDefaultCanvasTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.textFieldColors(
        textColor = Color(R.color.textDarkest),
        disabledTextColor = Color(R.color.textDark),
        backgroundColor = Color(R.color.backgroundLightest),
        cursorColor = Color(R.color.textDarkest),
        errorCursorColor = Color(R.color.textDanger),
        focusedIndicatorColor = Color(R.color.textDarkest).copy(alpha = ContentAlpha.high),
        unfocusedIndicatorColor = Color(R.color.textDark).copy(alpha = UnfocusedIndicatorLineOpacity),
        errorIndicatorColor = Color(R.color.textDanger),
        leadingIconColor = Color(R.color.textDark).copy(alpha = IconOpacity),
        trailingIconColor = Color(R.color.textDark).copy(alpha = IconOpacity),
        errorTrailingIconColor = Color(R.color.textDanger),
        focusedLabelColor = Color(R.color.textDarkest).copy(alpha = ContentAlpha.high),
        unfocusedLabelColor = Color(R.color.textDark).copy(ContentAlpha.medium),
        errorLabelColor = Color(R.color.textDanger),
        placeholderColor = Color(R.color.textDark).copy(ContentAlpha.medium),
    )
}

@Composable
@Preview
fun CanvasThemedTextFieldPreview() {
    CanvasThemedTextField(
        value = "Value",
        onValueChange = {},
        label = { /*TODO*/ },
        placeholder = { /*TODO*/ },
        leadingIcon = { /*TODO*/ },
        trailingIcon = { /*TODO*/ }
    )
}