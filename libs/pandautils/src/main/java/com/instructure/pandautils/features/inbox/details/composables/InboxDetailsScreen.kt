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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.features.inbox.details.InboxDetailsAction
import com.instructure.pandautils.features.inbox.details.InboxDetailsUiState
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState
import com.instructure.pandautils.features.inbox.utils.InboxMessageView
import com.instructure.pandautils.features.inbox.utils.MessageAction
import com.instructure.pandautils.utils.ScreenState
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
        CanvasScaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                AppBar(title, uiState, actionHandler)
            },
            content = { padding ->
                InboxDetailsScreenContent(padding, uiState, messageActionHandler, actionHandler)
            }
        )
    }
}

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
        navigationIcon = if (uiState.showBackButton) {
            {
                IconButton(onClick = { actionHandler(InboxDetailsAction.CloseFragment) }) {
                    Icon(
                        painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
        } else null,
        actions = {
            AppBarMenu(
                conversation = uiState.conversation,
                showDeleteButton = uiState.showDeleteButton,
                showReplyAllButton = uiState.showReplyAllButton,
                actionHandler = actionHandler
            )
        },
        backgroundColor = Color(color = ThemePrefs.primaryColor),
        contentColor = Color(color = ThemePrefs.primaryTextColor),
        elevation = 0.dp,
        modifier = Modifier
            .testTag("toolbar")
            .windowInsetsPadding(WindowInsets.displayCutout),
        windowInsets = WindowInsets.statusBars
    )
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun InboxDetailsScreenContent(
    padding: PaddingValues,
    uiState: InboxDetailsUiState,
    messageActionHandler: (MessageAction) -> Unit,
    actionHandler: (InboxDetailsAction) -> Unit
) {
    val pullToRefreshState = rememberPullRefreshState(refreshing = false, onRefresh = {
        actionHandler(InboxDetailsAction.RefreshCalled)
    })

    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.displayCutout)
            .fillMaxSize()
            .pullRefresh(pullToRefreshState)
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

            ScreenState.Content -> {
                InboxDetailsContentView(uiState, actionHandler, messageActionHandler)
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.state == ScreenState.Loading,
            state = pullToRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .testTag("pullRefreshIndicator"),
        )
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

            IconButton(onClick = { actionHandler(InboxDetailsAction.UpdateStarred(conversation.id, !conversation.isStarred)) }) {
                Icon(
                    painter = if (conversation.isStarred) painterResource(id = R.drawable.ic_star_filled) else painterResource(id = R.drawable.ic_star_outline),
                    tint = colorResource(id = R.color.textDarkest),
                    contentDescription = if (conversation.isStarred) stringResource(id = R.string.unstarSelected) else stringResource(id = R.string.starSelected),
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
private fun AppBarMenu(
    conversation: Conversation?,
    showDeleteButton: Boolean,
    showReplyAllButton: Boolean,
    actionHandler: (InboxDetailsAction) -> Unit
) {
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
            if (!conversation.cannotReply){
                DropdownMenuItem(
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(InboxDetailsAction.Reply(message))
                    }
                ) {
                    MessageMenuItem(R.drawable.ic_reply, stringResource(id = R.string.reply))
                }

                if (showReplyAllButton) {
                    DropdownMenuItem(
                        onClick = {
                            showMenu = !showMenu
                            actionHandler(InboxDetailsAction.ReplyAll(message))
                        }
                    ) {
                        MessageMenuItem(R.drawable.ic_reply_all, stringResource(id = R.string.replyAll))
                    }
                }
            }

            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(InboxDetailsAction.Forward(message))
                }
            ) {
                MessageMenuItem(R.drawable.ic_forward, stringResource(id = R.string.forward))
            }

            if (conversation.workflowState == Conversation.WorkflowState.READ) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.UNREAD
                            )
                        )
                    }
                ) {
                    MessageMenuItem(
                        R.drawable.ic_mark_as_unread,
                        stringResource(id = R.string.markAsUnread)
                    )
                }
            }

            if (conversation.workflowState == Conversation.WorkflowState.UNREAD) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.READ
                            )
                        )
                    }
                ) {
                    MessageMenuItem(
                        R.drawable.ic_mark_as_read,
                        stringResource(id = R.string.markAsRead)
                    )
                }
            }

            if (conversation.workflowState != Conversation.WorkflowState.ARCHIVED) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.ARCHIVED
                            )
                        )
                    }
                ) {
                    MessageMenuItem(R.drawable.ic_archive, stringResource(id = R.string.archive))
                }
            } else {
                DropdownMenuItem(
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(
                            InboxDetailsAction.UpdateState(
                                conversation.id,
                                Conversation.WorkflowState.READ
                            )
                        )
                    }
                ) {
                    MessageMenuItem(
                        R.drawable.ic_unarchive,
                        stringResource(id = R.string.unarchive)
                    )
                }
            }

            if (showDeleteButton) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = !showMenu
                        actionHandler(InboxDetailsAction.DeleteConversation(conversation.id))
                    }
                ) {
                    MessageMenuItem(R.drawable.ic_trash, stringResource(id = R.string.delete))
                }
            }
        }

    }
}

@Composable
@Preview
fun InboxDetailsScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current

    InboxDetailsScreen(title = "Message", actionHandler = {}, messageActionHandler = {}, uiState = InboxDetailsUiState(
        conversationId = 1,
        conversation = null,
        messageStates = emptyList(),
        state = ScreenState.Loading
    ))
}

@Composable
@Preview
fun InboxDetailsScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current

    InboxDetailsScreen(title = "Message", actionHandler = {}, messageActionHandler = {}, uiState = InboxDetailsUiState(
        conversationId = 1,
        conversation = null,
        messageStates = emptyList(),
        state = ScreenState.Error
    ))
}

@Composable
@Preview
fun InboxDetailsScreenEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current

    InboxDetailsScreen(title = "Message", actionHandler = {}, messageActionHandler = {}, uiState = InboxDetailsUiState(
        conversationId = 1,
        conversation = Conversation(),
        messageStates = emptyList(),
        state = ScreenState.Empty
    ))
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
        val recipients = conversation.participants.filter { message.participatingUserIds.filter { it != message.authorId }.contains(it.id) }
        InboxMessageUiState(
            message = message,
            author = author,
            recipients = recipients,
            enabledActions = true,
            canDelete = true,
        )
    }

    InboxDetailsScreen(title = "Message", actionHandler = {}, messageActionHandler = {}, uiState = InboxDetailsUiState(
        conversationId = 1,
        conversation = conversation,
        messageStates = messageStates,
        state = ScreenState.Content
    ))
}