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
@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.horizon.features.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.features.aiassistant.AiAssistantScreen
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.navelements.SelectableNavigationItem
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull

data class BottomNavItem(
    val route: String?,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeNavigationRoute.Dashboard.route, R.string.bottomNav_home, R.drawable.home, R.drawable.home_filled),
    BottomNavItem(HomeNavigationRoute.Learn.route, R.string.bottomNav_learn, R.drawable.book_2, R.drawable.book_2_filled),
    BottomNavItem(null, R.string.bottomNav_aiAssist, R.drawable.ai, R.drawable.ai_filled),
    BottomNavItem(HomeNavigationRoute.Skillspace.route, R.string.bottomNav_skillspace, R.drawable.hub, R.drawable.hub_filled),
    BottomNavItem(
        HomeNavigationRoute.Account.route,
        R.string.bottomNav_account,
        R.drawable.account_circle,
        R.drawable.account_circle_filled
    )
)

@Composable
fun HomeScreen(parentNavController: NavHostController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val activity = LocalContext.current.getActivityOrNull()
    if (activity != null) ViewStyler.setStatusBarColor(activity, ContextCompat.getColor(activity, R.color.surface_pagePrimary))

    LaunchedEffect(key1 = uiState.theme) {
        val theme = uiState.theme
        if (theme != null && activity != null && !ThemePrefs.isThemeApplied) ThemePrefs.applyCanvasTheme(theme, activity)
    }
    Scaffold(content = { padding ->
        if (uiState.initialDataLoading) {
            val spinnerColor =
                if (ThemePrefs.isThemeApplied) HorizonColors.Surface.institution() else HorizonColors.Surface.inverseSecondary()
            Spinner(modifier = Modifier.fillMaxSize(), color = spinnerColor)
        } else {
            if (uiState.showAiAssist) {
                AiAssistantScreen(AiAssistContext(), navController, { uiState.updateShowAiAssist(false) })
            }
            HomeNavigation(navController, parentNavController, Modifier.padding(padding))
        }
    }, containerColor = HorizonColors.Surface.pagePrimary(), bottomBar = {
        BottomNavigationBar(navController, currentDestination, !uiState.initialDataLoading, { uiState.updateShowAiAssist(it) })
    })
}

@Composable
private fun BottomNavigationBar(
    homeNavController: NavController,
    currentDestination: NavDestination?,
    buttonsEnabled: Boolean,
    updateShowAiAssist: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(shadowElevation = HorizonElevation.level5) {
        NavigationBar(containerColor = HorizonColors.Surface.pageSecondary(), modifier = modifier) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                if (item.route == null) {
                    AiAssistantItem(item, buttonsEnabled, onClick = {
                        updateShowAiAssist(true)
                    })
                } else {
                    SelectableNavigationItem(item, selected, buttonsEnabled, onClick = {
                        homeNavController.navigate(item.route) {
                            popUpTo(homeNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun RowScope.AiAssistantItem(item: BottomNavItem, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        modifier = modifier
            .requiredSize(44.dp)
            .weight(1f),
        onClick = onClick,
        contentDescription = stringResource(item.label),
        iconRes = R.drawable.ai,
        color = IconButtonColor.Ai,
        enabled = enabled
    )
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(parentNavController = rememberNavController(), viewModel = viewModel())
}