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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.R

@Composable
fun Avatar(user: Recipient) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .border(1.dp, colorResource(id = R.color.borderDark), CircleShape)
            .background(colorResource(id = R.color.backgroundLightest))
    ) {
        if (user.avatarURL != null) {
            AsyncImage(
                model = user.avatarURL,
                contentDescription = user.name,
            )
        } else {
            val initialLetters = user.name?.split(" ")?.map { it.first() }?.joinToString("")?.uppercase() ?: ""
            Text(initialLetters)
        }
    }
}

@Composable
@Preview
fun AvatarPreview() {
    Avatar(
        Recipient(
            name = "John Doe",
            avatarURL = null
        )
    )
}