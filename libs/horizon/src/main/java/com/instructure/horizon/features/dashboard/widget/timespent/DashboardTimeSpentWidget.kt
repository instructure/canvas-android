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
package com.instructure.horizon.features.dashboard.widget.timespent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCardError
import com.instructure.horizon.features.dashboard.widget.timespent.card.DashboardTimeSpentCardContent
import com.instructure.horizon.features.dashboard.widget.timespent.card.DashboardTimeSpentCardState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun DashboardTimeSpentWidget(
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<DashboardTimeSpentViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshState.update { it + true }
            state.onRefresh {
                refreshState.update { it - true }
            }
        }
    }

    DashboardTimeSpentSection(state, modifier)
}

@Composable
fun DashboardTimeSpentSection(
    state: DashboardTimeSpentUiState,
    modifier: Modifier = Modifier
) {
    when (state.state) {
        DashboardItemState.LOADING -> {
            DashboardTimeSpentCardContent(
                DashboardTimeSpentCardState.Loading,
                true,
                modifier
            )
        }
        DashboardItemState.ERROR -> {
            DashboardWidgetCardError(
                stringResource(R.string.dashboardTimeSpentTitle),
                R.drawable.schedule,
                HorizonColors.PrimitivesHoney.honey12(),
                false,
                { state.onRefresh {} },
                modifier = modifier
            )
        }
        DashboardItemState.SUCCESS -> {
            DashboardTimeSpentCardContent(
                state.cardState,
                false,
                modifier
            )
        }
    }
}
