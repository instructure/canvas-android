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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@Composable
fun TextFieldWithHeader(
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes headerIconResource: Int? = null,
    iconContentDescription: String? = null,
    onIconClick: (() -> Unit)? = null,
) {
    Column(modifier = modifier.defaultMinSize(minHeight = 100.dp)) {
        TextFieldHeader(
            label = label,
            headerIconResource = headerIconResource,
            iconContentDescription = iconContentDescription,
            onIconClick = onIconClick
        )

        CanvasThemedTextField(onValueChange = onValueChange)
    }
}

@Composable
private fun TextFieldHeader(
    label: String,
    @DrawableRes headerIconResource: Int?,
    iconContentDescription: String?,
    onIconClick: (() -> Unit)?,
) {
    Row {
        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )
        
        Spacer(Modifier.weight(1f))

        headerIconResource?.let { icon ->
            Icon(
                modifier = Modifier.clickable { onIconClick?.invoke() },
                painter = painterResource(id = icon),
                contentDescription = iconContentDescription)
        }
    }
}