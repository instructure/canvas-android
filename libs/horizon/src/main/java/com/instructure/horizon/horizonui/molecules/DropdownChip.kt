/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.InputDropDownPopup
import com.instructure.pandautils.compose.modifiers.conditional

data class DropdownItem<T>(
    val value: T,
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val iconTint: Color? = null,
    val backgroundColor: Color? = null
)

@Composable
fun <T> DropdownChip(
    items: List<DropdownItem<out T>>,
    selectedItem: DropdownItem<out T>?,
    onItemSelected: (DropdownItem<out T>?) -> Unit,
    modifier: Modifier = Modifier,
    dropdownWidth: Dp? = null,
    placeholder: String,
    borderColor: Color = HorizonColors.LineAndBorder.lineStroke(),
    contentColor: Color = HorizonColors.Text.body(),
    verticalPadding: Dp = 0.dp
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val localDensity = LocalDensity.current
    var heightInPx by remember { mutableIntStateOf(0) }
    var width by remember { mutableStateOf(dropdownWidth) }
    val iconRotation = animateIntAsState(
        targetValue = if (isMenuOpen) 180 else 0,
        label = "iconRotation"
    )

    val expandedState = stringResource(R.string.a11y_expanded)
    val collapsedState = stringResource(R.string.a11y_collapsed)

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    if (isMenuOpen) {
                        HorizonColors.PrimitivesGrey.grey12()
                    } else {
                        HorizonColors.Surface.cardPrimary()
                    }, shape = HorizonCornerRadius.level1
                )
                .border(
                    HorizonBorder.level1(color = borderColor),
                    HorizonCornerRadius.level1
                )
                .clip(HorizonCornerRadius.level1)
                .clickable { isMenuOpen = !isMenuOpen }
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .onGloballyPositioned {
                    heightInPx = it.size.height
                    if (dropdownWidth == null) {
                        width = with(localDensity) { it.size.width.toDp() }
                    }
                }
                .clearAndSetSemantics {
                    role = Role.DropdownList
                    stateDescription = if (isMenuOpen) expandedState else collapsedState
                    contentDescription = selectedItem?.label ?: placeholder
                }
        ) {
            Text(
                text = selectedItem?.label ?: placeholder,
                style = HorizonTypography.p2,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, false)
                    .padding(
                    end = 2.dp,
                    top = verticalPadding,
                    bottom = verticalPadding
                )
            )

            Icon(
                painter = painterResource(R.drawable.keyboard_arrow_down),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(iconRotation.value.toFloat()),
                tint = contentColor
            )
        }

        InputDropDownPopup(
            isMenuOpen = isMenuOpen,
            options = items,
            width = width,
            verticalOffsetPx = heightInPx,
            onMenuOpenChanged = { isMenuOpen = it },
            onOptionSelected = { item ->
                if (selectedItem != item) {
                    onItemSelected(item)
                }
            },
            item = { item ->
                DropdownChipItem(item, selectedItem)
            }
        )
    }
}

@Composable
private fun <T> DropdownChipItem(
    item: DropdownItem<out T>,
    selectedItem: DropdownItem<out T>?
) {
    val isSelected = selectedItem?.value == item.value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .conditional(isSelected && item.backgroundColor != null) {
                background(
                    item.backgroundColor!!,
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        item.iconRes?.let { iconRes ->
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(1.dp),
                tint = if (isSelected) {
                    item.iconTint ?: HorizonColors.Icon.default()
                } else {
                    HorizonColors.Icon.default()
                }
            )
        }
        Text(
            text = item.label,
            style = HorizonTypography.p1,
            color = if (isSelected && item.backgroundColor != null) {
                item.iconTint ?: HorizonColors.Text.body()
            } else {
                HorizonColors.Text.body()
            },
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
@Preview
private fun DropdownChipPreview() {
    val items = listOf(
        DropdownItem(
            value = "important",
            label = "Important",
            iconRes = R.drawable.flag_2,
            iconTint = HorizonColors.Icon.action(),
            backgroundColor = HorizonColors.PrimitivesSea.sea12()
        ),
        DropdownItem(
            value = "unclear",
            label = "Unclear",
            iconRes = R.drawable.help,
            iconTint = HorizonColors.Icon.error()
        )
    )

    var selectedItem by remember { mutableStateOf<DropdownItem<out String>?>(items[0]) }

    DropdownChip(
        items = items,
        selectedItem = selectedItem,
        onItemSelected = { selectedItem = it },
        dropdownWidth = 140.dp,
        verticalPadding = 6.dp,
        placeholder = "Type"
    )
}
