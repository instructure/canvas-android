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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.UserAvatar

@Composable
fun RecipientChip(
    enabled: Boolean,
    recipient: Recipient,
    onRemove: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(colorResource(R.color.backgroundLightest))
            .border(1.dp, colorResource(R.color.borderMedium), CircleShape)
            .testTag("recipientChip")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                recipient.avatarURL,
                recipient.name ?: "",
                Modifier
                    .size(30.dp)
                    .padding(2.dp)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = recipient.name ?: "",
                color = colorResource(id = R.color.textDarkest),
                fontSize = 14.sp,
            )

            Spacer(Modifier.width(4.dp))

            IconButton(
                enabled = enabled,
                onClick = { onRemove() },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.a11y_removeRecipient),
                    tint = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
@Preview
fun RecipientChipPreview() {
    RecipientChip(
        enabled = true,
        recipient = Recipient(
            name = "John Doe",
            avatarURL = null
        )
    )
}