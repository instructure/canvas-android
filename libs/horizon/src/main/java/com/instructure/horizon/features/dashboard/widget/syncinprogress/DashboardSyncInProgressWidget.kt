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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetPageState
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

private val SyncIconBackgroundColor = Color(0xFFE6EDF3)
private val SyncProgressColor = Color(0xFF09508C)
private val SyncProgressTrackColor = Color(0xFFE0EBF5)
private val SyncProgressLabelColor = Color(0xFF586874)

@Composable
fun DashboardSyncInProgressWidget(
    syncProgress: Float,
    syncProgressLabel: String = "",
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    DashboardWidgetCard(
        title = stringResource(R.string.offline_syncInProgressTitle),
        iconRes = R.drawable.cloud_sync,
        widgetColor = SyncIconBackgroundColor,
        pageState = DashboardWidgetPageState.Empty,
        useMinWidth = false,
        onClick = { navController.navigate(AccountRoute.ManageOfflineContent.route) },
        modifier = modifier,
    ) {
        Column {
            if (syncProgressLabel.isNotEmpty()) {
                Text(
                    text = syncProgressLabel,
                    style = HorizonTypography.p2,
                    color = SyncProgressLabelColor,
                )
                HorizonSpace(SpaceSize.SPACE_8)
            }
            LinearProgressIndicator(
                progress = { syncProgress },
                modifier = Modifier.fillMaxWidth(),
                color = SyncProgressColor,
                trackColor = SyncProgressTrackColor,
            )
        }
    }
}

@Preview
@Composable
private fun DashboardSyncInProgressWidgetPreview() {
    DashboardSyncInProgressWidget(
        syncProgress = 0.4f,
        syncProgressLabel = "Downloading 40 MB of 100 MB",
        navController = rememberNavController(),
        modifier = Modifier.padding(24.dp),
    )
}