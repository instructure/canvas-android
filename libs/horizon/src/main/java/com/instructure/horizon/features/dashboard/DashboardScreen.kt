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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetPageState
import com.instructure.horizon.features.dashboard.widget.announcement.DashboardAnnouncementBannerWidget
import com.instructure.horizon.features.dashboard.widget.course.DashboardCourseSection
import com.instructure.horizon.features.dashboard.widget.myprogress.DashboardMyProgressWidget
import com.instructure.horizon.features.dashboard.widget.skillhighlights.DashboardSkillHighlightsWidget
import com.instructure.horizon.features.dashboard.widget.skilloverview.DashboardSkillOverviewWidget
import com.instructure.horizon.features.dashboard.widget.timespent.DashboardTimeSpentWidget
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.isWideLayout
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.organisms.AnimatedHorizontalPager
import com.instructure.horizon.horizonui.organisms.CollapsableHeaderScreen
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.horizon.util.HorizonEdgeToEdgeSystemBars
import com.instructure.horizon.util.bottomNavigationScreenInsets
import com.instructure.horizon.util.horizontalSafeDrawing
import com.instructure.horizon.util.zeroScreenInsets
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun DashboardScreen(uiState: DashboardUiState, mainNavController: NavHostController, homeNavController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }

    var shouldRefresh by rememberSaveable { mutableStateOf(false) }

    /*
    Using a list of booleans to represent each refreshing component.
    Components get the `shouldRefresh` flag to start refreshing on pull-to-refresh.
    Each component append the `refreshStateFlow` with `true` when starting to refresh and remove it when done.
    If any component is refreshing, the dashboard shows the refreshing indicator.
     */
    val refreshStateFlow = remember { MutableStateFlow(emptyList<Boolean>()) }
    val refreshState by refreshStateFlow.collectAsState()

    NotificationPermissionRequest()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            shouldRefresh = false
        }
    }

    LaunchedEffect(uiState.externalShouldRefresh) {
        if (uiState.externalShouldRefresh) {
            shouldRefresh = true
            uiState.updateExternalShouldRefresh(false)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = uiState.snackbarMessage,
            )
            if (result == SnackbarResult.Dismissed) {
                uiState.onSnackbarDismiss()
            }
        }
    }

    HorizonEdgeToEdgeSystemBars {
        Scaffold(
            contentWindowInsets = WindowInsets.zeroScreenInsets,
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
                            .padding(top = 56.dp),
                        isRefreshing = isRefreshing,
                        containerColor = HorizonColors.Surface.pageSecondary(),
                        color = HorizonColors.Surface.institution(),
                        state = pullToRefreshState
                    )
                }
            ) {
                val scrollState = rememberScrollState()
                CollapsableHeaderScreen(
                    modifier = Modifier.padding(paddingValues),
                    headerContent = {
                        Column(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.bottomNavigationScreenInsets)
                        ) {
                            HomeScreenTopBar(
                                uiState,
                                mainNavController,
                                modifier = Modifier
                                    .height(56.dp)
                                    .padding(bottom = 12.dp)
                            )
                        }
                    },
                    bodyContent = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
                                .verticalScroll(scrollState)
                        ) {
                            HorizonSpace(SpaceSize.SPACE_12)
                            DashboardAnnouncementBannerWidget(
                                mainNavController,
                                homeNavController,
                                shouldRefresh,
                                refreshStateFlow
                            )
                            DashboardCourseSection(
                                mainNavController,
                                homeNavController,
                                shouldRefresh,
                                refreshStateFlow
                            )
                            HorizonSpace(SpaceSize.SPACE_16)
                            NumericWidgetRow(shouldRefresh, refreshStateFlow, homeNavController)
                            DashboardSkillHighlightsWidget(
                                homeNavController,
                                shouldRefresh,
                                refreshStateFlow
                            )
                            HorizonSpace(SpaceSize.SPACE_24)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreenTopBar(uiState: DashboardUiState, mainNavController: NavController, modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .padding(horizontal = 24.dp)
    ) {
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
            iconRes = R.drawable.edit_note,
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
private fun NumericWidgetRow(
    shouldRefresh: Boolean,
    refreshStateFlow: MutableStateFlow<List<Boolean>>,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val pageCount = 3
        val pagerState = rememberPagerState{ pageCount }
        if (this.isWideLayout) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp)
            ) {
                DashboardMyProgressWidget(
                    shouldRefresh,
                    refreshStateFlow,
                    DashboardWidgetPageState.Empty,
                    Modifier.width(IntrinsicSize.Max)
                )
                DashboardTimeSpentWidget(
                    shouldRefresh,
                    refreshStateFlow,
                    DashboardWidgetPageState.Empty,
                    Modifier.width(IntrinsicSize.Max)
                )
                DashboardSkillOverviewWidget(
                    homeNavController,
                    shouldRefresh,
                    refreshStateFlow,
                    DashboardWidgetPageState.Empty,
                    Modifier.width(IntrinsicSize.Max)
                )
            }
        } else {
            AnimatedHorizontalPager(
                pagerState,
                sizeAnimationRange = 0f,
                beyondViewportPageCount = 3,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 12.dp,
                verticalAlignment = Alignment.CenterVertically,
            ) { index, modifier ->
                when (index) {
                    0 -> {
                        DashboardMyProgressWidget(
                            shouldRefresh,
                            refreshStateFlow,
                            DashboardWidgetPageState(index + 1, pageCount),
                            modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    1 -> {
                        DashboardTimeSpentWidget(
                            shouldRefresh,
                            refreshStateFlow,
                            DashboardWidgetPageState(index + 1, pageCount),
                            modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    2 -> {
                        DashboardSkillOverviewWidget(
                            homeNavController,
                            shouldRefresh,
                            refreshStateFlow,
                            DashboardWidgetPageState(index + 1, pageCount),
                            modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    else -> {

                    }
                }
            }
        }
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