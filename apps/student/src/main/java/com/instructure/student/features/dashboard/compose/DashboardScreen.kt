/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.dashboard.compose

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity

@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    DashboardScreenContent(uiState = uiState)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreenContent(uiState: DashboardUiState) {
    val activity = LocalActivity.current
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.refreshing,
        onRefresh = uiState.onRefresh
    )

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.backgroundLightest)),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.dashboard),
                navIconRes = R.drawable.ic_hamburger,
                navIconContentDescription = stringResource(id = R.string.navigation_drawer_open),
                navigationActionClick = { (activity as? NavigationActivity)?.openNavigationDrawer() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .background(colorResource(R.color.backgroundLightest))
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            when {
                uiState.error != null -> {
                    ErrorContent(
                        errorMessage = uiState.error,
                        retryClick = uiState.onRetry,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("errorContent")
                    )
                }

                uiState.loading -> {
                    Loading(modifier = Modifier
                        .fillMaxSize()
                        .testTag("loading"))
                }

                else -> {
                    EmptyContent(
                        emptyMessage = stringResource(id = R.string.noCoursesSubtext),
                        imageRes = R.drawable.ic_panda_nocourses,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("emptyContent")
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.refreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .testTag("dashboardPullRefreshIndicator")
            )
        }
    }
}