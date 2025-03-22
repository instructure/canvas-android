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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.HorizonTheme
import com.instructure.horizon.MainNavigationRoute
import com.instructure.horizon.R
import com.instructure.horizon.design.foundation.Colors
import com.instructure.horizon.design.molecules.Spinner
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull

data class BottomNavItem(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeNavigationRoute.Dashboard.route, R.string.bottomNav_home, R.drawable.home, R.drawable.home_filled),
    BottomNavItem(HomeNavigationRoute.Courses.route, R.string.bottomNav_learn, R.drawable.book_2, R.drawable.book_2_filled),
    BottomNavItem(MainNavigationRoute.AiAssistant.route, R.string.bottomNav_aiAssist, R.drawable.ai, R.drawable.ai_filled),
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
    if (activity != null) ViewStyler.setStatusBarColor(activity, R.color.surface_pagePrimary)

    LaunchedEffect(key1 = uiState.theme) {
        val theme = uiState.theme
        if (theme != null && activity != null && !ThemePrefs.isThemeApplied) ThemePrefs.applyCanvasTheme(theme, activity)
    }
    HorizonTheme {
        Scaffold(content = { padding ->
            if (uiState.initialDataLoading) {
                Spinner(modifier = Modifier.fillMaxSize())
            } else {
                HomeNavigation(navController, Modifier.padding(padding))
            }
        }, containerColor = Colors.Surface.pagePrimary(), bottomBar = {
            BottomNavigationBar(navController, currentDestination, parentNavController)
        })
    }
}

@Composable
private fun BottomNavigationBar(
    homeNavController: NavController,
    currentDestination: NavDestination?,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Surface(shadowElevation = 8.dp) {
        NavigationBar(containerColor = Colors.Surface.pageSecondary(), tonalElevation = 8.dp, modifier = modifier) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                if (item.route == MainNavigationRoute.AiAssistant.route) {
                    AiAssistantItem(item, onClick = {
                        // This will be changed later because in this case we don't have the current screen in the background
                        mainNavController.navigate(item.route)
                    })
                } else {
                    SelectableNavigationItem(item, selected, onClick = {
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
fun RowScope.SelectableNavigationItem(item: BottomNavItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(item.label)
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(painter = painterResource(if (selected) item.selectedIcon else item.icon), contentDescription = label)
        },
        label = {
            val color = if (selected) Colors.Text.surfaceInverseSecondary() else Colors.Text.body()
            val fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            Text(label, color = color, fontSize = 12.sp, fontWeight = fontWeight)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Colors.Icon.surfaceInverseSecondary(),
            unselectedIconColor = Colors.Icon.medium(),
            selectedTextColor = Colors.Text.surfaceInverseSecondary(),
            unselectedTextColor = Colors.Text.body(),
            indicatorColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Composable
fun RowScope.AiAssistantItem(item: BottomNavItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .requiredSize(48.dp)
        .weight(1f)
        .background(
            brush = Colors.Surface.aiGradient(),
            shape = RoundedCornerShape(500.dp)
        )
        .clickable { onClick() }) {
        Icon(
            painter = painterResource(R.drawable.ai),
            tint = Color.White,
            contentDescription = stringResource(item.label),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(parentNavController = rememberNavController(), viewModel = viewModel())
}