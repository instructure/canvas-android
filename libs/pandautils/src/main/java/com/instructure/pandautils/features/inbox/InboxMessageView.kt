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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.toLocalString
import java.time.ZonedDateTime
import java.time.format.FormatStyle

@Composable
fun InboxMessageView(
    messageState: InboxMessageUiState,
    actionHandler: (MessageAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
    ) {
        InboxMessageAuthorView(messageState, actionHandler)

        Spacer(Modifier.height(16.dp))

        Text(
            text = messageState.message?.body ?: "",
            fontSize = 16.sp,
        )

        if (messageState.enabledActions) {
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                TextButton(
                    onClick = { actionHandler(MessageAction.Reply) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorResource(id = R.color.backgroundLightest),
                        contentColor = Color(ThemePrefs.brandColor)
                    ),
                    contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                    content = {
                        Text(
                            text = stringResource(id = R.string.reply),
                            modifier = Modifier.offset(x = (-4).dp) // Remove button's default padding
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun InboxMessageAuthorView(
    messageState: InboxMessageUiState,
    actionHandler: (MessageAction) -> Unit
) {
    val author = messageState.author
    val message = messageState.message

    var recipientsExpanded by remember { mutableStateOf(false) }

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
            val recipientText = if (recipientsExpanded) {
                messageState.recipients.map { it.name }.joinToString(", ")
            } else {
                if (messageState.recipients.size > 1) {
                    "${messageState.recipients[0].name} + ${messageState.recipients.size - 1} Others"
                } else {
                    messageState.recipients[0].name
                }
            }
            Text(
                text = "${author?.name} to $recipientText",
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { recipientsExpanded = !recipientsExpanded }
            )

            val date = ZonedDateTime.parse(message?.createdAt ?: "")

            Text(
                text = date.toLocalString(FormatStyle.MEDIUM),
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark)
            )
        }

        if (messageState.enabledActions) {
            IconButton(onClick = { actionHandler(MessageAction.Reply) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reply),
                    contentDescription = stringResource(id = R.string.reply)
                )
            }

            IconButton(onClick = { actionHandler(MessageAction.Reply) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_overflow),
                    contentDescription = stringResource(id = R.string.reply)
                )
            }
        }
    }
}