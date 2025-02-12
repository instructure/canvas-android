/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    selectedIndex: Int,
    options: List<String>,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = options.getOrNull(selectedIndex).orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.colors().copy(
                unfocusedIndicatorColor = colorResource(id = R.color.borderMedium),
                focusedIndicatorColor = colorResource(id = R.color.borderInfo),
                cursorColor = colorResource(id = R.color.textDarkest),
                focusedTextColor = colorResource(id = R.color.textDark)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.background(color = colorResource(id = R.color.backgroundLightestElevated))
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = it,
                            color = colorResource(id = R.color.textDarkest)
                        )
                    },
                    onClick = {
                        onOptionSelected(options.indexOf(it))
                        expanded = false
                    },
                    modifier = Modifier.background(color = colorResource(id = R.color.backgroundLightestElevated))
                )
            }
        }
    }
}

@Composable
private fun TrailingIcon(
    expanded: Boolean,
    onIconClick: () -> Unit = {}
) {
    IconButton(onClick = onIconClick, modifier = Modifier.clearAndSetSemantics { }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            tint = colorResource(id = R.color.borderMedium),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .rotate(if (expanded) 180f else 360f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DropdownPreview() {
    ContextKeeper.appContext = LocalContext.current
    Dropdown(
        selectedIndex = 0,
        options = listOf("Option 1", "Option 2", "Option 3"),
        onOptionSelected = {}
    )
}