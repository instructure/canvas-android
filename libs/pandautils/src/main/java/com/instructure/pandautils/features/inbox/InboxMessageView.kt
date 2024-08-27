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
package com.instructure.pandautils.features.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.utils.toLocalString
import java.time.ZonedDateTime
import java.time.format.FormatStyle

@Composable
fun InboxMessageView(
    messageState: InboxMessageUiState,
    actionHandler: (MessageAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        InboxMessageAuthorView(messageState, actionHandler)

        Text(messageState.message?.body ?: "")
    }
}

@Composable
private fun InboxMessageAuthorView(
    messageState: InboxMessageUiState,
    actionHandler: (MessageAction) -> Unit
) {
    val author = messageState.author
    val message = messageState.message

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        UserAvatar(
            imageUrl = author?.avatarUrl,
            name = author?.name ?: "",
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
        ) {
            Text(author?.name ?: "")

            val date = ZonedDateTime.parse(message?.createdAt ?: "")
            Text(date.toLocalString(FormatStyle.MEDIUM))
        }

        if (messageState.enabledActions.contains(MessageAction.Reply)) {
            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { actionHandler(MessageAction.Reply) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reply),
                    contentDescription = stringResource(id = R.string.reply)
                )
            }
        }

        if (messageState.enabledActions.filterIsInstance<MessageAction.Reply>().isNotEmpty()) {
            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { actionHandler(MessageAction.Reply) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_overflow),
                    contentDescription = stringResource(id = R.string.reply)
                )
            }
        }
    }
}