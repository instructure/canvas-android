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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.pandautils.utils.toPx

@Composable
fun <T>InputDropDownPopup(
    isMenuOpen: Boolean,
    options: List<T>,
    verticalOffsetPx: Int,
    width: Dp,
    onMenuOpenChanged: (Boolean) -> Unit,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isFocusable: Boolean = true,
    item: @Composable (T) -> Unit = { selectionOption ->
        SingleSelectItem(selectionOption.toString())
    },
) {
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(
            -SpaceSize.SPACE_8.value.toPx,
            verticalOffsetPx + SpaceSize.SPACE_8.value.toPx
        ),
        onDismissRequest = { onMenuOpenChanged(false) },
        properties = PopupProperties(focusable = isMenuOpen && isFocusable)
    ) {
        AnimatedVisibility(
            isMenuOpen,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
            label = "InputDropDownPopupAnimation",
        ) {
            Card(
                modifier = modifier
                    .padding(bottom = 8.dp) // to avoid shadow clipping during animation
                    .padding(horizontal = 8.dp) // to avoid shadow clipping during animation
                    .width(width),
                shape = HorizonCornerRadius.level2,
                colors = CardDefaults.cardColors()
                    .copy(containerColor = HorizonColors.Surface.cardPrimary()),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = HorizonElevation.level3,
                    pressedElevation = HorizonElevation.level3,
                    focusedElevation = HorizonElevation.level3,
                    disabledElevation = HorizonElevation.level3,
                    hoveredElevation = HorizonElevation.level3,
                    draggedElevation = HorizonElevation.level3
                ),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ){
                            Spinner(size = SpinnerSize.EXTRA_SMALL)
                        }
                    } else if (options.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            SingleSelectItem(stringResource(R.string.noOptionsAvailable))
                        }
                    } else {
                        options.forEach { selectionOption ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOptionSelected(selectionOption)
                                        onMenuOpenChanged(false)
                                    }
                            ) {
                                item(selectionOption)
                            }
                        }
                    }
                }
            }
        }
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