package com.instructure.horizon.horizonui.organisms.inputs.timepicker

import androidx.annotation.DrawableRes
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TimePickerState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val enabled: Boolean = true,
    val errorText: String? = null,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val size: TimePickerInputSize = TimePickerInputSize.Medium,
    @DrawableRes val trailingIcon: Int = R.drawable.schedule,
    val selectedTime: LocalTime? = null,
    val onClick: () -> Unit = {},
    val onFocusChanged: (Boolean) -> Unit = {},
    val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()),
)
