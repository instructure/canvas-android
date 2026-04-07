/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.instructure.ngc.R
import com.instructure.ngc.designsystem.DSIconButton
import com.instructure.ngc.designsystem.DSIconButtonColor
import com.instructure.ngc.designsystem.NGCTypography
import com.instructure.ngc.navigation.NGCComposeNavigationHandler
import com.instructure.pandautils.R as PandaR
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.compose.composables.rememberWithRequireNetwork
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.features.dashboard.DashboardNavigationHandler
import com.instructure.pandautils.features.dashboard.compose.DashboardBody
import com.instructure.pandautils.features.dashboard.compose.DashboardUiState
import com.instructure.pandautils.features.dashboard.compose.DashboardViewModel
import com.instructure.pandautils.utils.ThemePrefs
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun NGCDashboardScreen(navController: NavHostController) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val navigationHandler = remember { NGCComposeNavigationHandler(navController) }

    NGCDashboardScreenContent(
        uiState = uiState,
        refreshSignal = viewModel.refreshSignal,
        snackbarMessageFlow = viewModel.snackbarMessage,
        onShowSnackbar = viewModel::showSnackbar,
        navigationHandler = navigationHandler
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NGCDashboardScreenContent(
    uiState: DashboardUiState,
    refreshSignal: SharedFlow<Unit>,
    snackbarMessageFlow: SharedFlow<SnackbarMessage>,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    navigationHandler: DashboardNavigationHandler
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.refreshing,
        onRefresh = uiState.onRefresh
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        snackbarMessageFlow.collect { snackbarMessage ->
            val actionLabel =
                if (snackbarMessage.action != null) snackbarMessage.actionLabel else null
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

    val backgroundColor = colorResource(PandaR.color.backgroundLight)

    Scaffold(
        modifier = Modifier.background(backgroundColor),
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionColor = Color(ThemePrefs.textButtonColor)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            DashboardBody(
                paddingValues = PaddingValues(),
                pullRefreshState = pullRefreshState,
                uiState = uiState,
                refreshSignal = refreshSignal,
                onShowSnackbar = onShowSnackbar,
                navigationHandler = navigationHandler,
                headerContent = {
                    NGCDashboardTopBar(
                        navigationHandler = navigationHandler,
                        modifier = Modifier
                            .background(backgroundColor)
                            .height(56.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun NGCDashboardTopBar(navigationHandler: DashboardNavigationHandler, modifier: Modifier = Modifier) {
    val manageOfflineContentClick = rememberWithRequireNetwork {
        navigationHandler.handleDashboardNavigation(DashboardNavigationEvent.Dashboard.NavigateToManageOfflineContent)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 16.dp)
    ) {
        Text(text = stringResource(R.string.ngc_dashboardTitle), style = NGCTypography.h2)
        Spacer(modifier = Modifier.weight(1f))
        DSIconButton(
            iconRes = R.drawable.cloud_download,
            contentDescription = stringResource(R.string.a11y_dashboardNotebookButtonContentDescription),
            onClick = {
                manageOfflineContentClick()
            },
            color = DSIconButtonColor.Inverse,
            elevation = 4.dp,
            modifier = Modifier.padding(end = 8.dp)
        )
        DSIconButton(
            iconRes = R.drawable.edit,
            contentDescription = stringResource(R.string.a11y_dashboardInboxContentDescription),
            onClick = { navigationHandler.handleDashboardNavigation(DashboardNavigationEvent.Dashboard.NavigateToCustomizeDashboard) },
            elevation = 4.dp,
            color = DSIconButtonColor.Inverse,
        )
    }
}
