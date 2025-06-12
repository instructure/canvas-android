package com.instructure.horizon.horizonui.organisms.inputs.numberfield

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired

data class NumberFieldState(
    val value: TextFieldValue,
    val onValueChange: (TextFieldValue) -> Unit,
    val size: NumberFieldInputSize,
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val enabled: Boolean = true,
    val errorText: String? = null,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val onFocusChanged: (Boolean) -> Unit = {},
    val showIncreaseDecreaseButtons: Boolean = false,
    val onIncreaseButtonClick: () -> Unit = {},
    val onDecreaseButtonClick: () -> Unit = {},
) {
    val floatValue: Float?
        get() = value.text.toFloatOrNull()

    val doubleValue: Double?
        get() = value.text.toDoubleOrNull()

    val intValue: Int?
        get() = value.text.toIntOrNull()

    val longValue: Long?
        get() = value.text.toLongOrNull()
}
