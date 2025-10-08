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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.DashboardSkillHighlightsCardContent
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.DashboardSkillHighlightsCardError
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.DashboardSkillHighlightsCardLoading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun DashboardSkillHighlightsWidget(
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>
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

    DashboardSkillHighlightsSection(state)
}

@Composable
fun DashboardSkillHighlightsSection(
    state: DashboardSkillHighlightsUiState
) {
    when (state.state) {
        DashboardItemState.LOADING -> {
            DashboardSkillHighlightsCardLoading(Modifier.padding(horizontal = 16.dp))
        }
        DashboardItemState.ERROR -> {
            DashboardSkillHighlightsCardError(
                { state.onRefresh {} },
                Modifier.padding(horizontal = 16.dp)
            )
        }
        DashboardItemState.SUCCESS -> {
            DashboardSkillHighlightsCardContent(
                state.cardState,
                Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
