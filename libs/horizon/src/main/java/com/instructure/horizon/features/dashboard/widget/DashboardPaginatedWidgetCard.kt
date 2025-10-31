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
package com.instructure.horizon.features.dashboard.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.horizon.horizonui.organisms.AnimatedHorizontalPager
import com.instructure.pandautils.utils.localisedFormat
import java.util.Date


@Composable
fun DashboardPaginatedWidgetCard(
    state: DashboardPaginatedWidgetCardState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { state.items.size })

    if (state.items.isNotEmpty()) {

        AnimatedHorizontalPager(
            pagerState,
            modifier,
            sizeAnimationRange = 0f,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp,
            verticalAlignment = Alignment.CenterVertically,
        ) { index, modifier ->
            DashboardCard(
                modifier = modifier.padding(bottom = 16.dp),
                onClick = {
                    state.items[index].route?.let { route ->
                        when (route) {
                            is DashboardPaginatedWidgetCardButtonRoute.HomeRoute -> {
                                homeNavController.navigate(route.route)
                            }
                            is DashboardPaginatedWidgetCardButtonRoute.MainRoute -> {
                                mainNavController.navigate(route.route)
                            }
                        }
                    }
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    HorizonSpace(SpaceSize.SPACE_24)

                    DashboardPaginatedWidgetCardItem(
                        item = state.items[index],
                        isLoading = state.isLoading,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .semantics(mergeDescendants = true) {
                                role = Role.Button
                            }
                    )

                    HorizonSpace(SpaceSize.SPACE_24)
                }
            }
        }
    }
}

@Composable
private fun DashboardPaginatedWidgetCardItem(
    item: DashboardPaginatedWidgetCardItemState,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (item.chipState != null || item.pageState != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item.chipState?.let { chipState ->
                    StatusChip(
                        state = StatusChipState(
                            label = chipState.label,
                            color = chipState.color,
                            fill = true
                        ),
                        modifier = Modifier.shimmerEffect(
                            isLoading,
                            backgroundColor = chipState.color.fillColor.copy(0.8f),
                            shimmerColor = chipState.color.fillColor.copy(0.5f)
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                item.pageState?.let {
                    Text(
                        it,
                        style = HorizonTypography.p2,
                        color = HorizonColors.Text.dataPoint(),
                    )
                }
            }
            HorizonSpace(SpaceSize.SPACE_16)
        }
        item.source?.let { source ->
            Text(
                text = stringResource(
                    R.string.dashboardAnnouncementBannerFrom,
                    source
                ),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.dataPoint(),
                modifier = Modifier.shimmerEffect(isLoading)
            )
            HorizonSpace(SpaceSize.SPACE_4)
        }
        item.date?.let { date ->
            Text(
                text = date.localisedFormat("MMM dd, yyyy"),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp(),
                modifier = Modifier.shimmerEffect(isLoading)
            )
            HorizonSpace(SpaceSize.SPACE_8)
        }

        item.title?.let { title ->
            Text(
                text = title,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier
                    .fillMaxWidth()
                    .shimmerEffect(isLoading)
            )

            HorizonSpace(SpaceSize.SPACE_16)
        }
    }
}

@Composable
@Preview
private fun DashboardPaginatedWidgetCardAnnouncementContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardPaginatedWidgetCard(
        state = DashboardPaginatedWidgetCardState(
            items = listOf(
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                    source = "Institution or Course Name Here",
                    date = Date(),
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                ),
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Second announcement with different content to show pagination.",
                    source = "Another Course Name",
                    date = Date(),
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                ),
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Third global announcement without a source.",
                    date = Date(),
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                )
            )
        ),
        rememberNavController(),
        rememberNavController(),
    )
}

@Composable
@Preview
private fun DashboardPaginatedWidgetCardAnnouncementLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardPaginatedWidgetCard(
        state = DashboardPaginatedWidgetCardState(
            items = listOf(
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                    source = "Institution or Course Name Here",
                    date = Date(),
                    route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                ),
            ),
            isLoading = true
        ),
        rememberNavController(),
        rememberNavController(),
    )
}