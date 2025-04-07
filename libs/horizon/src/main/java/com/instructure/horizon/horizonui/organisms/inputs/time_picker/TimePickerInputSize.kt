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
package com.instructure.horizon.horizonui.organisms.inputs.time_picker

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class TimePickerInputSize(
    val verticalPadding: Dp,
    val horizontalPadding: Dp,
) {
    data object Small : TimePickerInputSize(11.dp, 12.dp)
    data object Medium : TimePickerInputSize(6.dp, 12.dp)
}