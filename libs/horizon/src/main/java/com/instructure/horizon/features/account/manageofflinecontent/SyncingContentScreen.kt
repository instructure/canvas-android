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
package com.instructure.horizon.features.account.manageofflinecontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold

@Composable
fun SyncingContentScreen(
    uiState: SyncingContentUiState,
    navController: NavHostController,
) {
    HorizonScaffold(
        title = stringResource(R.string.offline_manageOfflineContentTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        Column(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)) {
                    if (uiState.syncProgressLabel.isNotEmpty()) {
                        Text(
                            text = uiState.syncProgressLabel,
                            style = HorizonTypography.p3,
                            color = HorizonColors.Text.timestamp(),
                        )
                        HorizonSpace(SpaceSize.SPACE_8)
                    }
                    ProgressBarSmall(
                        progress = (uiState.syncProgress * 100).toDouble(),
                        style = ProgressBarStyle.Dark(overrideProgressColor = HorizonColors.PrimitivesBlue.blue82()),
                        showLabels = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HorizonSpace(SpaceSize.SPACE_16)
                }
                uiState.courses.forEach { course ->
                    SyncingCourseRow(course)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Button(
                    label = stringResource(R.string.offline_cancelSync),
                    color = ButtonColor.BlackOutline,
                    width = ButtonWidth.FILL,
                    onClick = uiState.onCancelSyncClick,
                )
            }
        }
    }
}

@Composable
private fun SyncingCourseRow(course: OfflineCourseItemUiState) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = course.courseName,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = HorizonColors.Surface.divider())
        course.files.forEach { file ->
            SyncingFileRow(file)
            HorizontalDivider(color = HorizonColors.Surface.divider())
        }
    }
}

@Composable
private fun SyncingFileRow(file: OfflineFileItemUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(horizontal = 24.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = file.fileName, style = HorizonTypography.p1, color = HorizonColors.Text.body())
            Text(text = file.fileSizeLabel, style = HorizonTypography.p2, color = HorizonColors.Text.timestamp())
        }
        when (file.syncState) {
            FileSyncState.SYNCING -> Spinner(
                size = SpinnerSize.EXTRA_SMALL,
                modifier = Modifier.size(24.dp),
            )
            FileSyncState.DONE -> Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = stringResource(R.string.offline_syncStatusSynced),
                tint = HorizonColors.PrimitivesBlue.blue82(),
                modifier = Modifier.size(24.dp),
            )
            FileSyncState.PENDING -> Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Preview
@Composable
private fun SyncingContentScreenPreview() {
    SyncingContentScreen(
        uiState = SyncingContentUiState(
            syncProgress = 0.4f,
            syncProgressLabel = "Downloading 40 MB of 100 MB",
            courses = listOf(
                OfflineCourseItemUiState(
                    courseId = 1L,
                    courseName = "Introduction to Biology",
                    files = listOf(
                        OfflineFileItemUiState(1L, "Chapter 1.pdf", "12 MB", syncState = FileSyncState.DONE),
                        OfflineFileItemUiState(2L, "Chapter 2.pdf", "8 MB", syncState = FileSyncState.SYNCING),
                        OfflineFileItemUiState(3L, "Chapter 3.pdf", "10 MB", syncState = FileSyncState.PENDING),
                    ),
                ),
            ),
        ),
        navController = rememberNavController(),
    )
}
