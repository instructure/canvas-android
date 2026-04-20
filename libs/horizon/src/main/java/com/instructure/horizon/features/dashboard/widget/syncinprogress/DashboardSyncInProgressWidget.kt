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
        widgetColor = HorizonColors.PrimitivesBlue.blue12(),
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
                    color = HorizonColors.Text.timestamp(),
                )
                HorizonSpace(SpaceSize.SPACE_8)
            }
            ProgressBarSmall(
                progress = (syncProgress * 100).toDouble(),
                style = ProgressBarStyle.Dark(overrideProgressColor = HorizonColors.PrimitivesBlue.blue82()),
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
        syncProgressLabel = "Downloading 40 MB of 100 MB",
        navController = rememberNavController(),
        modifier = Modifier.padding(24.dp),
    )
}