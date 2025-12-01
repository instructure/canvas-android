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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.courseinvitation.CourseInvitationsWidget
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidget
import com.instructure.pandautils.features.dashboard.widget.welcome.WelcomeWidget
import com.instructure.pandautils.features.dashboard.widget.institutionalannouncements.InstitutionalAnnouncementsWidget
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun DashboardScreen(router: DashboardRouter) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    DashboardScreenContent(
        uiState = uiState,
        refreshSignal = viewModel.refreshSignal,
        snackbarMessageFlow = viewModel.snackbarMessage,
        onShowSnackbar = viewModel::showSnackbar,
        router = router
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreenContent(
    uiState: DashboardUiState,
    refreshSignal: SharedFlow<Unit>,
    snackbarMessageFlow: SharedFlow<SnackbarMessage>,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    router: DashboardRouter
) {
    val activity = LocalActivity.current
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.refreshing,
        onRefresh = uiState.onRefresh
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        snackbarMessageFlow.collect { snackbarMessage ->
            val actionLabel = if (snackbarMessage.action != null) snackbarMessage.actionLabel else null
            val result = snackbarHostState.showSnackbar(
                message = snackbarMessage.message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                snackbarMessage.action?.invoke()
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.backgroundLightest)),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.dashboard),
                navIconRes = R.drawable.ic_hamburger,
                navIconContentDescription = stringResource(id = R.string.navigation_drawer_open),
                navigationActionClick = { (activity as? NavigationActivity)?.openNavigationDrawer() }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                        onShowSnackbar = onShowSnackbar,
                        router = router,
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
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    router: DashboardRouter,
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
        contentPadding = PaddingValues(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        items(
            items = widgets,
            span = { metadata ->
                if (metadata.isFullWidth) {
                    StaggeredGridItemSpan.FullLine
                } else {
                    StaggeredGridItemSpan.SingleLane
                }
            }
        ) { metadata ->
            GetWidgetComposable(metadata.id, refreshSignal, columns, onShowSnackbar, router)
        }
    }
}

@Composable
private fun GetWidgetComposable(
    widgetId: String,
    refreshSignal: SharedFlow<Unit>,
    columns: Int,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    router: DashboardRouter
) {
    return when (widgetId) {
        WidgetMetadata.WIDGET_ID_WELCOME -> WelcomeWidget(refreshSignal = refreshSignal)
        WidgetMetadata.WIDGET_ID_COURSES -> CoursesWidget(refreshSignal = refreshSignal)
        WidgetMetadata.WIDGET_ID_COURSE_INVITATIONS -> CourseInvitationsWidget(
            refreshSignal = refreshSignal,
            columns = columns,
            onShowSnackbar = onShowSnackbar
        )
        WidgetMetadata.WIDGET_ID_INSTITUTIONAL_ANNOUNCEMENTS -> InstitutionalAnnouncementsWidget(
            refreshSignal = refreshSignal,
            columns = columns,
            onAnnouncementClick = router::routeToGlobalAnnouncement
        )
        else -> {}
    }
}