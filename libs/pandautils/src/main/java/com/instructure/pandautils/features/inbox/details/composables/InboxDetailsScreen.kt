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
package com.instructure.pandautils.features.inbox.details.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.features.inbox.details.InboxDetailsAction
import com.instructure.pandautils.features.inbox.details.InboxDetailsUiState
import com.instructure.pandautils.features.inbox.details.ScreenState
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState
import com.instructure.pandautils.features.inbox.utils.InboxMessageView
import com.instructure.pandautils.features.inbox.utils.MessageAction
import com.instructure.pandautils.utils.ThemePrefs
import java.time.ZonedDateTime

@Composable
fun InboxDetailsScreen(
    title: String,
    uiState: InboxDetailsUiState,
    messageActionHandler: (MessageAction) -> Unit,
    actionHandler: (InboxDetailsAction) -> Unit
) {
    CanvasTheme {
        Scaffold(
            containerColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                AppBar(title, uiState, actionHandler)
            },
            content = { padding ->
                InboxDetailsScreenContent(padding, uiState, messageActionHandler, actionHandler)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    title: String,
    uiState: InboxDetailsUiState,
    actionHandler: (InboxDetailsAction) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (uiState.showBackButton) {
                IconButton(onClick = { actionHandler(InboxDetailsAction.CloseFragment) }) {
                    Icon(
                        painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
        },
        actions = {
            AppBarMenu(uiState.conversation, actionHandler)
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color(color = ThemePrefs.primaryColor),
            titleContentColor = Color(color = ThemePrefs.primaryTextColor),
        ),
        modifier = Modifier
            .height(64.dp)
            .testTag("toolbar"),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxDetailsScreenContent(
    padding: PaddingValues,
    uiState: InboxDetailsUiState,
    messageActionHandler: (MessageAction) -> Unit,
    actionHandler: (InboxDetailsAction) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = false,
        onRefresh = {
            actionHandler(InboxDetailsAction.RefreshCalled)
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        when (uiState.state) {
            ScreenState.Loading -> {
                InboxDetailsLoading()
            }

            ScreenState.Error -> {
                InboxDetailsError(actionHandler)
            }

            ScreenState.Empty -> {
                InboxDetailsEmpty(actionHandler)
            }

            ScreenState.Success -> {
                InboxDetailsContentView(uiState, actionHandler, messageActionHandler)
            }
        }
    }
}

@Composable
private fun InboxDetailsLoading() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Loading()
    }
}

@Composable
private fun InboxDetailsError(actionHandler: (InboxDetailsAction) -> Unit) {
    ErrorContent(
        errorMessage = stringResource(R.string.failed_to_load_conversation),
        modifier = Modifier.fillMaxSize(),
        retryClick = { actionHandler(InboxDetailsAction.RefreshCalled) }
    )
}

@Composable
private fun InboxDetailsEmpty(actionHandler: (InboxDetailsAction) -> Unit) {
    EmptyContent(
        emptyMessage = stringResource(R.string.no_messages_found),
        imageRes = R.drawable.ic_panda_nocourses,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        buttonText = stringResource(id = R.string.retry),
        buttonClick = { actionHandler(InboxDetailsAction.RefreshCalled) }
    )
}

@Composable
private fun InboxDetailsContentView(
    uiState: InboxDetailsUiState,
    actionHandler: (InboxDetailsAction) -> Unit,
    messageActionHandler: (MessageAction) -> Unit,
) {
    val conversation = uiState.conversation
    val messages = uiState.messageStates

    if (conversation == null) {
        InboxDetailsError(actionHandler)
        return
    }

    if (uiState.confirmationDialogState.showDialog) {
        SimpleAlertDialog(
            dialogTitle = uiState.confirmationDialogState.title,
            dialogText = uiState.confirmationDialogState.message,
            dismissButtonText = uiState.confirmationDialogState.negativeButton,
            confirmationButtonText = uiState.confirmationDialogState.positiveButton,
            onDismissRequest = uiState.confirmationDialogState.onNegativeButtonClick,
            onConfirmation = uiState.confirmationDialogState.onPositiveButtonClick
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = conversation.subject ?: stringResource(id = R.string.message),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            )

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = {
                actionHandler(
                    InboxDetailsAction.UpdateStarred(
                        conversation.id,
                        !conversation.isStarred
                    )
                )
            }) {
                Icon(
                    painter = if (conversation.isStarred) painterResource(id = R.drawable.ic_star_filled) else painterResource(
                        id = R.drawable.ic_star_outline
                    ),
                    tint = colorResource(id = R.color.textDarkest),
                    contentDescription = if (conversation.isStarred) stringResource(id = R.string.unstarSelected) else stringResource(
                        id = R.string.starSelected
                    ),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }

            Spacer(Modifier.width(4.dp))
        }

        Divider(
            color = colorResource(id = R.color.borderLight),
        )

        messages.forEach { messageState ->
            InboxMessageView(messageState, messageActionHandler)

            Divider(
                color = colorResource(id = R.color.borderLight),
            )
        }
    }
}

@Composable
private fun AppBarMenu(conversation: Conversation?, actionHandler: (InboxDetailsAction) -> Unit) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    OverflowMenu(
        modifier = Modifier
            .background(color = colorResource(id = R.color.backgroundLightestElevated))
            .testTag("overFlowMenu"),
        showMenu = showMenu,
        onDismissRequest = {
            showMenu = !showMenu
        }
    ) {
        conversation?.messages?.sortedBy { it.createdAt }?.last()?.let { message ->
            if (!conversation.cannotReply) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.reply),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_reply),
                            contentDescription = stringResource(id = R.string.reply),
                            tint = colorResource(id = R.color.textDarkest)
                        )
                    },
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(InboxDetailsAction.Reply(message))
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.replyAll),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_reply_all),
                            contentDescription = stringResource(id = R.string.replyAll),
                            tint = colorResource(id = R.color.textDarkest)
                        )
                    },
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(InboxDetailsAction.ReplyAll(message))
                    }
                )
            }

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.forward),
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_forward),
                        contentDescription = stringResource(id = R.string.forward),
                        tint = colorResource(id = R.color.textDarkest)
                    )
                },
                onClick = {
                    showMenu = !showMenu
                    actionHandler(InboxDetailsAction.Forward(message))
                }
            )

            if (conversation.workflowState == Conversation.WorkflowState.READ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.markAsUnread),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mark_as_unread),
                            contentDescription = stringResource(id = R.string.markAsUnread),
                            tint = colorResource(id = R.color.textDarkest)
                        )
                    },
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.UNREAD
                            )
                        )
                    }
                )
            }

            if (conversation.workflowState == Conversation.WorkflowState.UNREAD) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.markAsRead),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mark_as_read),
                            contentDescription = stringResource(id = R.string.markAsRead),
                            tint = colorResource(id = R.color.textDarkest)
                        )
                    },
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.READ
                            )
                        )
                    }
                )
            }

            if (conversation.workflowState != Conversation.WorkflowState.ARCHIVED) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.archive),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_archive),
                            contentDescription = stringResource(id = R.string.archive),
                            tint = colorResource(id = R.color.textDarkest)
                        )
                    },
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.ARCHIVED
                            )
                        )
                    }
                )
            } else {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.unarchive),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_unarchive),
                            contentDescription = stringResource(id = R.string.unarchive),
                            tint = colorResource(id = R.color.textDarkest)
                        )
                    },
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.READ
                            )
                        )
                    }
                )
            }

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.delete),
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = stringResource(id = R.string.delete),
                        tint = colorResource(id = R.color.textDarkest)
                    )
                },
                onClick = {
                    showMenu = !showMenu
                    actionHandler(InboxDetailsAction.DeleteConversation(conversation.id))
                }
            )
        }

    }
}

