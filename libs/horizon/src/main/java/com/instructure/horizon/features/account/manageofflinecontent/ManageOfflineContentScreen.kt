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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ManageOfflineContentScreen(
    uiState: ManageOfflineContentUiState,
    navController: NavHostController,
) {
    HorizonScaffold(
        title = stringResource(R.string.offline_manageOfflineContentTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (uiState.storageUsedLabel.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.offline_storageUsedLabel, uiState.storageUsedLabel),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.body(),
                )
                HorizonSpace(SpaceSize.SPACE_16)
            }

            when (uiState.mode) {
                ManageOfflineContentMode.SYNCING -> SyncingContent(uiState)
                ManageOfflineContentMode.DELETING -> DeletingContent(uiState, navController)
                ManageOfflineContentMode.SELECTING -> SelectingContent(uiState, navController)
            }
        }
    }
}

@Composable
private fun SelectingContent(uiState: ManageOfflineContentUiState, navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        uiState.courses.forEach { course ->
            CourseItem(course)
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                label = stringResource(R.string.offline_manageOfflineContentButton),
                width = ButtonWidth.FILL,
                onClick = uiState.onSyncClick,
                modifier = Modifier.weight(1f),
            )
            Button(
                label = stringResource(R.string.offline_removeSyncedContentTitle),
                color = ButtonColor.Danger,
                width = ButtonWidth.FILL,
                onClick = { navController.navigate(AccountRoute.RemoveSyncedContentConfirmation.route) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SyncingContent(uiState: ManageOfflineContentUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.offline_syncInProgressTitle),
            style = HorizonTypography.h4,
            color = HorizonColors.Text.body(),
        )
        Text(
            text = stringResource(R.string.offline_syncInProgressDescription),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
        )
        LinearProgressIndicator(
            progress = { uiState.syncProgress },
            modifier = Modifier.fillMaxWidth(),
            color = HorizonColors.Surface.institution(),
        )
    }
}

@Composable
private fun DeletingContent(uiState: ManageOfflineContentUiState, navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        uiState.courses.forEach { course ->
            CourseItem(course)
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Button(
            label = stringResource(R.string.offline_removeSyncedContentConfirm),
            color = ButtonColor.Danger,
            width = ButtonWidth.FILL,
            onClick = { navController.navigate(AccountRoute.RemoveSyncedContentConfirmation.route) },
            modifier = Modifier.fillMaxWidth(),
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
                    controlsContentState = ControlsContentState(title = course.courseName),
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
            storageUsedLabel = "1.2 GB",
            courses = listOf(
                OfflineCourseItemUiState(
                    courseId = 1L,
                    courseName = "Introduction to Biology",
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
