/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.instui.compose.input

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUITextInput
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Dummy InstUI TextInput component. Uses [InstUITextInput] tokens for styling.
 * Will be replaced with the final InstUI design.
 */
enum class TextInputSize(
    val height: @Composable () -> Dp,
    val fontSize: @Composable () -> androidx.compose.ui.unit.TextUnit,
    val horizontalPadding: @Composable () -> Dp,
) {
    Small(
        height = { InstUITextInput.heightSm },
        fontSize = { InstUITextInput.fontSizeSm },
        horizontalPadding = { InstUITextInput.paddingHorizontalSm },
    ),
    Medium(
        height = { InstUITextInput.heightMd },
        fontSize = { InstUITextInput.fontSizeMd },
        horizontalPadding = { InstUITextInput.paddingHorizontalMd },
    ),
    Large(
        height = { InstUITextInput.heightLg },
        fontSize = { InstUITextInput.fontSizeLg },
        horizontalPadding = { InstUITextInput.paddingHorizontalLg },
    ),
}

@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    size: TextInputSize = TextInputSize.Medium,
    placeholder: String? = null,
    textAlign: TextAlign = TextAlign.Start,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val height = size.height()
    val fontSize = size.fontSize()
    val horizontalPadding = size.horizontalPadding()

    val backgroundColor = when {
        !enabled -> InstUITextInput.backgroundDisabledColor
        readOnly -> InstUITextInput.backgroundReadonlyColor
        else -> InstUITextInput.backgroundColor
    }
    val borderColor = when {
        isError -> InstUITextInput.errorBorderColor
        !enabled -> InstUITextInput.borderDisabledColor
        readOnly -> InstUITextInput.borderReadonlyColor
        else -> InstUITextInput.borderColor
    }
    val textColor = when {
        !enabled -> InstUITextInput.textDisabledColor
        readOnly -> InstUITextInput.textReadonlyColor
        else -> InstUITextInput.textColor
    }

    val textStyle = remember(textColor, fontSize, textAlign) {
        TextStyle(
            color = textColor,
            fontFamily = InstUITextInput.fontFamily,
            fontWeight = InstUITextInput.fontWeight,
            fontSize = fontSize,
            textAlign = textAlign,
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .heightIn(min = height)
            .clip(RoundedCornerShape(InstUITextInput.borderRadius))
            .background(backgroundColor)
            .border(
                width = InstUITextInput.borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(InstUITextInput.borderRadius),
            ),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(textColor),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = when (textAlign) {
                    TextAlign.End -> Alignment.CenterEnd
                    TextAlign.Center -> Alignment.Center
                    else -> Alignment.CenterStart
                },
                modifier = Modifier
                    .defaultMinSize(minHeight = height)
                    .padding(horizontal = horizontalPadding),
            ) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = InstUIText.content.copy(
                            color = InstUITextInput.placeholderColor,
                            fontSize = fontSize,
                            textAlign = textAlign,
                        ),
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview(name = "TextInput — Light", showBackground = true)
@Preview(name = "TextInput — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputPreview() {
    InstUITheme {
        var small by remember { mutableStateOf("") }
        var medium by remember { mutableStateOf("Hello") }
        var large by remember { mutableStateOf("") }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp)
        ) {
            TextInput(
                value = small,
                onValueChange = { small = it },
                size = TextInputSize.Small,
                placeholder = "Small",
                modifier = Modifier.width(180.dp),
            )
            TextInput(
                value = medium,
                onValueChange = { medium = it },
                size = TextInputSize.Medium,
                placeholder = "Medium",
                modifier = Modifier.width(240.dp),
            )
            TextInput(
                value = large,
                onValueChange = { large = it },
                size = TextInputSize.Large,
                placeholder = "Large numeric",
                textAlign = TextAlign.End,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(240.dp),
            )
            TextInput(
                value = "Disabled",
                onValueChange = {},
                enabled = false,
                modifier = Modifier.width(240.dp),
            )
            TextInput(
                value = "Error state",
                onValueChange = {},
                isError = true,
                modifier = Modifier.width(240.dp),
            )
        }
    }
}