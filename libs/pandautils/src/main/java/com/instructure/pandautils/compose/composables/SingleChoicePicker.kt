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
package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@Composable
fun<T> SingleChoicePicker(
    title: String,
    items: List<T>,
    stringValueOfItem: @Composable (T) -> String,
    contextColor: Color,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.backgroundLightest))
    ) {
        ListHeaderItem(title)
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(colorResource(R.color.backgroundLightest))
                    .clickable {
                        onItemSelected(item)
                    }
                    .clearAndSetSemantics {
                        contentDescription = item.toString()
                        selected = item == selectedItem
                    }
            ) {
                Spacer(modifier = Modifier.width(20.dp))
                RadioButton(
                    selected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = contextColor,
                        unselectedColor = contextColor
                    )
                )
                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    stringValueOfItem(item),
                    color = colorResource(R.color.textDarkest),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
@Preview
private fun SingleChoicePickerPreview(){
    SingleChoicePicker(
        title = "Title",
        items = listOf("Item 1", "Item 2", "Item 3"),
        stringValueOfItem = { it },
        contextColor = Color.Blue,
        selectedItem = "Item 2",
        onItemSelected = {},
        modifier = Modifier
    )
}