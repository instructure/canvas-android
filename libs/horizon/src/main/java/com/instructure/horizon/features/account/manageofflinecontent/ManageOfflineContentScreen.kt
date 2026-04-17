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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.organisms.controls.CheckboxItem
import com.instructure.horizon.horizonui.organisms.controls.CheckboxItemState
import com.instructure.horizon.horizonui.organisms.controls.ControlsContentState
import com.instructure.horizon.horizonui.organisms.controls.TriStateCheckboxItem
import com.instructure.horizon.horizonui.organisms.controls.TriStateCheckboxItemState
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold

private val OtherAppsColor = Color(0xFF586874)
private val CanvasCareerColor = Color(0xFF09508C)
private val RemainingColor = Color(0xFFE0EBF5)

@Composable
fun ManageOfflineContentScreen(
    uiState: ManageOfflineContentUiState,
    navController: NavHostController,
) {
    HorizonScaffold(
        title = stringResource(R.string.offline_manageOfflineContentTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize(),
            ) {
                CircularProgressIndicator(color = HorizonColors.Surface.institution())
            }
        } else {
            Column(
                modifier = modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                StorageCard(uiState)
                HorizonSpace(SpaceSize.SPACE_24)

                when (uiState.mode) {
                    ManageOfflineContentMode.SYNCING -> SyncingContent(uiState)
                    ManageOfflineContentMode.DELETING -> DeletingContent()
                    ManageOfflineContentMode.SELECTING -> SelectingContent(uiState, navController)
                }
            }
        }
    }
}

@Composable
private fun StorageCard(uiState: ManageOfflineContentUiState) {
    val total = uiState.storageTotalBytes.toFloat().coerceAtLeast(1f)
    val otherFraction = (uiState.storageOtherAppBytes / total).coerceIn(0f, 1f)
    val canvasFraction = (uiState.storageCanvasBytes / total).coerceIn(0f, 1f - otherFraction)
    val remainingFraction = (1f - otherFraction - canvasFraction).coerceAtLeast(0f)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            if (otherFraction > 0f) {
                Box(
                    modifier = Modifier
                        .weight(otherFraction)
                        .fillMaxSize()
                        .background(OtherAppsColor)
                )
            }
            if (canvasFraction > 0f) {
                Box(
                    modifier = Modifier
                        .weight(canvasFraction)
                        .fillMaxSize()
                        .background(CanvasCareerColor)
                )
            }
            Box(
                modifier = Modifier
                    .weight(remainingFraction.coerceAtLeast(0.01f))
                    .fillMaxSize()
                    .background(RemainingColor)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StorageLegendItem(color = OtherAppsColor, label = stringResource(R.string.offline_storageOtherApps))
            StorageLegendItem(color = CanvasCareerColor, label = stringResource(R.string.offline_storageCanvasCareer))
            StorageLegendItem(color = RemainingColor, label = stringResource(R.string.offline_storageRemaining))
        }
        if (uiState.storageUsedLabel.isNotEmpty() && uiState.storageTotalLabel.isNotEmpty()) {
            Text(
                text = stringResource(R.string.offline_storageUsedLabel, uiState.storageUsedLabel, uiState.storageTotalLabel),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.body(),
            )
        }
    }
}

@Composable
private fun StorageLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = HorizonTypography.p2,
            color = HorizonColors.Text.body(),
        )
    }
}

