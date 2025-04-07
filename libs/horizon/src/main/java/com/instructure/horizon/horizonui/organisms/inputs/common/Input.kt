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
package com.instructure.horizon.horizonui.organisms.inputs.common

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun Input(
    modifier: Modifier = Modifier,
    label: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    required: InputLabelRequired = InputLabelRequired.Regular,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        if (label != null) {
            InputLabel(
                state = InputLabelState(
                    text = label,
                    required = required,
                    isError = errorText == null,
                ),
            )

            HorizonSpace(size = SpaceSize.SPACE_8)
        }

        content()

        if (errorText != null) {
            HorizonSpace(size = SpaceSize.SPACE_8)
            InputErrorText(
                text = errorText,
            )

            HorizonSpace(size = SpaceSize.SPACE_8)
        }

        if (helperText != null) {
            HorizonSpace(size = SpaceSize.SPACE_8)
            InputHelperText(
                text = helperText,
            )
        }
    }
}