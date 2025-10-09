/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.dashboard.widget.skilloverview.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun DashboardSkillOverviewCardContent(
    state: DashboardSkillOverviewCardState,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    DashboardWidgetCard(
        title = stringResource(R.string.dashboardSkillOverviewTitle),
        iconRes = R.drawable.hub,
        widgetColor = HorizonColors.PrimitivesGreen.green12(),
        useMinWidth = true,
        onClick = {
            homeNavController.navigate(HomeNavigationRoute.Skillspace.route) {
                popUpTo(homeNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = modifier
    ) {
        if (state.completedSkillCount == 0) {
            Text(
                text = stringResource(R.string.dashboardSkillOverviewNoDataMessage),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp(),
                modifier = Modifier.width(IntrinsicSize.Max)
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.completedSkillCount.toString(),
                    style = HorizonTypography.h1.copy(fontSize = 38.sp, letterSpacing = 0.sp),
                    color = HorizonColors.Text.body()
                )
                Text(
                    text = stringResource(R.string.dashboardSkillOverviewEarnedLabel),
                    style = HorizonTypography.labelMediumBold,
                    color = HorizonColors.Text.title()
                )
            }
        }
    }
}

@Composable
@Preview
private fun DashboardSkillOverviewCardContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardSkillOverviewCardContent(
        state = DashboardSkillOverviewCardState(completedSkillCount = 24),
        rememberNavController()
    )
}

@Composable
@Preview
private fun DashboardSkillOverviewCardContentNoDataPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardSkillOverviewCardContent(
        state = DashboardSkillOverviewCardState(completedSkillCount = 0),
        rememberNavController()
    )
}
