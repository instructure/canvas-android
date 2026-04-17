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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
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

private val SyncBlue = Color(0xFF09508C)
private val CanvasCareerBlue = Color(0xFF2B7ABC)
private val RemainingBlue = Color(0xFFE0EBF5)
private val TimestampColor = Color(0xFF586874)
private val DividerColor = Color(0xFFE8EAEC)
private val CardBorderColor = Color(0xFFD7DADE)

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
            Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
                CircularProgressIndicator(color = HorizonColors.Surface.institution())
            }
        } else {
            Column(modifier = modifier) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    StorageCard(
                        uiState = uiState,
                        modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    )
                    when (uiState.mode) {
                        ManageOfflineContentMode.SELECTING -> SelectingList(uiState)
                        ManageOfflineContentMode.SYNCING -> SyncingList(uiState)
                        ManageOfflineContentMode.DELETING -> DeletingContent()
                    }
                }
                when (uiState.mode) {
                    ManageOfflineContentMode.SELECTING -> SelectingBottomBar(uiState, navController)
                    ManageOfflineContentMode.SYNCING -> SyncingBottomBar(uiState)
                    ManageOfflineContentMode.DELETING -> Unit
                }
            }
        }
    }
}

// region Storage card

@Composable
private fun StorageCard(uiState: ManageOfflineContentUiState, modifier: Modifier = Modifier) {
    val total = uiState.storageTotalBytes.toFloat().coerceAtLeast(1f)
    val otherFraction = (uiState.storageOtherAppBytes / total).coerceIn(0f, 1f)
    val canvasFraction = (uiState.storageCanvasBytes / total).coerceIn(0f, 1f - otherFraction)
    val remainingFraction = (1f - otherFraction - canvasFraction).coerceAtLeast(0f)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pageSecondary(), RoundedCornerShape(16.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.offline_storageTitle),
                style = HorizonTypography.h4,
                color = HorizonColors.Text.title(),
            )
            if (uiState.storageUsedLabel.isNotEmpty() && uiState.storageTotalLabel.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.offline_storageUsedLabel,
                        uiState.storageUsedLabel,
                        uiState.storageTotalLabel,
                    ),
                    style = HorizonTypography.p3,
                    color = TimestampColor,
                )
            }
        }
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
                        .background(SyncBlue),
                )
            }
            if (canvasFraction > 0f) {
                Box(
                    modifier = Modifier
                        .weight(canvasFraction)
                        .fillMaxSize()
                        .background(CanvasCareerBlue),
                )
            }
            Box(
                modifier = Modifier
                    .weight(remainingFraction.coerceAtLeast(0.01f))
                    .fillMaxSize()
                    .background(RemainingBlue),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StorageLegendDot(color = SyncBlue, label = stringResource(R.string.offline_storageOtherApps))
            StorageLegendDot(color = CanvasCareerBlue, label = stringResource(R.string.offline_storageCanvasCareer))
            StorageLegendDot(color = RemainingBlue, label = stringResource(R.string.offline_storageRemaining))
        }
    }
}

@Composable
private fun StorageLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(text = label, style = HorizonTypography.p3, color = TimestampColor)
    }
}

// endregion

// region Selecting mode

@Composable
private fun SelectingList(uiState: ManageOfflineContentUiState) {
    val allSelected = uiState.courses.isNotEmpty() &&
        uiState.courses.all { it.offlineState == CourseOfflineState.ALL }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        Button(
            label = if (allSelected) {
                stringResource(R.string.offline_deselectAll)
            } else {
                stringResource(R.string.offline_selectAll)
            },
            color = ButtonColor.Ghost,
            onClick = uiState.onSelectAllClick,
        )
        HorizonSpace(SpaceSize.SPACE_8)
        uiState.courses.forEach { course ->
            CourseRow(course)
        }
    }
}

@Composable
private fun SelectingBottomBar(uiState: ManageOfflineContentUiState, navController: NavHostController) {
    val hasSelection = uiState.courses.any { it.offlineState != CourseOfflineState.NONE }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (hasSelection) 8.dp else 0.dp, spotColor = Color(0x2E273540))
            .background(HorizonColors.Surface.pageSecondary())
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Button(
            label = stringResource(R.string.offline_syncButton),
            color = ButtonColor.Black,
            width = ButtonWidth.FILL,
            enabled = hasSelection,
            onClick = uiState.onSyncClick,
        )
        Button(
            label = stringResource(R.string.offline_removeSyncedContentTitle),
            color = ButtonColor.Custom(
                backgroundColor = HorizonColors.Surface.pageSecondary(),
                contentColor = HorizonColors.Text.error(),
                outlineColor = HorizonColors.Surface.error(),
            ),
            width = ButtonWidth.FILL,
            onClick = { navController.navigate(AccountRoute.RemoveSyncedContentConfirmation.route) },
        )
    }
}

