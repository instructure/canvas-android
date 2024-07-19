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

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.pandautils.compose.composables.Avatar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipientChip(
    recipient: BasicUser,
) {
    Chip(onClick = {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(recipient)

            Text(
                text = recipient.name ?: "",
            )
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