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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCard
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCardError
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetPageState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

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

    if (state.state != DashboardItemState.SUCCESS || state.cardState.items.isNotEmpty()) {
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
                DashboardPaginatedWidgetCardState.Loading,
                mainNavController,
                homeNavController,
            )
        }
        DashboardItemState.ERROR -> {
            DashboardWidgetCardError(
                stringResource(R.string.notificationsAnnouncementCategoryLabel),
                R.drawable.campaign,
                HorizonColors.Surface.institution().copy(alpha = 0.1f),
                false,
                DashboardWidgetPageState.Empty,
                { state.onRefresh {} },
            )
        }
        DashboardItemState.SUCCESS -> {
            DashboardPaginatedWidgetCard(
                state.cardState,
                mainNavController,
                homeNavController,
            )
        }
    }
}