// endregion

// region Syncing mode

@Composable
private fun SyncingList(uiState: ManageOfflineContentUiState) {
    Column(modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)) {
        if (uiState.syncProgressLabel.isNotEmpty()) {
            Text(
                text = uiState.syncProgressLabel,
                style = HorizonTypography.p3,
                color = TimestampColor,
            )
            HorizonSpace(SpaceSize.SPACE_8)
        }
        LinearProgressIndicator(
            progress = { uiState.syncProgress },
            modifier = Modifier.fillMaxWidth(),
            color = SyncBlue,
            trackColor = RemainingBlue,
        )
        HorizonSpace(SpaceSize.SPACE_16)
    }
    uiState.courses.forEach { course ->
        SyncingCourseRow(course)
    }
}

@Composable
private fun SyncingBottomBar(uiState: ManageOfflineContentUiState) {
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
        HorizontalDivider(color = DividerColor)
        course.files.forEach { file ->
            SyncingFileRow(file)
            HorizontalDivider(color = DividerColor)
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
            Text(text = file.fileSizeLabel, style = HorizonTypography.p2, color = TimestampColor)
        }
        when (file.syncState) {
            FileSyncState.SYNCING -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = SyncBlue,
            )
            FileSyncState.DONE -> Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = SyncBlue,
                modifier = Modifier.size(24.dp),
            )
            FileSyncState.PENDING -> Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

// endregion

// region Deleting mode

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
            style = HorizonTypography.h3,
            color = HorizonColors.Text.body(),
        )
    }
}

// endregion

// region Course and file rows

@Composable
private fun CourseRow(course: OfflineCourseItemUiState) {
    val toggleableState = when (course.offlineState) {
        CourseOfflineState.ALL -> ToggleableState.On
        CourseOfflineState.NONE -> ToggleableState.Off
        CourseOfflineState.PARTIAL -> ToggleableState.Indeterminate
    }
    val nextOfflineState = if (course.offlineState == CourseOfflineState.ALL) {
        CourseOfflineState.NONE
    } else {
        CourseOfflineState.ALL
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().height(76.dp),
        ) {
            TriStateCheckboxItem(
                state = TriStateCheckboxItemState(
                    controlsContentState = ControlsContentState(title = ""),
                    toggleableState = toggleableState,
                    onClick = { course.onOfflineStateChanged(nextOfflineState) },
                ),
                modifier = Modifier
                    .width(68.dp)
                    .padding(start = 24.dp),
            )
            // Right column: course name + size + chevron, tap to expand/collapse
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { course.onToggleExpanded() }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = course.courseName,
                        style = HorizonTypography.p1,
                        color = HorizonColors.Text.body(),
                    )
                    if (course.courseSizeLabel.isNotEmpty()) {
                        Text(
                            text = course.courseSizeLabel,
                            style = HorizonTypography.p2,
                            color = TimestampColor,
                        )
                    }
                }
                Icon(
                    painter = painterResource(
                        if (course.isExpanded) R.drawable.expand_circle_up else R.drawable.expand_circle_down,
                    ),
                    contentDescription = null,
                    tint = HorizonColors.Icon.default(),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        HorizontalDivider(color = DividerColor)
        AnimatedVisibility(
            visible = course.isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                course.files.forEach { file ->
                    FileRow(file)
                    HorizontalDivider(color = DividerColor)
                }
            }
        }
    }
}

@Composable
private fun FileRow(file: OfflineFileItemUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(76.dp),
    ) {
        CheckboxItem(
            state = CheckboxItemState(
                controlsContentState = ControlsContentState(title = ""),
                checked = file.isSelected,
                onCheckedChanged = file.onSelectionChanged,
            ),
            modifier = Modifier
                .width(68.dp)
                .padding(start = 48.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = file.fileName,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
            Text(
                text = file.fileSizeLabel,
                style = HorizonTypography.p2,
                color = TimestampColor,
            )
        }
    }
}

// endregion

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
                    ),
                ),
                OfflineCourseItemUiState(
                    courseId = 2L,
                    courseName = "Advanced Mathematics",
                    courseSizeLabel = "",
                    offlineState = CourseOfflineState.NONE,
                    isExpanded = false,
                    files = emptyList(),
                ),
            ),
        ),
        navController = rememberNavController(),
    )
}
