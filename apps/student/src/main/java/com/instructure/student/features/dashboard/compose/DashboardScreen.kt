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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.rememberWithRequireNetwork
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.features.dashboard.DashboardNavigationHandler
import com.instructure.pandautils.features.dashboard.compose.DashboardBody
import com.instructure.pandautils.features.dashboard.compose.DashboardUiState
import com.instructure.pandautils.features.dashboard.compose.DashboardViewModel
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun DashboardScreen(
    navigationHandler: DashboardNavigationHandler
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    DashboardScreenContent(
        uiState = uiState,
        refreshSignal = viewModel.refreshSignal,
        snackbarMessageFlow = viewModel.snackbarMessage,
        onShowSnackbar = viewModel::showSnackbar,
        navigationHandler = navigationHandler
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreenContent(
    uiState: DashboardUiState,
    refreshSignal: SharedFlow<Unit>,
    snackbarMessageFlow: SharedFlow<SnackbarMessage>,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    navigationHandler: DashboardNavigationHandler
) {
    val activity = LocalActivity.current
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

    var showMenu by remember { mutableStateOf(false) }

    val manageOfflineContentClick = rememberWithRequireNetwork {
        navigationHandler.handleDashboardNavigation(DashboardNavigationEvent.Dashboard.NavigateToManageOfflineContent)
    }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.backgroundLight)),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.dashboard),
                navIconRes = R.drawable.ic_hamburger,
                navIconContentDescription = stringResource(id = R.string.navigation_drawer_open),
                navigationActionClick = { (activity as? NavigationActivity)?.openNavigationDrawer() },
                actions = {
                    OverflowMenu(
                        showMenu = showMenu,
                        onDismissRequest = { showMenu = !showMenu },
                        iconColor = Color(ThemePrefs.primaryTextColor),
                        modifier = Modifier
                            .background(color = colorResource(id = R.color.backgroundLightestElevated))
                    ) {
                        DropdownMenuItem(onClick = {
                            showMenu = !showMenu
                            manageOfflineContentClick()
                        }) {
                            Text(
                                stringResource(R.string.course_menu_manage_offline_content),
                                color = colorResource(id = R.color.textDarkest)
                            )
                        }
                        DropdownMenuItem(onClick = {
                            showMenu = !showMenu
                            navigationHandler.handleDashboardNavigation(DashboardNavigationEvent.Dashboard.NavigateToCustomizeDashboard)
                        }) {
                            Text(
                                stringResource(R.string.customize_dashboard),
                                color = colorResource(id = R.color.textDarkest)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionColor = Color(ThemePrefs.textButtonColor)
                )
            }
        }
    ) { paddingValues ->
        DashboardBody(paddingValues, pullRefreshState, uiState, refreshSignal, onShowSnackbar, navigationHandler)
    }
}