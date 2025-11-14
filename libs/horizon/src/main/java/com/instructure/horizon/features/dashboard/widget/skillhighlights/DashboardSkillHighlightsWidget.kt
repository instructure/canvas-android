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
package com.instructure.horizon.features.dashboard.widget.skillhighlights

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
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCardError
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.DashboardSkillHighlightsCardContent
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.DashboardSkillHighlightsCardState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun DashboardSkillHighlightsWidget(
    homeNavController: NavHostController,
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<DashboardSkillHighlightsViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshState.update { it + true }
            state.onRefresh {
                refreshState.update { it - true }
            }
        }
    }

    DashboardSkillHighlightsSection(state, homeNavController, modifier)
}

@Composable
fun DashboardSkillHighlightsSection(
    state: DashboardSkillHighlightsUiState,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    when (state.state) {
        DashboardItemState.LOADING -> {
            DashboardSkillHighlightsCardContent(
                DashboardSkillHighlightsCardState.Loading,
                homeNavController,
                true,
                modifier.padding(horizontal = 24.dp),
            )
        }
        DashboardItemState.ERROR -> {
            DashboardWidgetCardError(
                stringResource(R.string.dashboardSkillHighlightsTitle),
                R.drawable.hub,
                HorizonColors.PrimitivesGreen.green12(),
                false,
                { state.onRefresh {} },
                modifier = modifier.padding(horizontal = 24.dp)
            )
        }
        DashboardItemState.SUCCESS -> {
            DashboardSkillHighlightsCardContent(
                state.cardState,
                homeNavController,
                false,
                modifier.padding(horizontal = 24.dp),
            )
        }
    }
}
