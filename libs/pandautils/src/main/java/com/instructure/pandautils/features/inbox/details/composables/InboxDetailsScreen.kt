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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.inbox.InboxMessageUiState
import com.instructure.pandautils.features.inbox.InboxMessageView
import com.instructure.pandautils.features.inbox.details.InboxDetailsAction
import com.instructure.pandautils.features.inbox.details.InboxDetailsUiState
import com.instructure.pandautils.features.inbox.details.ScreenState

@Composable
fun InboxDetailsScreen(
    title: String,
    uiState: InboxDetailsUiState,
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
                    },
                )
            },
            content = { padding ->
                InboxDetailsScreenContent(padding, uiState, actionHandler)
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun InboxDetailsScreenContent(
    padding: PaddingValues,
    uiState: InboxDetailsUiState,
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
        PullRefreshIndicator(
            refreshing = uiState.state == ScreenState.Loading,
            state = pullToRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .testTag("pullRefreshIndicator"),
        )
    }

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
            InboxDetailsContentView(uiState, actionHandler)
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
    actionHandler: (InboxDetailsAction) -> Unit
) {
    val conversation = uiState.conversation
    if (conversation == null) {
        InboxDetailsError(actionHandler)
        return
    }

    LazyColumn {
        item {
            Text(text = conversation.subject ?: stringResource(id = R.string.message))
        }
        items(conversation.messages) { message ->
            val messageState = InboxMessageUiState(message = message)
            InboxMessageView(messageState = messageState) {

            }
        }
    }
}
