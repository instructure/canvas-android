/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.instructure.horizon.features.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.course.DashboardCourseSection
import com.instructure.horizon.features.dashboard.widget.timespent.DashboardTimeSpentWidget
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.navigation.MainNavigationRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

const val DASHBOARD_REFRESH = "refreshDashboard"
const val DASHBOARD_SNACKBAR = "dashboardSnackbar"

@Composable
fun DashboardScreen(uiState: DashboardUiState, mainNavController: NavHostController, homeNavController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }

    val parentEntry = remember(mainNavController.currentBackStackEntry) { mainNavController.getBackStackEntry("home") }
    val savedStateHandle = parentEntry.savedStateHandle

    val externalRefreshFlow = remember { savedStateHandle.getStateFlow(DASHBOARD_REFRESH, false) }
    val externalRefreshState by externalRefreshFlow.collectAsState()
    var shouldRefresh by rememberSaveable { mutableStateOf(false) }

    val snackbarFlow = remember { savedStateHandle.getStateFlow(DASHBOARD_SNACKBAR, "") }
    val snackbar by snackbarFlow.collectAsState()

    /*
    Using a list of booleans to represent each refreshing component.
    Components get the `shouldRefresh` flag to start refreshing on pull-to-refresh.
    Each component append the `refreshStateFlow` with `true` when starting to refresh and remove it when done.
    If any component is refreshing, the dashboard shows the refreshing indicator.
     */
    val refreshStateFlow = remember { MutableStateFlow(emptyList<Boolean>()) }
    val refreshState by refreshStateFlow.collectAsState()

    NotificationPermissionRequest()

    LaunchedEffect(shouldRefresh, externalRefreshState) {
        if (shouldRefresh || externalRefreshState) {
            savedStateHandle[DASHBOARD_REFRESH] = false
            delay(50)
            shouldRefresh = false
        }
    }

    LaunchedEffect(snackbar) {
        if (snackbar.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = snackbar,
            )
            savedStateHandle[DASHBOARD_SNACKBAR] = ""
        }
    }

    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val pullToRefreshState = rememberPullToRefreshState()
        val isRefreshing = refreshState.any { it }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { shouldRefresh = true },
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    isRefreshing = isRefreshing,
                    containerColor = HorizonColors.Surface.pageSecondary(),
                    color = HorizonColors.Surface.institution(),
                    state = pullToRefreshState
                )
            }
        ){
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                HomeScreenTopBar(
                    uiState,
                    mainNavController,
                    modifier = Modifier.height(56.dp)
                )
                HorizonSpace(SpaceSize.SPACE_24)
                DashboardCourseSection(
                    mainNavController,
                    homeNavController,
                    shouldRefresh,
                    refreshStateFlow
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp)
                ) {
                    DashboardTimeSpentWidget(
                        shouldRefresh,
                        refreshStateFlow
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeScreenTopBar(uiState: DashboardUiState, mainNavController: NavController, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.Bottom, modifier = modifier.padding(horizontal = 24.dp)) {
        GlideImage(
            model = uiState.logoUrl,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 44.dp)
                .shimmerEffect(uiState.logoUrl.isEmpty()),
            contentDescription = stringResource(R.string.a11y_institutionLogoContentDescription),
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            iconRes = R.drawable.menu_book_notebook,
            contentDescription = stringResource(R.string.a11y_dashboardNotebookButtonContentDescription),
            onClick = {
                mainNavController.navigate(MainNavigationRoute.Notebook.route)
            },
            color = IconButtonColor.Inverse,
            elevation = HorizonElevation.level4,
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.notifications,
            contentDescription = stringResource(R.string.a11y_dashboardNotificationsContentDescription),
            onClick = {
                mainNavController.navigate(MainNavigationRoute.Notification.route)
            },
            elevation = HorizonElevation.level4,
            color = IconButtonColor.Inverse,
            badge = if (uiState.unreadCountState.unreadNotifications > 0) {
                {
                    Badge(
                        content = BadgeContent.Color,
                        type = BadgeType.Inverse
                    )
                }
            } else null
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.mail,
            contentDescription = stringResource(R.string.a11y_dashboardInboxContentDescription),
            onClick = { mainNavController.navigate(MainNavigationRoute.Inbox.route) },
            elevation = HorizonElevation.level4,
            color = IconButtonColor.Inverse,
            badge = if (uiState.unreadCountState.unreadConversations > 0) {
                {
                    Badge(
                        content = BadgeContent.Color,
                        type = BadgeType.Inverse
                    )
                }
            } else null
        )
    }
}

@Composable
private fun NotificationPermissionRequest() {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var isPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionRequest = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { result ->
        hasNotificationPermission = result
    }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && !isPermissionRequested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionRequested = true
            permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
@Preview
private fun DashboardScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardScreen(DashboardUiState(), mainNavController = rememberNavController(), homeNavController = rememberNavController())
}