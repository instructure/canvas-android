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

package com.instructure.pandautils.features.todo.details.composables

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.instructure.pandautils.R

@Composable
fun OverflowMenu(content: @Composable () -> Unit) {
    val showMenu = remember {
        mutableStateOf(false)
    }

    IconButton(onClick = {
        showMenu.value = !showMenu.value
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.utils_contentDescriptionDiscussionsOverflow),
            tint = Color.White
        )
    }
    DropdownMenu(
        modifier = Modifier.background(color = colorResource(id = R.color.backgroundLightestElevated)),
        expanded = showMenu.value,
        onDismissRequest = {
            showMenu.value = false
        }
    ) {
        content()
    }
}