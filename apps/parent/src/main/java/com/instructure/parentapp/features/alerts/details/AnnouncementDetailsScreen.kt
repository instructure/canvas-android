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
@file:OptIn(ExperimentalMaterialApi::class)

package com.instructure.parentapp.features.alerts.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandares.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.inbox.utils.AttachmentCard
import com.instructure.pandautils.features.inbox.utils.AttachmentCardItem
import com.instructure.pandautils.features.inbox.utils.AttachmentStatus
import com.instructure.pandautils.views.CanvasWebView
import com.jakewharton.threetenabp.AndroidThreeTen
import java.util.Date

@Composable
fun AnnouncementDetailsScreen(
    uiState: AnnouncementDetailsUiState,
    actionHandler: (AnnouncementDetailsAction) -> Unit,
    navigationActionClick: () -> Unit,
    applyOnWebView: (CanvasWebView.() -> Unit),
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val errorMessage = stringResource(id = R.string.errorLoadingAnnouncement)
        LaunchedEffect(uiState.showErrorSnack) {
            if (uiState.showErrorSnack) {
                val result = snackbarHostState.showSnackbar(errorMessage)
                if (result == SnackbarResult.Dismissed) {
                    actionHandler(AnnouncementDetailsAction.SnackbarDismissed)
                }
            }
        }

        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = uiState.pageTitle.orEmpty(),
                    backgroundColor = Color(uiState.studentColor),
                    contentColor = colorResource(id = R.color.textLightest),
                    navigationActionClick = {
                        navigationActionClick()
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { padding ->
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.isRefreshing,
                    onRefresh = {
                        actionHandler(AnnouncementDetailsAction.Refresh)
                    }
                )
                Box(modifier = modifier.padding(padding).pullRefresh(pullRefreshState)) {
                    when {
                        uiState.isError -> {
                            ErrorContent(
                                errorMessage = stringResource(id = R.string.errorLoadingAnnouncement),
                                retryClick = {
                                    actionHandler(AnnouncementDetailsAction.Refresh)
                                }, modifier = Modifier.fillMaxSize()
                            )
                        }

                        uiState.isLoading -> {
                            Loading(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("loading"),
                                color = Color(uiState.studentColor)
                            )
                        }

                        else -> {
                            AnnouncementDetailsSuccessScreen(
                                uiState,
                                actionHandler,
                                applyOnWebView,
                                modifier
                            )
                        }
                    }
                    PullRefreshIndicator(
                        refreshing = uiState.isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .testTag("pullRefreshIndicator"),
                        contentColor = Color(uiState.studentColor)
                    )
                }
            })
    }
}

@Composable
private fun AnnouncementDetailsSuccessScreen(
    uiState: AnnouncementDetailsUiState,
    actionHandler: (AnnouncementDetailsAction) -> Unit,
    applyOnWebView: (CanvasWebView.() -> Unit),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val padding = 16.dp
        Column(
            modifier = modifier.padding(vertical = padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = padding)) {
                Text(
                    text = uiState.announcementTitle.orEmpty(),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 24.sp
                )
                Text(
                    text = DateHelper.getDateAtTimeString(
                        LocalContext.current,
                        R.string.alertDateTime,
                        uiState.postedDate
                    ).orEmpty(),
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 14.sp
                )
                AttachmentsRow(uiState.attachment, actionHandler)
            }
            uiState.message?.let { message ->
                Divider(Modifier.padding(horizontal = padding))
                Text(
                    modifier = Modifier.padding(
                        top = 18.dp, bottom = 6.dp, start = padding, end = padding
                    ),
                    text = stringResource(R.string.description),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                ComposeCanvasWebViewWrapper(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    html = message,
                    applyOnWebView = applyOnWebView
                )
            }
        }
    }
}

@Composable
private fun AttachmentsRow(
    attachment: Attachment?,
    actionHandler: (AnnouncementDetailsAction) -> Unit
) {
    attachment?.let {
        AttachmentCard(
            AttachmentCardItem(
                attachment = it,
                status = AttachmentStatus.UPLOADED,
                readOnly = true
            ),
            onSelect = { actionHandler(AnnouncementDetailsAction.OpenAttachment(it)) },
            onRemove = {}
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview
@Composable
private fun AnnouncementDetailsPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    AnnouncementDetailsScreen(
        uiState = AnnouncementDetailsUiState(
            pageTitle = "Course Name",
            announcementTitle = "Announcement Title",
            postedDate = Date(),
            message = "",
            attachment = null
        ),
        actionHandler = {},
        navigationActionClick = {},
        applyOnWebView = {}
    )
}

@Preview
@Composable
private fun AnnouncementDetailsAttachmentPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    AnnouncementDetailsScreen(
        uiState = AnnouncementDetailsUiState(
            pageTitle = "Course Name",
            announcementTitle = "Announcement Title",
            postedDate = Date(),
            message = "",
            attachment = Attachment(
                id = 1,
                filename = "Attached_document",
                contentType = "pdf"
            )
        ),
        actionHandler = {},
        navigationActionClick = {},
        applyOnWebView = {}
    )
}

@Preview
@Composable
private fun AnnouncementDetailsErrorPreview() {
    AnnouncementDetailsScreen(
        uiState = AnnouncementDetailsUiState(
            isError = true
        ),
        actionHandler = {},
        navigationActionClick = {},
        applyOnWebView = {}
    )
}

@Preview
@Composable
private fun AnnouncementDetailsLoadingPreview() {
    AnnouncementDetailsScreen(
        uiState = AnnouncementDetailsUiState(
            isLoading = true
        ),
        actionHandler = {},
        navigationActionClick = {},
        applyOnWebView = {}
    )
}
