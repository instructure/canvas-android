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
package com.instructure.horizon.features.dashboard.widget.myprogress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCardError
import com.instructure.horizon.features.dashboard.widget.myprogress.card.DashboardMyProgressCardContent
import com.instructure.horizon.horizonui.foundation.HorizonColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun DashboardMyProgressWidget(
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>
) {
    val viewModel = hiltViewModel<DashboardMyProgressViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshState.update { it + true }
            state.onRefresh {
                refreshState.update { it - true }
            }
        }
    }

    DashboardMyProgressSection(state)
}

@Composable
fun DashboardMyProgressSection(
    state: DashboardMyProgressUiState
) {
    when (state.state) {
        DashboardItemState.LOADING -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                DashboardMyProgressCardContent(
                    state.cardState,
                    isLoading = true
                )
            }
        }
        DashboardItemState.ERROR -> {
            DashboardWidgetCardError(
                stringResource(R.string.dashboardMyProgressTitle),
                R.drawable.trending_up,
                HorizonColors.PrimitivesSky.sky12,
                true,
                { state.onRefresh {} },
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .padding(bottom = 8.dp)
            )
        }
        DashboardItemState.SUCCESS -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                DashboardMyProgressCardContent(
                    state.cardState,
                    isLoading = false
                )
            }
        }
    }
}
