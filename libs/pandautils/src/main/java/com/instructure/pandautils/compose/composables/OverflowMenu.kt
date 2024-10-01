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

import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun OverflowMenu(
    modifier: Modifier = Modifier,
    showMenu: Boolean,
    iconColor: Color = Color(ThemePrefs.primaryTextColor),
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(onClick = {
        onDismissRequest()
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.utils_contentDescriptionDiscussionsOverflow),
            tint = iconColor
        )
    }
    DropdownMenu(
        modifier = modifier,
        expanded = showMenu,
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        content()
    }
}

@Preview
@Composable
fun OverflowMenuPreview() {
    OverflowMenu(showMenu = true, onDismissRequest = {}, content = {})
}