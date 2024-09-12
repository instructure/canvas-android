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
package com.instructure.pandautils.features.inbox.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Message
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.inbox.details.composables.MessageMenuItem
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.handleUrlAt
import com.instructure.pandautils.utils.linkify
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        InboxMessageAuthorView(messageState, actionHandler)

        Spacer(Modifier.height(16.dp))

        InboxMessageDetailsView(messageState, actionHandler)
    }
}

@Composable
private fun InboxMessageDetailsView(
    messageState: InboxMessageUiState,
    actionHandler: (MessageAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val annotatedString = messageState.message?.body?.linkify(
            SpanStyle(
                color = colorResource(id = R.color.textInfo),
                textDecoration = TextDecoration.Underline
            )
        ) ?: AnnotatedString("")
        SelectionContainer {
            ClickableText(
                text = annotatedString,
                onClick = {
                    annotatedString.handleUrlAt(it) {
                        actionHandler(MessageAction.UrlSelected(it))
                    }
                },
                style = TextStyle.Default.copy(
                    fontSize = 16.sp
                )
            )
        }

        messageState.message?.attachments?.forEach { attachment ->
            Spacer(modifier = Modifier.height(16.dp))

            val attachmentCardItem = AttachmentCardItem(attachment, AttachmentStatus.UPLOADED, true)
            AttachmentCard(
                attachmentCardItem,
                onSelect = { actionHandler(MessageAction.OpenAttachment(attachment)) },
                onRemove = {})
        }

        if (messageState.enabledActions) {
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { messageState.message?.let { actionHandler( MessageAction.Reply(it) ) } },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.backgroundLightest),
                    contentColor = Color(ThemePrefs.brandColor)
                ),
                contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                content = {
                    Text(
                        text = stringResource(id = R.string.reply),
                        modifier = Modifier.offset(x = (-8).dp) // Remove button's default padding
                    )
                },
            )
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

    var recipientsExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp)
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
                .clickable { recipientsExpanded = !recipientsExpanded }
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
                color = colorResource(id = R.color.textDarkest)
            )

            val date = ZonedDateTime.parse(message?.createdAt ?: "")

            Text(
                text = date.toLocalString(FormatStyle.MEDIUM),
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark)
            )
        }

        if (messageState.enabledActions) {
            IconButton(onClick = { messageState.message?.let { actionHandler( MessageAction.Reply(it) ) } }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reply),
                    contentDescription = stringResource(id = R.string.reply)
                )
            }

            message?.let {
                MessageMenu(it, actionHandler)
            }
        }
    }
}

@Composable
private fun MessageMenu(message: Message, actionHandler: (MessageAction) -> Unit) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.CenterEnd,
    ){
        OverflowMenu(
            modifier = Modifier
                .background(color = colorResource(id = R.color.backgroundLightestElevated))
                .testTag("overFlowMenu"),
            showMenu = showMenu,
            tint = colorResource(id = R.color.textDarkest),
            onDismissRequest = {
                showMenu = !showMenu
            }
        ) {
            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(MessageAction.Reply(message))
                }
            ) {
                MessageMenuItem(R.drawable.ic_reply, stringResource(id = R.string.reply))
            }

            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(MessageAction.ReplyAll(message))
                }
            ) {
                MessageMenuItem(R.drawable.ic_reply_all, stringResource(id = R.string.replyAll))
            }

            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(MessageAction.Forward(message))
                }
            ) {
                MessageMenuItem(R.drawable.ic_forward, stringResource(id = R.string.forward))
            }

            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(MessageAction.DeleteMessage(message))
                }
            ) {
                MessageMenuItem(R.drawable.ic_trash, stringResource(id = R.string.delete))
            }

        }
    }
}