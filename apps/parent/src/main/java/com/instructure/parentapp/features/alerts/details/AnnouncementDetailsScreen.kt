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
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Attachment
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

@Composable
fun AnnounceDetailsScreen(
    uiState: AnnouncementDetailsUiState,
    actionHandler: (AnnouncementDetailsAction) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
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
                AnnounceDetailsSuccessScreen(
                    uiState,
                    actionHandler,
                    navigationActionClick,
                    modifier
                )
            }
        }
    }
}

@Composable
private fun AnnounceDetailsSuccessScreen(
    uiState: AnnouncementDetailsUiState,
    actionHandler: (AnnouncementDetailsAction) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CanvasThemedAppBar(
                title = uiState.pageTitle
                    ?: LocalContext.current.getString(R.string.announcementPageTitle),
                backgroundColor = Color(uiState.studentColor),
                navigationActionClick = {
                    navigationActionClick()
                }
            )
        },
        content = { padding ->
            val pullRefreshState = rememberPullRefreshState(
                refreshing = uiState.isRefreshing,
                onRefresh = {
                    actionHandler(AnnouncementDetailsAction.Refresh)
                }
            )
            Box(modifier = modifier.pullRefresh(pullRefreshState)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = modifier.padding(16.dp)
                    ) {
                        Text(
                            text = uiState.announcementTitle ?: "",
                            style = TextStyle(
                                color = colorResource(id = R.color.textDarkest),
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            text = DateHelper.getDateAtTimeString(
                                LocalContext.current,
                                R.string.alertDateTime,
                                uiState.postedDate
                            ) ?: "",
                            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                            style = TextStyle(
                                color = colorResource(id = R.color.textDarkest),
                                fontSize = 14.sp
                            )
                        )
                        AttachmentsRow(uiState.attachment, actionHandler)
                        uiState.message?.let { message ->
                            Divider()
                            Text(
                                modifier = Modifier.padding(
                                    top = 18.dp, bottom = 6.dp
                                ),
                                text = stringResource(R.string.description),
                                style = TextStyle(
                                    color = colorResource(id = R.color.textDarkest),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            ComposeCanvasWebViewWrapper(message)
                        }
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
