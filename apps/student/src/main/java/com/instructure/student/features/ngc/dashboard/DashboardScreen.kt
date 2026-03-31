/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.features.ngc.dashboard

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.organisms.scaffolds.CollapsableHeaderScreen
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.compose.composables.rememberWithRequireNetwork
import com.instructure.pandautils.features.dashboard.DefaultDashboardRouter
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.R
import com.instructure.student.features.dashboard.compose.DashboardBody
import com.instructure.student.features.dashboard.compose.DashboardUiState
import com.instructure.student.features.dashboard.compose.DashboardViewModel
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    DashboardScreenContent(
        uiState = uiState,
        refreshSignal = viewModel.refreshSignal,
        snackbarMessageFlow = viewModel.snackbarMessage,
        onShowSnackbar = viewModel::showSnackbar,
        router = DefaultDashboardRouter() // TODO Handle routing later
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

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.backgroundLight)),
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
        CollapsableHeaderScreen(
            statusBarColor = colorResource(R.color.backgroundLight),
            modifier = Modifier.padding(paddingValues),
            headerContent = { paddingValues ->
                    DashboardTopBar(
                        router,
                        modifier = Modifier
                            .background(colorResource(R.color.backgroundLight))
                            .padding(paddingValues)
                            .height(56.dp)
                    )
            },
            bodyContent = {
                DashboardBody(paddingValues, pullRefreshState, uiState, refreshSignal, onShowSnackbar, router)
            })
    }
}

@Composable
private fun DashboardTopBar(router: DashboardRouter, modifier: Modifier = Modifier) {
    val manageOfflineContentClick = rememberWithRequireNetwork {
        router.routeToManageOfflineContent()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 16.dp)
    ) {
        Text(text = stringResource(R.string.ngc_dashboardTitle), style = HorizonTypography.h2)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            iconRes = R.drawable.cloud_download,
            contentDescription = stringResource(com.instructure.horizon.R.string.a11y_dashboardNotebookButtonContentDescription), // TODO
            onClick = {
                manageOfflineContentClick()
            },
            color = IconButtonColor.Inverse,
            elevation = HorizonElevation.level4,
            modifier = Modifier.padding(end = 8.dp)
        )
        IconButton(
            iconRes = R.drawable.edit,
            contentDescription = stringResource(com.instructure.horizon.R.string.a11y_dashboardInboxContentDescription), // TODO
            onClick = { router.routeToCustomizeDashboard() },
            elevation = HorizonElevation.level4,
            color = IconButtonColor.Inverse,
        )
    }
}