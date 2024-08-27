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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.features.inbox.details.InboxDetailsAction
import com.instructure.pandautils.features.inbox.details.InboxDetailsUiState
import com.instructure.pandautils.features.inbox.details.ScreenState
import com.instructure.pandautils.features.inbox.util.InboxMessageView
import com.instructure.pandautils.features.inbox.util.MessageAction

@Composable
fun InboxDetailsScreen(
    title: String,
    uiState: InboxDetailsUiState,
    messageActionHandler: (MessageAction) -> Unit,
    actionHandler: (InboxDetailsAction) -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasAppBar(
                    title = title,
                    navIconRes = R.drawable.ic_back_arrow,
                    navIconContentDescription = stringResource(id = R.string.contentDescription_back),
                    navigationActionClick = { actionHandler(InboxDetailsAction.CloseFragment) },
                    actions = {
                        AppBarMenu(uiState.conversation, actionHandler)
                    },
                )
            },
            content = { padding ->
                InboxDetailsScreenContent(padding, uiState, messageActionHandler, actionHandler)
            }
        )
    }
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
            .fillMaxSize()
            .pullRefresh(pullToRefreshState)
            .padding(padding)
    ) {

        LazyColumn {
            item {
                when (uiState.state) {
                    is ScreenState.Loading -> {
                        InboxDetailsLoading()
                    }

                    is ScreenState.Error -> {
                        InboxDetailsError(actionHandler)
                    }

                    is ScreenState.Empty -> {
                        InboxDetailsEmpty(actionHandler)
                    }

                    is ScreenState.Success -> {
                        InboxDetailsContentView(uiState, actionHandler, messageActionHandler)
                    }
                }
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
        modifier = Modifier.fillMaxSize(),
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

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = conversation.subject ?: stringResource(id = R.string.message),
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
                    contentDescription = if (conversation.isStarred) stringResource(id = R.string.unstarSelected) else stringResource(id = R.string.starSelected),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }
        }

        Divider()

        messages.forEach { messageState ->
            InboxMessageView(messageState, messageActionHandler, modifier = Modifier.padding(16.dp))

            Divider()
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
        tint = colorResource(id = R.color.textDarkest),
        onDismissRequest = {
            showMenu = !showMenu
        }
    ) {
        DropdownMenuItem(
            onClick = {
                showMenu = !showMenu
                actionHandler(InboxDetailsAction.Reply)
            }
        ) {
            MessageMenuItem(R.drawable.ic_reply, stringResource(id = R.string.reply))
        }

        DropdownMenuItem(
            onClick = {
                showMenu = !showMenu
                actionHandler(InboxDetailsAction.ReplyAll)
            }
        ) {
            MessageMenuItem(R.drawable.ic_reply_all, stringResource(id = R.string.replyAll))
        }

        DropdownMenuItem(
            onClick = {
                showMenu = !showMenu
                actionHandler(InboxDetailsAction.Forward)
            }
        ) {
            MessageMenuItem(R.drawable.ic_forward, stringResource(id = R.string.forward))
        }

        if (conversation?.workflowState == Conversation.WorkflowState.READ) {
            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(InboxDetailsAction.UpdateState(conversation.id, Conversation.WorkflowState.UNREAD))
                }
            ) {
                MessageMenuItem(R.drawable.ic_mark_as_unread, stringResource(id = R.string.markAsUnread))
            }
        }

        if (conversation?.workflowState == Conversation.WorkflowState.UNREAD) {
            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(InboxDetailsAction.UpdateState(conversation.id, Conversation.WorkflowState.READ))
                }
            ) {
                MessageMenuItem(R.drawable.ic_mark_as_read, stringResource(id = R.string.markAsRead))
            }
        }

        if (conversation != null && conversation.workflowState != Conversation.WorkflowState.ARCHIVED) {
            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(InboxDetailsAction.UpdateState(conversation.id, Conversation.WorkflowState.ARCHIVED))
                }
            ) {
                MessageMenuItem(R.drawable.ic_archive, stringResource(id = R.string.archive))
            }
        } else if (conversation != null) {
            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(InboxDetailsAction.UpdateState(conversation.id, Conversation.WorkflowState.READ))
                }
            ) {
                MessageMenuItem(R.drawable.ic_unarchive, stringResource(id = R.string.unarchive))
            }
        }

        conversation?.let {
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

