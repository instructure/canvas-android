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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(HorizonColors.Surface.pageSecondary())
                        .padding(top = 32.dp, start = 32.dp, end = 32.dp, bottom = 16.dp),
                ) {
                    if (uiState.syncProgressLabel.isNotEmpty()) {
                        Text(
                            text = uiState.syncProgressLabel,
                            style = HorizonTypography.p3,
                            color = HorizonColors.Text.dataPoint(),
                        )
                        HorizonSpace(SpaceSize.SPACE_8)
                    }
                    ProgressBarSmall(
                        progress = (uiState.syncProgress * 100).toDouble(),
                        style = ProgressBarStyle.Dark(overrideProgressColor = HorizonColors.PrimitivesBlue.blue82()),
                        showLabels = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HorizonColors.Surface.pageSecondary()),
                ) {
                    uiState.courses.forEach { course ->
                        SyncingCourseRow(course)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HorizonColors.Surface.pageSecondary())
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(HorizonColors.Surface.pageSecondary())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName,
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body(),
                )
                if (course.courseSizeLabel.isNotEmpty()) {
                    Text(
                        text = course.courseSizeLabel,
                        style = HorizonTypography.p2,
                        color = HorizonColors.Text.timestamp(),
                    )
                }
            }
            when (course.syncState) {
                CourseSyncState.SYNCING -> Spinner(
                    size = SpinnerSize.EXTRA_SMALL,
                    modifier = Modifier.size(24.dp),
                )
                CourseSyncState.PENDING -> Spinner(
                    size = SpinnerSize.EXTRA_SMALL,
                    modifier = Modifier.size(24.dp),
                )
                CourseSyncState.DONE -> Icon(
                    painter = painterResource(R.drawable.check_circle),
                    contentDescription = stringResource(R.string.offline_syncStatusSynced),
                    tint = HorizonColors.PrimitivesBlue.blue82(),
                    modifier = Modifier.size(24.dp),
                )
                CourseSyncState.ERROR -> Icon(
                    painter = painterResource(R.drawable.error),
                    contentDescription = null,
                    tint = HorizonColors.Surface.attention(),
                    modifier = Modifier.size(24.dp),
                )
            }
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
            .background(HorizonColors.Surface.pageSecondary())
            .padding(start = 48.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = file.fileName, style = HorizonTypography.p1, color = HorizonColors.Text.body())
            if (file.fileSizeLabel.isNotEmpty()) {
                Text(text = file.fileSizeLabel, style = HorizonTypography.p2, color = HorizonColors.Text.timestamp())
            }
        }
        when (file.syncState) {
            FileSyncState.SYNCING -> Spinner(
                size = SpinnerSize.EXTRA_SMALL,
                modifier = Modifier.size(24.dp),
            )
            FileSyncState.PENDING -> Spinner(
                size = SpinnerSize.EXTRA_SMALL,
                modifier = Modifier.size(24.dp),
            )
            FileSyncState.DONE -> Icon(
                painter = painterResource(R.drawable.check_circle),
                contentDescription = stringResource(R.string.offline_syncStatusSynced),
                tint = HorizonColors.PrimitivesBlue.blue82(),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SyncingContentScreenPreview() {
    SyncingContentScreen(
        uiState = SyncingContentUiState(
            syncProgress = 0.2f,
            syncProgressLabel = "Downloading 12.7 MB of 64 MB",
            courses = listOf(
                OfflineCourseItemUiState(
                    courseId = 1L,
                    courseName = "Lorem Ipsum Dolor Course",
                    courseSizeLabel = "~64 MB",
                    syncState = CourseSyncState.SYNCING,
                    files = listOf(
                        OfflineFileItemUiState(1L, "file-name.pdf", "~64 MB", syncState = FileSyncState.DONE),
                        OfflineFileItemUiState(2L, "file-name.doc", "~64 MB", syncState = FileSyncState.SYNCING),
                    ),
                ),
            ),
        ),
        navController = rememberNavController(),
    )
}
