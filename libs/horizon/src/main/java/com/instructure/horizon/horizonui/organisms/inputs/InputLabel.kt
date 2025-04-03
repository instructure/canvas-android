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
package com.instructure.horizon.horizonui.organisms.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

enum class InputLabelRequired {
    Regular,
    Required,
    Optional
}

data class InputLabelState(
    val text: String,
    val required: InputLabelRequired = InputLabelRequired.Regular,
    val isError: Boolean = false,
)

@Composable
fun InputLabel(
    state: InputLabelState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            state.text,
            style = HorizonTypography.labelLargeBold
        )
        when (state.required) {
            InputLabelRequired.Required -> {
                val requiredColor = if (state.isError) {
                    HorizonColors.Text.error()
                } else {
                    HorizonColors.Text.body()
                }
                Text(
                    stringResource(R.string.requiredMark),
                    style = HorizonTypography.labelLargeBold,
                    color = requiredColor,
                )
            }

            InputLabelRequired.Optional -> {
                Text(
                    stringResource(R.string.optionalMark),
                    style = HorizonTypography.p1,
                )
            }

            else -> {}
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputLabelRegularPreview() {
    InputLabel(
        state = InputLabelState(
            text = "Label",
            required = InputLabelRequired.Regular,
            isError = false
        )
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputLabelRegularErrorPreview() {
    InputLabel(
        state = InputLabelState(
            text = "Label",
            required = InputLabelRequired.Regular,
            isError = true
        )
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputLabelRequiredPreview() {
    InputLabel(
        state = InputLabelState(
            text = "Label",
            required = InputLabelRequired.Required,
            isError = false
        )
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputLabelRequiredErrorPreview() {
    InputLabel(
        state = InputLabelState(
            text = "Label",
            required = InputLabelRequired.Required,
            isError = true
        )
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputLabelOptionalPreview() {
    InputLabel(
        state = InputLabelState(
            text = "Label",
            required = InputLabelRequired.Optional,
            isError = false
        )
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputLabelOptionalErrorPreview() {
    InputLabel(
        state = InputLabelState(
            text = "Label",
            required = InputLabelRequired.Optional,
            isError = true
        )
    )
}