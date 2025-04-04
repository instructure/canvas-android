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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun InputHelperText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = HorizonTypography.p2,
        modifier = modifier,
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputHelperTextPreview() {
    InputHelperText(
        text = "This is a helper text",
        modifier = Modifier
    )
}