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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography

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
    placeholder: String,
    modifier: Modifier = Modifier,
    borderColor: Color = HorizonColors.LineAndBorder.containerStroke(),
    contentColor: Color = HorizonColors.Text.body()
) {
    var expanded by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(
                    HorizonBorder.level1(color = borderColor),
                    HorizonCornerRadius.level1
                )
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = selectedItem?.label ?: placeholder,
                style = HorizonTypography.p2,
                color = contentColor,
                modifier = Modifier.padding(end = 2.dp)
            )

            Icon(
                painter = painterResource(R.drawable.keyboard_arrow_down),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, HorizonColors.LineAndBorder.containerStroke()),
            containerColor = HorizonColors.Surface.cardPrimary(),
            offset = DpOffset(0.dp, 4.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            items.forEach { item ->
                val isSelected = selectedItem?.value == item.value
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .then(
                                    if (/*isSelected*/ true && item.backgroundColor != null) {
                                        Modifier.background(
                                            item.backgroundColor
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            item.iconRes?.let { iconRes ->
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 4.dp),
                                    tint = item.iconTint ?: HorizonColors.Icon.default()
                                )
                            }
                            Text(
                                text = item.label,
                                style = HorizonTypography.p2,
                                color = if (isSelected && item.backgroundColor != null) {
                                    item.iconTint ?: HorizonColors.Text.body()
                                } else {
                                    HorizonColors.Text.body()
                                }
                            )
                        }
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = HorizonColors.Text.body()
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.background(item.backgroundColor ?: Color.Transparent)
                )
            }
        }
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
            backgroundColor = Color.Red
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
        placeholder = "Type"
    )
}
