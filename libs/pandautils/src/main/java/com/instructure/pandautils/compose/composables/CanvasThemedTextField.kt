package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.IconOpacity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CanvasThemedTextField(
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default.copy(
        color = colorResource(R.color.textDarkest),
        fontSize = 16.sp,
        fontFamily = FontFamily(Font(R.font.lato_font_family)),
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(Color.Black),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = @Composable { innerTextField -> innerTextField() }
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onValueChange(it.text)
        },
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = {
            val cursorRect = it.getCursorRect(textFieldValue.selection.start)
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView(cursorRect)
            }
        },
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        decorationBox = decorationBox,
    )

//    TextField(
//        value = value,
//        onValueChange = {
//            value = it
//            onValueChange(it)
//        },
//        modifier = modifier,
//        enabled = enabled,
//        readOnly = readOnly,
//        textStyle = textStyle,
//        label = label,
//        placeholder = placeholder,
//        leadingIcon = leadingIcon,
//        trailingIcon = trailingIcon,
//        isError = isError,
//        visualTransformation = visualTransformation,
//        keyboardOptions = keyboardOptions,
//        keyboardActions = keyboardActions,
//        singleLine = singleLine,
//        maxLines = maxLines,
//        minLines = minLines,
//        interactionSource = interactionSource,
//        shape = shape,
//        colors = colors
//    )
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
    )
}