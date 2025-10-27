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
package com.instructure.horizon.features.dashboard.widget.announcement

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCard
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonRoute
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardChipState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.announcement.card.DashboardAnnouncementBannerCardError
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

@Composable
fun DashboardAnnouncementBannerWidget(
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>
) {
    val viewModel = hiltViewModel<DashboardAnnouncementBannerViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshState.update { it + true }
            state.onRefresh {
                refreshState.update { it - true }
            }
        }
    }

    if (state.state == DashboardItemState.LOADING || state.cardState.items.isNotEmpty()) {
        DashboardAnnouncementBannerSection(state, mainNavController, homeNavController)
    }
}

@Composable
fun DashboardAnnouncementBannerSection(
    state: DashboardAnnouncementBannerUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
) {
    when (state.state) {
        DashboardItemState.LOADING -> {
            DashboardPaginatedWidgetCard(
                state.cardState.copy(
                    items = listOf(
                        DashboardPaginatedWidgetCardItemState(
                            chipState = DashboardPaginatedWidgetCardChipState(
                                label = stringResource(R.string.notificationsAnnouncementCategoryLabel),
                                color = StatusChipColor.Sky
                            ),
                            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                            source = "Institution or Course Name Here",
                            date = Date(),
                            buttonState = DashboardPaginatedWidgetCardButtonState(
                                label = stringResource(R.string.dashboardAnnouncementBannerGoToAnnouncement),
                                height = ButtonHeight.SMALL,
                                width = ButtonWidth.FILL,
                                color = ButtonColor.WhiteWithOutline,
                                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                            )
                        )
                    ),
                    isLoading = true
                ),
                mainNavController,
                homeNavController,
                Modifier.padding(horizontal = 16.dp)
            )
        }
        DashboardItemState.ERROR -> {
            DashboardAnnouncementBannerCardError(
                { state.onRefresh {} },
                Modifier.padding(horizontal = 16.dp)
            )
        }
        DashboardItemState.SUCCESS -> {
            DashboardPaginatedWidgetCard(
                state.cardState,
                mainNavController,
                homeNavController,
                Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
