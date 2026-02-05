/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.progress

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.utils.getFragmentActivityOrNull
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.PagerIndicator
import com.instructure.pandautils.features.offline.sync.ProgressState
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgressWidget(
    refreshSignal: SharedFlow<Unit>,
    columns: Int,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ProgressViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current?.getFragmentActivityOrNull()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            uiState.onRefresh()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarMessage ->
            onShowSnackbar(snackbarMessage.message, snackbarMessage.actionLabel, snackbarMessage.action)
            uiState.onClearSnackbar()
        }
    }

    ProgressWidgetContent(
        modifier = modifier,
        uiState = uiState,
        columns = columns,
        activity = activity
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgressWidgetContent(
    modifier: Modifier = Modifier,
    uiState: ProgressUiState,
    columns: Int,
    activity: FragmentActivity? = null
) {
    if (uiState.loading || (uiState.uploadItems.isEmpty() && uiState.syncProgress == null)) {
        return
    }

    val allItems = buildList {
        addAll(uiState.uploadItems.map { ProgressCardData.Upload(it) })
        uiState.syncProgress?.let { add(ProgressCardData.Sync(it)) }
    }

    if (allItems.isEmpty()) return

    val itemPages = allItems.chunked(columns)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            text = stringResource(R.string.progressWidgetTitle),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.textDarkest)
        )

        val pagerState = rememberPagerState(pageCount = { itemPages.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
            beyondViewportPageCount = 1
        ) { page ->
            val itemsInPage = itemPages[page]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsInPage.forEach { item ->
                    when (item) {
                        is ProgressCardData.Upload -> UploadProgressCard(
                            item = item.data,
                            onClick = { activity?.let { uiState.onUploadClick(it, item.data) } },
                            onDismiss = { uiState.onUploadDismiss(item.data) },
                            modifier = Modifier.weight(1f)
                        )
                        is ProgressCardData.Sync -> SyncProgressCard(
                            item = item.data,
                            onClick = { activity?.let { uiState.onSyncClick(it) } },
                            onDismiss = { uiState.onSyncDismiss() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                repeat(columns - itemsInPage.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (itemPages.size > 1) {
            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                activeColor = colorResource(R.color.backgroundDarkest),
                inactiveColor = colorResource(R.color.backgroundDarkest).copy(alpha = 0.4f)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun UploadProgressCard(
    item: UploadProgressItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .background(
                        color = colorResource(item.iconBackground),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = colorResource(R.color.textLightest),
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.textDarkest),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = if (item.state != UploadState.UPLOADING) 28.dp else 0.dp)
                    )

                    if (item.subtitle.isNotEmpty()) {
                        Text(
                            text = item.subtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.textDark),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (item.state == UploadState.UPLOADING) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { item.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = colorResource(R.color.backgroundInfo),
                            trackColor = colorResource(R.color.backgroundMedium)
                        )
                    }
                }

                if (item.state != UploadState.UPLOADING) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.dismiss),
                            tint = colorResource(R.color.textDarkest),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncProgressCard(
    item: SyncProgressItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = item.state == ProgressState.ERROR
    val backgroundColor = if (isError) R.color.backgroundDanger else R.color.licorice
    val textColor = if (isError) R.color.textLightest else R.color.white

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(backgroundColor)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(textColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = if (isError) 24.dp else 0.dp)
                )

                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(textColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (item.state == ProgressState.STARTING) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = colorResource(R.color.white),
                            trackColor = colorResource(R.color.white).copy(alpha = 0.3f)
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { item.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = colorResource(R.color.white),
                            trackColor = colorResource(R.color.white).copy(alpha = 0.3f)
                        )
                    }
                }
            }

            if (isError) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.a11y_contentDescription_syncDashboardNotificationDismiss),
                        tint = colorResource(textColor),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private sealed class ProgressCardData {
    data class Upload(val data: UploadProgressItem) : ProgressCardData()
    data class Sync(val data: SyncProgressItem) : ProgressCardData()
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    backgroundColor = 0x1F2124,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProgressWidgetContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressWidgetContent(
        uiState = ProgressUiState(
            loading = false,
            uploadItems = listOf(
                UploadProgressItem(
                    workerId = UUID.randomUUID(),
                    title = "Assignment Submission",
                    subtitle = "Uploading...",
                    progress = 45,
                    state = UploadState.UPLOADING,
                    icon = R.drawable.ic_upload,
                    iconBackground = R.color.backgroundInfo,
                    courseId = 1L,
                    assignmentId = 1L,
                    attemptId = 1L,
                    folderId = null
                ),
                UploadProgressItem(
                    workerId = UUID.randomUUID(),
                    title = "File Upload",
                    subtitle = "Completed",
                    progress = 100,
                    state = UploadState.SUCCEEDED,
                    icon = R.drawable.ic_check_white_24dp,
                    iconBackground = R.color.backgroundSuccess,
                    courseId = null,
                    assignmentId = null,
                    attemptId = null,
                    folderId = 1L
                )
            ),
            syncProgress = SyncProgressItem(
                title = "Syncing offline content",
                subtitle = "3 courses",
                progress = 67,
                state = ProgressState.IN_PROGRESS,
                itemCount = 3
            )
        ),
        columns = 1
    )
}