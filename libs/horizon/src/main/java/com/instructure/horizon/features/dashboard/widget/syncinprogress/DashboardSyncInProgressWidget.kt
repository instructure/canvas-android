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
package com.instructure.horizon.features.dashboard.widget.syncinprogress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetPageState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle
import com.instructure.horizon.offline.sync.HorizonProgressState

@Composable
fun DashboardSyncInProgressWidget(
    syncProgress: Float,
    progressState: HorizonProgressState,
    onDismiss: () -> Unit,
    navController: NavHostController,
    syncProgressLabel: String = "",
    modifier: Modifier = Modifier,
) {
    val progressColor = when (progressState) {
        HorizonProgressState.COMPLETED -> HorizonColors.Text.success()
        HorizonProgressState.ERROR -> HorizonColors.Text.error()
        else -> HorizonColors.PrimitivesBlue.blue82()
    }
    val displayProgress = if (progressState.isFinished()) 100.0 else (syncProgress * 100).toDouble()

    DashboardWidgetCard(
        title = stringResource(R.string.offline_syncInProgressTitle),
        iconRes = R.drawable.sync,
        widgetColor = HorizonColors.PrimitivesBlue.blue12(),
        pageState = DashboardWidgetPageState.Empty,
        useMinWidth = false,
        onClick = {
            navController.navigate(AccountRoute.SyncingContent.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onClose = onDismiss,
        modifier = modifier,
    ) {
        Column {
            if (syncProgressLabel.isNotEmpty()) {
                Text(
                    text = syncProgressLabel,
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.timestamp(),
                )
                HorizonSpace(SpaceSize.SPACE_8)
            }
            ProgressBarSmall(
                progress = displayProgress,
                style = ProgressBarStyle.Dark(overrideProgressColor = progressColor),
                showLabels = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun DashboardSyncInProgressWidgetPreview() {
    DashboardSyncInProgressWidget(
        syncProgress = 0.4f,
        progressState = HorizonProgressState.IN_PROGRESS,
        syncProgressLabel = "Downloading in progress",
        onDismiss = {},
        navController = rememberNavController(),
        modifier = Modifier.padding(24.dp),
    )
}

@Preview
@Composable
private fun DashboardSyncCompletedWidgetPreview() {
    DashboardSyncInProgressWidget(
        syncProgress = 1f,
        progressState = HorizonProgressState.COMPLETED,
        syncProgressLabel = "64 MB downloaded successfully",
        onDismiss = {},
        navController = rememberNavController(),
        modifier = Modifier.padding(24.dp),
    )
}

@Preview
@Composable
private fun DashboardSyncErrorWidgetPreview() {
    DashboardSyncInProgressWidget(
        syncProgress = 0.6f,
        progressState = HorizonProgressState.ERROR,
        syncProgressLabel = "Some files could not be downloaded",
        onDismiss = {},
        navController = rememberNavController(),
        modifier = Modifier.padding(24.dp),
    )
}