@Composable
@Preview
fun InboxDetailsScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current

    InboxDetailsScreen(title = "Message",
        actionHandler = {},
        messageActionHandler = {},
        uiState = InboxDetailsUiState(
            conversationId = 1,
            conversation = null,
            messageStates = emptyList(),
            state = ScreenState.Loading
        )
    )
}

@Composable
@Preview
fun InboxDetailsScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current

    InboxDetailsScreen(title = "Message",
        actionHandler = {},
        messageActionHandler = {},
        uiState = InboxDetailsUiState(
            conversationId = 1,
            conversation = null,
            messageStates = emptyList(),
            state = ScreenState.Error
        )
    )
}

@Composable
@Preview
fun InboxDetailsScreenEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current

    InboxDetailsScreen(title = "Message",
        actionHandler = {},
        messageActionHandler = {},
        uiState = InboxDetailsUiState(
            conversationId = 1,
            conversation = Conversation(),
            messageStates = emptyList(),
            state = ScreenState.Empty
        )
    )
}

@Composable
@Preview
fun InboxDetailsScreenContentPreview() {
    ContextKeeper.appContext = LocalContext.current

    val messages = listOf(
        Message(
            createdAt = ZonedDateTime.now().toString(),
            body = "Message 1",
            authorId = 1,
            participatingUserIds = listOf(2),
            attachments = listOf(
                Attachment(filename = "Attachment 1.txt", size = 1452),
            )
        ),
        Message(
            createdAt = ZonedDateTime.now().toString(),
            body = "Message 2",
            authorId = 2,
            participatingUserIds = listOf(1),
            attachments = listOf(
                Attachment(filename = "Attachment 2.txt", size = 1252),
            )
        ),
    )

    val conversation = Conversation(
        id = 1,
        subject = "Test subject",
        messageCount = 2,
        messages = messages,
        isStarred = true,
        participants = mutableListOf(
            BasicUser(id = 1, name = "User 1"),
            BasicUser(id = 2, name = "User 2"),
        )
    )

    val messageStates = messages.map { message ->
        val author = conversation.participants.find { it.id == message.authorId }
        val recipients = conversation.participants.filter {
            message.participatingUserIds.filter { it != message.authorId }.contains(it.id)
        }
        InboxMessageUiState(
            message = message,
            author = author,
            recipients = recipients,
            enabledActions = true,
        )
    }

    InboxDetailsScreen(title = "Message",
        actionHandler = {},
        messageActionHandler = {},
        uiState = InboxDetailsUiState(
            conversationId = 1,
            conversation = conversation,
            messageStates = messageStates,
            state = ScreenState.Success
        )
    )
}