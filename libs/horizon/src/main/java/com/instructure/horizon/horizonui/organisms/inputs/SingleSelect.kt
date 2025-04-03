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

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class SingleSelectSize(open val height: Dp, open val width: SingleSelectWidth) {
    data object Small: SingleSelectSize(34.dp, SingleSelectWidth.Fill)
    data object Medium: SingleSelectSize(44.dp, SingleSelectWidth.Fill)
    data class Custom(override val height: Dp, override val width: SingleSelectWidth) : SingleSelectSize(height, width)
}

sealed class SingleSelectWidth {
    data object Fill: SingleSelectWidth()
    data class Fixed(val width: Dp): SingleSelectWidth()
}

data class SingleSelectState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val isDisabled: Boolean = false,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val size: SingleSelectSize,
    val options: List<String>,
    val selectedOption: String?,
    val onOptionSelected: (String) -> Unit,
    val onMenuOpenChanged: (Boolean) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {},
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SingleSelect(
    modifier: Modifier = Modifier,
    state: SingleSelectState
) {
    ExposedDropdownMenuBox(
        expanded = state.isMenuOpen,
        onExpandedChange = { state.onMenuOpenChanged(it) },
        modifier = modifier
    ) {

    }
}

