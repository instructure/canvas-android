package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.pandautils.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CanvasThemedTextField(
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
    var value by remember {
        mutableStateOf("")
    }

    TextField(
        value = value,
        onValueChange = {
            value = it
            onValueChange(it)
        },
        modifier = modifier.imePadding().imeNestedScroll(),
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
        keyboardActions = keyboardActions,
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
        textColor = colorResource(R.color.textDarkest),
        disabledTextColor = colorResource(R.color.textDark),
        backgroundColor = colorResource(R.color.backgroundLightest),
        cursorColor = colorResource(R.color.textDarkest),
        errorCursorColor = colorResource(R.color.textDanger),
        focusedIndicatorColor = colorResource(R.color.backgroundLightest),
        unfocusedIndicatorColor = colorResource(R.color.backgroundLightest),
        errorIndicatorColor = colorResource(R.color.textDanger),
        leadingIconColor = colorResource(R.color.textDark).copy(alpha = IconOpacity),
        trailingIconColor = colorResource(R.color.textDark).copy(alpha = IconOpacity),
        errorTrailingIconColor = colorResource(R.color.textDanger),
        focusedLabelColor = colorResource(R.color.textDarkest).copy(alpha = ContentAlpha.high),
        unfocusedLabelColor = colorResource(R.color.textDark).copy(ContentAlpha.medium),
        errorLabelColor = colorResource(R.color.textDanger),
        placeholderColor = colorResource(R.color.textDark).copy(ContentAlpha.medium),
    )
}

@Composable
@Preview
fun CanvasThemedTextFieldPreview() {
    CanvasThemedTextField(
        onValueChange = {},
        label = { /*TODO*/ },
        placeholder = { /*TODO*/ },
        leadingIcon = { /*TODO*/ },
        trailingIcon = { /*TODO*/ }
    )
}