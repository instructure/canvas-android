package com.instructure.horizon.horizonui.organisms.inputs.text_field

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

data class TextFieldState(
    val value: TextFieldValue,
    val onValueChange: (TextFieldValue) -> Unit,
    val size: TextFieldInputSize,
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val isDisabled: Boolean = false,
    val errorText: String? = null,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val onFocusChanged: (Boolean) -> Unit = {},
)