@Composable
private fun SelectingContent(uiState: ManageOfflineContentUiState, navController: NavHostController) {
    val hasSelection = uiState.courses.any { it.offlineState != CourseOfflineState.NONE }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.offline_selectAll),
            style = HorizonTypography.p1.copy(textDecoration = TextDecoration.Underline),
            color = HorizonColors.Text.title(),
            modifier = Modifier.clickable { uiState.onSelectAllClick() },
        )
        uiState.courses.forEach { course ->
            CourseItem(course)
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                label = stringResource(R.string.offline_syncButton),
                width = ButtonWidth.FILL,
                onClick = uiState.onSyncClick,
                enabled = hasSelection,
                modifier = Modifier.weight(1f),
            )
            Button(
                label = stringResource(R.string.offline_removeSyncedContentTitle),
                color = ButtonColor.DangerInverse,
                width = ButtonWidth.FILL,
                onClick = { navController.navigate(AccountRoute.RemoveSyncedContentConfirmation.route) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SyncingContent(uiState: ManageOfflineContentUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.syncProgressLabel.isNotEmpty()) {
            Text(
                text = uiState.syncProgressLabel,
                style = HorizonTypography.p2,
                color = Color(0xFF586874),
            )
        }
        LinearProgressIndicator(
            progress = { uiState.syncProgress },
            modifier = Modifier.fillMaxWidth(),
            color = CanvasCareerColor,
            trackColor = RemainingColor,
        )
        HorizonSpace(SpaceSize.SPACE_8)
        uiState.courses.forEach { course ->
            SyncingCourseItem(course)
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Button(
            label = stringResource(R.string.offline_cancelSync),
            color = ButtonColor.BlackOutline,
            width = ButtonWidth.FILL,
            onClick = uiState.onCancelSyncClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SyncingCourseItem(course: OfflineCourseItemUiState) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            Text(
                text = course.courseName,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier.weight(1f),
            )
        }
        course.files.forEach { file ->
            SyncingFileItem(file)
        }
    }
}

@Composable
private fun SyncingFileItem(file: OfflineFileItemUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
    ) {
        when (file.syncState) {
            FileSyncState.SYNCING -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = CanvasCareerColor,
            )
            FileSyncState.DONE -> Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = CanvasCareerColor,
                modifier = Modifier.size(16.dp),
            )
            FileSyncState.PENDING -> Spacer(modifier = Modifier.size(16.dp))
        }
        Text(
            text = file.fileName,
            style = HorizonTypography.p2,
            color = HorizonColors.Text.body(),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = file.fileSizeLabel,
            style = HorizonTypography.p2,
            color = Color(0xFF586874),
        )
    }
}

@Composable
private fun DeletingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = HorizonColors.Surface.institution(),
        )
        HorizonSpace(SpaceSize.SPACE_16)
        Text(
            text = stringResource(R.string.offline_deletingContent),
            style = HorizonTypography.h4,
            color = HorizonColors.Text.body(),
        )
    }
}

@Composable
private fun CourseItem(course: OfflineCourseItemUiState) {
    val toggleableState = when (course.offlineState) {
        CourseOfflineState.ALL -> ToggleableState.On
        CourseOfflineState.NONE -> ToggleableState.Off
        CourseOfflineState.PARTIAL -> ToggleableState.Indeterminate
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { course.onToggleExpanded() },
        ) {
            TriStateCheckboxItem(
                state = TriStateCheckboxItemState(
                    controlsContentState = ControlsContentState(
                        title = course.courseName,
                        description = course.courseSizeLabel.ifEmpty { null },
                    ),
                    toggleableState = toggleableState,
                    onClick = {
                        val next = if (course.offlineState == CourseOfflineState.ALL) {
                            CourseOfflineState.NONE
                        } else {
                            CourseOfflineState.ALL
                        }
                        course.onOfflineStateChanged(next)
                    },
                ),
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(
                    if (course.isExpanded) R.drawable.expand_circle_up else R.drawable.expand_circle_down
                ),
                contentDescription = null,
                tint = HorizonColors.Icon.default(),
            )
        }

        AnimatedVisibility(visible = course.isExpanded) {
            Column(modifier = Modifier.padding(start = 32.dp)) {
                course.files.forEach { file ->
                    CheckboxItem(
                        state = CheckboxItemState(
                            controlsContentState = ControlsContentState(
                                title = file.fileName,
                                description = file.fileSizeLabel,
                            ),
                            checked = file.isSelected,
                            onCheckedChanged = file.onSelectionChanged,
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ManageOfflineContentScreenPreview() {
    ManageOfflineContentScreen(
        uiState = ManageOfflineContentUiState(
            storageOtherAppBytes = 2_000_000_000L,
            storageCanvasBytes = 500_000_000L,
            storageTotalBytes = 16_000_000_000L,
            storageUsedLabel = "2.5 GB",
            storageTotalLabel = "16 GB",
            courses = listOf(
                OfflineCourseItemUiState(
                    courseId = 1L,
                    courseName = "Introduction to Biology",
                    courseSizeLabel = "20 MB",
                    offlineState = CourseOfflineState.PARTIAL,
                    isExpanded = true,
                    files = listOf(
                        OfflineFileItemUiState(1L, "Chapter 1.pdf", "12 MB", isSelected = true),
                        OfflineFileItemUiState(2L, "Chapter 2.pdf", "8 MB", isSelected = false),
                    )
                )
            )
        ),
        navController = rememberNavController(),
    )
}