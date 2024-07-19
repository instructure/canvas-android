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
package com.instructure.pandautils.features.inbox.compose.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Avatar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipientChip(
    recipient: BasicUser,
    onRemove: () -> Unit = {}
) {
    Chip(
        onClick = {},
        border = BorderStroke(
            ChipDefaults.OutlinedBorderSize,
            colorResource(id = R.color.borderDark)
        ),
        colors = ChipDefaults.chipColors(
            backgroundColor = colorResource(R.color.backgroundLightest),
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(recipient)

            Spacer(Modifier.width(4.dp))

            Text(
                text = recipient.name ?: "",
            )

            Spacer(Modifier.width(4.dp))

            IconButton(
                onClick = { onRemove() },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    tint = colorResource(id = R.color.textDark)
                )
            }
        }
    }
}

@Composable
@Preview
fun RecipientChipPreview() {
    RecipientChip(
        recipient = BasicUser(
            id = 1,
            name = "John Doe",
            avatarUrl = null
        )
    )
}