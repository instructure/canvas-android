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
package com.instructure.horizon.horizonui.organisms.inputs.date_picker

import androidx.annotation.DrawableRes
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
import java.util.Date

data class DatePickerState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val isDisabled: Boolean = false,
    val errorText: String? = null,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val size: DatePickerInputSize = DatePickerInputSize.Medium,
    @DrawableRes val trailingIcon: Int = R.drawable.calendar_month,
    val selectedDate: Date? = null,
    val onClick: () -> Unit = {},
    val onFocusChanged: (Boolean) -> Unit = {},
    val dateFormat: DatePickerFormat = DatePickerFormat.Full,
)