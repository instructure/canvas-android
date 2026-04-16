/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.pandautils.features.dashboard.compose

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.features.dashboard.DashboardNavigationHandler
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.conferences.ConferencesWidget
import com.instructure.pandautils.features.dashboard.widget.courseinvitation.CourseInvitationsWidget
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidget
import com.instructure.pandautils.features.dashboard.widget.forecast.ForecastWidget
import com.instructure.pandautils.features.dashboard.widget.institutionalannouncements.InstitutionalAnnouncementsWidget
import com.instructure.pandautils.features.dashboard.widget.progress.ProgressWidget
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidget
import com.instructure.pandautils.features.dashboard.widget.welcome.WelcomeWidget
import com.instructure.pandautils.utils.ThemedColor
import kotlinx.coroutines.flow.SharedFlow

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun DashboardBody(
    paddingValues: PaddingValues,
    pullRefreshState: PullRefreshState,
    uiState: DashboardUiState,
    refreshSignal: SharedFlow<Unit>,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    navigationHandler: DashboardNavigationHandler,
    headerContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .background(colorResource(R.color.backgroundLight))
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
                Loading(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("loading")
                )
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
                WidgetList(
                    widgets = uiState.widgets,
                    refreshSignal = refreshSignal,
                    onShowSnackbar = onShowSnackbar,
                    navigationHandler = navigationHandler,
                    color = uiState.color,
                    headerContent = headerContent,
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun WidgetList(
    widgets: List<WidgetMetadata>,
    refreshSignal: SharedFlow<Unit>,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    navigationHandler: DashboardNavigationHandler,
    color: ThemedColor,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null,
) {
    val activity = LocalActivity.current ?: return
    val windowSizeClass = calculateWindowSizeClass(activity = activity)

    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        if (headerContent != null) {
            headerContent()
        }
        widgets.forEach { metadata ->
            GetWidgetComposable(
                metadata.id,
                refreshSignal,
                columns,
                onShowSnackbar,
                navigationHandler,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        CustomizeDashboardButton(
            onClick = { navigationHandler.handleDashboardNavigation(DashboardNavigationEvent.Dashboard.NavigateToCustomizeDashboard) },
            color = Color(color.color()),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
    }
}

@Composable
private fun GetWidgetComposable(
    widgetId: String,
    refreshSignal: SharedFlow<Unit>,
    columns: Int,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    navigationHandler: DashboardNavigationHandler,
    modifier: Modifier = Modifier
) {
    return when (widgetId) {
        WidgetMetadata.WIDGET_ID_PROGRESS -> ProgressWidget(
            refreshSignal = refreshSignal,
            columns = columns,
            onShowSnackbar = onShowSnackbar,
            onNavigationEvent = navigationHandler::handleProgressNavigation,
            modifier = modifier
        )

        WidgetMetadata.WIDGET_ID_CONFERENCES -> ConferencesWidget(
            refreshSignal = refreshSignal,
            columns = columns,
            onShowSnackbar = onShowSnackbar,
            onNavigationEvent = navigationHandler::handleConferencesNavigation,
            modifier = modifier
        )

        WidgetMetadata.WIDGET_ID_WELCOME -> WelcomeWidget(refreshSignal = refreshSignal, modifier = modifier)
        WidgetMetadata.WIDGET_ID_COURSES -> CoursesWidget(
            refreshSignal = refreshSignal,
            columns = columns,
            onNavigationEvent = navigationHandler::handleCoursesNavigation,
            modifier = modifier
        )
        WidgetMetadata.WIDGET_ID_COURSE_INVITATIONS -> CourseInvitationsWidget(
            refreshSignal = refreshSignal,
            columns = columns,
            onShowSnackbar = onShowSnackbar,
            modifier = modifier
        )

        WidgetMetadata.WIDGET_ID_INSTITUTIONAL_ANNOUNCEMENTS -> InstitutionalAnnouncementsWidget(
            refreshSignal = refreshSignal,
            columns = columns,
            onAnnouncementClick = { subject, message ->
                navigationHandler.handleDashboardNavigation(
                    DashboardNavigationEvent.Dashboard.NavigateToGlobalAnnouncement(subject, message)
                )
            },
            modifier = modifier
        )

        WidgetMetadata.WIDGET_ID_FORECAST -> ForecastWidget(
            refreshSignal = refreshSignal,
            onNavigationEvent = navigationHandler::handleForecastNavigation,
            modifier = modifier
        )
        WidgetMetadata.WIDGET_ID_TODO -> TodoWidget(
            refreshSignal = refreshSignal,
            onShowSnackbar = onShowSnackbar,
            onNavigationEvent = navigationHandler::handleTodoNavigation,
            modifier = modifier
        )

        else -> {}
    }
}

@Composable
fun CustomizeDashboardButton(
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = color
            ),
            border = BorderStroke(1.dp, color),
            contentPadding = PaddingValues(start = 8.dp, end = 12.dp, top = 0.dp, bottom = 0.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit_new),
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp).size(16.dp),
                tint = color
            )
            Text(
                text = stringResource(R.string.customize_dashboard),
                color = color
            )
        }
    }
}
