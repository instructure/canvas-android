/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.queuedsubmissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission.CancelScheduledSubmissionModal
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold
import com.instructure.pandautils.room.studentdb.entities.SubmissionState

@Composable
fun QueuedSubmissionsScreen(
    uiState: QueuedSubmissionsUiState,
    navController: NavHostController,
) {
    HorizonScaffold(
        title = stringResource(R.string.queuedSubmissions_title),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        when {
            uiState.loading -> Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spinner()
            }

            uiState.items.isEmpty() -> Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.queuedSubmissions_empty),
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body(),
                )
            }

            else -> LazyColumn(modifier = modifier.fillMaxSize()) {
                items(uiState.items, key = { it.submissionId }) { item ->
                    QueuedSubmissionRow(
                        item = item,
                        onOpen = { navigateToAssignment(navController, item) },
                    )
                    HorizontalDivider(color = HorizonColors.Surface.divider())
                }
            }
        }
    }
}

private fun navigateToAssignment(navController: NavHostController, item: QueuedSubmissionItemUiState) {
    val deepLink = NavDeepLinkRequest.Builder
        .fromUri("${ApiPrefs.fullDomain}/courses/${item.courseId}/assignments/${item.assignmentId}".toUri())
        .build()
    navController.navigate(deepLink)
}

@Composable
private fun QueuedSubmissionRow(
    item: QueuedSubmissionItemUiState,
    onOpen: () -> Unit,
) {
    var showCancelConfirmation by rememberSaveable { mutableStateOf(false) }
    if (showCancelConfirmation) {
        CancelScheduledSubmissionModal(
            onConfirm = {
                showCancelConfirmation = false
                item.onCancel()
            },
            onDismiss = { showCancelConfirmation = false }
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.assignmentName,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
            val (statusRes, statusColor) = when (item.state) {
                SubmissionState.QUEUED -> R.string.queuedSubmissions_statusQueued to HorizonColors.Text.timestamp()
                SubmissionState.FAILED -> R.string.queuedSubmissions_statusFailed to HorizonColors.Text.error()
                else -> R.string.queuedSubmissions_statusSubmitting to HorizonColors.Text.timestamp()
            }
            HorizonSpace(SpaceSize.SPACE_4)
            Text(
                text = stringResource(statusRes),
                style = HorizonTypography.p2,
                color = statusColor,
            )
            if (item.state == SubmissionState.FAILED) {
                HorizonSpace(SpaceSize.SPACE_4)
                Text(
                    text = stringResource(R.string.queuedSubmissions_failureMessage),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.body(),
                )
            }
        }
        HorizonSpace(SpaceSize.SPACE_8)
        if (item.state == SubmissionState.FAILED) {
            Button(
                label = stringResource(R.string.queuedSubmissions_retry),
                color = ButtonColor.Institution,
                height = ButtonHeight.SMALL,
                onClick = item.onRetry,
            )
            HorizonSpace(SpaceSize.SPACE_8)
        }
        Button(
            label = stringResource(R.string.queuedSubmissions_cancel),
            color = ButtonColor.Black,
            height = ButtonHeight.SMALL,
            onClick = { showCancelConfirmation = true },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QueuedSubmissionsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    QueuedSubmissionsScreen(
        uiState = QueuedSubmissionsUiState(
            loading = false,
            items = listOf(
                QueuedSubmissionItemUiState(1L, "Essay on Photosynthesis", SubmissionState.QUEUED, 1L, 10L),
                QueuedSubmissionItemUiState(2L, "Lab Report", SubmissionState.SUBMITTING, 1L, 11L),
                QueuedSubmissionItemUiState(3L, "Reflection Journal", SubmissionState.FAILED, 1L, 12L),
            )
        ),
        navController = rememberNavController(),
    )
}
