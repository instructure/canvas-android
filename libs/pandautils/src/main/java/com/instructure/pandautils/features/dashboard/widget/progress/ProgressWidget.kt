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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.instructure.pandautils.features.offline.sync.ProgressState
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

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

    val itemRows = allItems.chunked(columns)

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemRows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
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
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UploadProgressCard(
    item: UploadProgressItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (item.state) {
        UploadState.UPLOADING -> R.color.licorice
        UploadState.SUCCEEDED -> R.color.backgroundSuccess
        UploadState.FAILED -> R.color.backgroundDanger
    }

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
        when (item.state) {
            UploadState.UPLOADING -> UploadInProgressContent(
                item = item,
                onDismiss = onDismiss
            )
            UploadState.SUCCEEDED -> UploadSuccessContent(
                item = item,
                onDismiss = onDismiss
            )
            UploadState.FAILED -> UploadErrorContent(
                item = item,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun UploadInProgressContent(
    item: UploadProgressItem,
    onDismiss: () -> Unit
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
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.white),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 24.dp)
            )

            if (item.subtitle.isNotEmpty()) {
                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.white),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.dismiss),
                tint = colorResource(R.color.white),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UploadSuccessContent(
    item: UploadProgressItem,
    onDismiss: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .background(
                    color = colorResource(R.color.backgroundSuccess),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_publish),
                contentDescription = null,
                tint = colorResource(R.color.white),
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.white),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 24.dp)
                )

                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.white),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.dismiss),
                    tint = colorResource(R.color.white),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun UploadErrorContent(
    item: UploadProgressItem,
    onDismiss: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .background(
                    color = colorResource(R.color.backgroundDanger),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning_red),
                contentDescription = null,
                tint = colorResource(R.color.white),
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.white),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 24.dp)
                )

                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.white),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.dismiss),
                    tint = colorResource(R.color.white),
                    modifier = Modifier.size(20.dp)
                )
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
        if (isError) {
            SyncErrorContent(
                item = item,
                onDismiss = onDismiss
            )
        } else {
            SyncProgressContent(
                item = item,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun SyncProgressContent(
    item: SyncProgressItem,
    onDismiss: () -> Unit
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
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.white),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 24.dp)
            )

            Text(
                text = item.subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.white),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.a11y_contentDescription_syncDashboardNotificationDismiss),
                tint = colorResource(R.color.white),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SyncErrorContent(
    item: SyncProgressItem,
    onDismiss: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .background(
                    color = colorResource(R.color.backgroundDanger),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning_red),
                contentDescription = null,
                tint = colorResource(R.color.white),
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.white),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 24.dp)
                )

                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.white),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.a11y_contentDescription_syncDashboardNotificationDismiss),
                    tint = colorResource(R.color.white),
                    modifier = Modifier.size(20.dp)
                )
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
private fun UploadInProgressPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressWidgetContent(
        uiState = ProgressUiState(
            loading = false,
            uploadItems = listOf(
                UploadProgressItem(
                    workerId = UUID.randomUUID(),
                    title = "Uploading Submission",
                    subtitle = "Assignment name",
                    progress = 45,
                    state = UploadState.UPLOADING,
                    icon = R.drawable.ic_upload,
                    iconBackground = R.color.backgroundInfo,
                    courseId = 1L,
                    assignmentId = 1L,
                    attemptId = 1L,
                    folderId = null
                )
            ),
            syncProgress = null
        ),
        columns = 1
    )
}

@Preview(showBackground = true)
@Composable
private fun UploadSuccessPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressWidgetContent(
        uiState = ProgressUiState(
            loading = false,
            uploadItems = listOf(
                UploadProgressItem(
                    workerId = UUID.randomUUID(),
                    title = "Submission Uploaded",
                    subtitle = "Assignment name",
                    progress = 100,
                    state = UploadState.SUCCEEDED,
                    icon = R.drawable.ic_check_white_24dp,
                    iconBackground = R.color.backgroundSuccess,
                    courseId = 1L,
                    assignmentId = 1L,
                    attemptId = 1L,
                    folderId = null
                )
            ),
            syncProgress = null
        ),
        columns = 1
    )
}

@Preview(showBackground = true)
@Composable
private fun UploadErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressWidgetContent(
        uiState = ProgressUiState(
            loading = false,
            uploadItems = listOf(
                UploadProgressItem(
                    workerId = UUID.randomUUID(),
                    title = "Submission Upload Failed",
                    subtitle = "We couldn't upload your submission. Try again, or come back later.",
                    progress = 0,
                    state = UploadState.FAILED,
                    icon = R.drawable.ic_warning,
                    iconBackground = R.color.backgroundDanger,
                    courseId = 1L,
                    assignmentId = 1L,
                    attemptId = 1L,
                    folderId = null
                )
            ),
            syncProgress = null
        ),
        columns = 1
    )
}

@Preview(showBackground = true)
@Composable
private fun SyncErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressWidgetContent(
        uiState = ProgressUiState(
            loading = false,
            uploadItems = emptyList(),
            syncProgress = SyncProgressItem(
                title = "Offline Content Sync Failed",
                subtitle = "One or more items failed to sync. Please check your internet connection and retry syncing.",
                progress = 0,
                state = ProgressState.ERROR,
                itemCount = 1
            )
        ),
        columns = 1
    )
}

@Preview(showBackground = true)
@Composable
private fun SyncQueuedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressWidgetContent(
        uiState = ProgressUiState(
            loading = false,
            uploadItems = emptyList(),
            syncProgress = SyncProgressItem(
                title = "Download starting",
                subtitle = "Queued",
                progress = 0,
                state = ProgressState.STARTING,
                itemCount = 1
            )
        ),
        columns = 1
    )
}