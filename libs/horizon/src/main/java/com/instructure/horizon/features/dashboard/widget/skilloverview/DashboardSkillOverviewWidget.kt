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
package com.instructure.horizon.features.dashboard.widget.skilloverview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.skilloverview.card.DashboardSkillOverviewCardContent
import com.instructure.horizon.features.dashboard.widget.skilloverview.card.DashboardSkillOverviewCardError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun DashboardSkillOverviewWidget(
    homeNavController: NavHostController,
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>
) {
    val viewModel = hiltViewModel<DashboardSkillOverviewViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshState.update { it + true }
            state.onRefresh {
                refreshState.update { it - true }
            }
        }
    }

    DashboardSkillOverviewSection(state, homeNavController)
}

@Composable
fun DashboardSkillOverviewSection(
    state: DashboardSkillOverviewUiState,
    homeNavController: NavHostController,
) {
    when (state.state) {
        DashboardItemState.LOADING -> {
            DashboardSkillOverviewCardContent(
                state.cardState,
                homeNavController,
                isLoading = true
            )
        }
        DashboardItemState.ERROR -> {
            DashboardSkillOverviewCardError(
                { state.onRefresh {} }
            )
        }
        DashboardItemState.SUCCESS -> {
            DashboardSkillOverviewCardContent(
                state.cardState,
                homeNavController,
                isLoading = false
            )
        }
    }
}
