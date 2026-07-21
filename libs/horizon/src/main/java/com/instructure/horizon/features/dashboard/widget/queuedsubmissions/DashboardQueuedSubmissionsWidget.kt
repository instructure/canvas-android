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
package com.instructure.horizon.features.dashboard.widget.queuedsubmissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardScheduledSubmissionUiState
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetPageState
import com.instructure.horizon.features.queuedsubmissions.QUEUED_SUBMISSIONS_PATH
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth

private const val MAX_VISIBLE_ROWS = 3

@Composable
fun DashboardQueuedSubmissionsWidget(
    submissions: List<DashboardScheduledSubmissionUiState>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    DashboardWidgetCard(
        title = stringResource(R.string.queuedSubmissions_title),
        iconRes = R.drawable.sync,
        widgetColor = HorizonColors.PrimitivesBlue.blue12(),
        pageState = DashboardWidgetPageState.Empty,
        useMinWidth = false,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            submissions.take(MAX_VISIBLE_ROWS).forEachIndexed { index, submission ->
                if (index > 0) {
                    HorizontalDivider(color = HorizonColors.Surface.divider())
                }
                ScheduledSubmissionRow(
                    submission = submission,
                    onOpen = { navigateToAssignment(navController, submission) },
                )
            }
            HorizonSpace(SpaceSize.SPACE_12)
            Button(
                label = stringResource(R.string.queuedSubmissions_viewAll),
                color = ButtonColor.BlackOutline,
                width = ButtonWidth.FILL,
                onClick = { navigateToList(navController) },
            )
        }
    }
}

@Composable
private fun ScheduledSubmissionRow(
    submission: DashboardScheduledSubmissionUiState,
    onOpen: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = submission.name,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier.weight(1f),
        )
        Button(
            label = stringResource(R.string.queuedSubmissions_cancel),
            color = ButtonColor.Black,
            height = ButtonHeight.SMALL,
            onClick = submission.onCancel,
        )
    }
}

private fun navigateToAssignment(navController: NavHostController, submission: DashboardScheduledSubmissionUiState) {
    val deepLink = NavDeepLinkRequest.Builder
        .fromUri("${ApiPrefs.fullDomain}/courses/${submission.courseId}/assignments/${submission.assignmentId}".toUri())
        .build()
    navController.navigate(deepLink)
}

private fun navigateToList(navController: NavHostController) {
    val deepLink = NavDeepLinkRequest.Builder
        .fromUri("${ApiPrefs.fullDomain}$QUEUED_SUBMISSIONS_PATH".toUri())
        .build()
    val navOptions = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setPopUpTo(navController.graph.findStartDestination().id, false, true)
        .setRestoreState(true)
        .build()
    navController.navigate(deepLink, navOptions)
}

@Preview
@Composable
private fun DashboardQueuedSubmissionsWidgetPreview() {
    DashboardQueuedSubmissionsWidget(
        submissions = listOf(
            DashboardScheduledSubmissionUiState(1L, "Submission name", 1L, 10L),
            DashboardScheduledSubmissionUiState(2L, "Submission name", 1L, 11L),
            DashboardScheduledSubmissionUiState(3L, "Submission name", 1L, 12L),
        ),
        navController = rememberNavController(),
        modifier = Modifier.padding(24.dp),
    )
}
