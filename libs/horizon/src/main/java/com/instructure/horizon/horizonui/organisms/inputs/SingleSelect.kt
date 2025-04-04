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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.sizes.SingleSelectInputSize

data class SingleSelectState(
    val label: String? = null,
    val helperText: String? = null,
    val placeHolderText: String? = null,
    val isFocused: Boolean = false,
    val isDisabled: Boolean = false,
    val isMenuOpen: Boolean = false,
    val errorText: String? = null,
    val size: SingleSelectInputSize,
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
    Column {
        val localDensity = LocalDensity.current
        var heightInPx by remember { mutableIntStateOf(0) }
        InputContainer(
            isFocused = state.isFocused,
            isError = state.errorText != null,
            isDisabled = state.isDisabled,
            size = state.size,
            modifier = Modifier
                .clickable { state.onMenuOpenChanged(!state.isMenuOpen) }
                .onGloballyPositioned {
                    heightInPx = it.size.height
                }
        ) {
            SingleSelectContent(state)
        }
        if (state.isMenuOpen) {
            Popup(
                onDismissRequest = { state.onMenuOpenChanged(false) },
                offset = IntOffset(0, heightInPx),
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    shape = HorizonCornerRadius.level2,
                    elevation = HorizonElevation.level3,
                    backgroundColor = HorizonColors.Surface.cardPrimary()
                ) {
                    Column {
                        state.options.forEach { selectionOption ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        state.onOptionSelected(selectionOption)
                                        state.onMenuOpenChanged(false)
                                    }
                            ) {
                                SingleSelectItem(selectionOption)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleSelectContent(state: SingleSelectState) {
    Row {
        if (state.selectedOption != null) {
            Text(
                text = state.selectedOption,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
        } else if (state.placeHolderText != null) {
            Text(
                text = state.placeHolderText,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.placeholder(),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_down),
            contentDescription = null
        )
    }
}

@Composable
private fun SingleSelectItem(item: String) {
    Text(
        text = item,
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        modifier = Modifier
            .padding(horizontal = 11.dp, vertical = 6.dp)
    )
}

@Composable
@Preview
fun SingleSelectCollapsedPreview() {
    SingleSelect(
        state = SingleSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        )
    )
}

@Composable
@Preview(heightDp = 200)
fun SingleSelectExpandedPreview() {
    SingleSelect(
        state = SingleSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = true,
            errorText = null,
            size = SingleSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        )
    )
}

@Composable
@Preview
fun SingleSelectFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleSelect(
        state = SingleSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = true,
            isDisabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        )
    )
}