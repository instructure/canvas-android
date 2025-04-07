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

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class NumberFieldInputSize(
    val verticalPadding: Dp,
    val horizontalPadding: Dp,
    val increaseDecreaseButtonVerticalPadding: Dp,
    val increaseDecreaseButtonHorizontalPadding: Dp,
) {
    data object Small: NumberFieldInputSize(6.dp, 12.dp, 1.dp, 10.dp)
    data object Medium: NumberFieldInputSize(8.dp, 12.dp, 3.dp, 10.dp)
}