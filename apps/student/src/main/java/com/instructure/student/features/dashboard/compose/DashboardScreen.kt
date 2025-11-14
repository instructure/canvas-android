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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.courseinvitation.CourseInvitationsWidget
import com.instructure.student.features.dashboard.widget.welcome.WelcomeWidget
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    DashboardScreenContent(
        uiState = uiState,
        refreshSignal = viewModel.refreshSignal
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreenContent(
    uiState: DashboardUiState,
    refreshSignal: SharedFlow<Unit>
) {
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

                uiState.widgets.isEmpty() -> {
                    EmptyContent(
                        emptyMessage = stringResource(id = R.string.noCoursesSubtext),
                        imageRes = R.drawable.ic_panda_nocourses,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("emptyContent")
                    )
                }

                else -> {
                    WidgetGrid(
                        widgets = uiState.widgets,
                        refreshSignal = refreshSignal,
                        modifier = Modifier.fillMaxSize()
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun WidgetGrid(
    widgets: List<WidgetMetadata>,
    refreshSignal: SharedFlow<Unit>,
    modifier: Modifier = Modifier
) {
    val activity = LocalActivity.current ?: return
    val windowSizeClass = calculateWindowSizeClass(activity = activity)

    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        items(widgets) { metadata ->
            GetWidgetComposable(metadata.id, refreshSignal)
        }
    }
}

@Composable
private fun GetWidgetComposable(
    widgetId: String,
    refreshSignal: SharedFlow<Unit>
) {
    return when (widgetId) {
        "welcome" -> WelcomeWidget(refreshSignal = refreshSignal)
        "course_invitations" -> CourseInvitationsWidget(refreshSignal = refreshSignal)
        else -> {}
    }
}