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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.offlineDisabled
import com.instructure.horizon.horizonui.organisms.navelements.SelectableNavigationItem

data class BottomNavItem(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
    val containsSubPages: Boolean = false
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeNavigationRoute.Dashboard.route, R.string.bottomNav_home, R.drawable.home, R.drawable.home_filled),
    BottomNavItem(HomeNavigationRoute.Learn.route, R.string.bottomNav_learn, R.drawable.book_2, R.drawable.book_2_filled, true),
    BottomNavItem(HomeNavigationRoute.Skillspace.route, R.string.bottomNav_skillspace, R.drawable.hub, R.drawable.hub_filled),
    BottomNavItem(
        HomeNavigationRoute.Account.route,
        R.string.bottomNav_account,
        R.drawable.account_circle,
        R.drawable.account_circle_filled,
        true
    )
)

@Composable
fun isBottomBarVisible(navController: NavHostController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val subPageRoutes = bottomNavItems.filter { it.containsSubPages }.map { it.route }.toSet()
    return bottomNavItems.map { it.route }.contains(currentDestination?.route)
            || currentDestination?.hierarchy?.drop(1)?.any { it.route in subPageRoutes } == true
}

private val offlineDisabledRoutes = setOf(
    HomeNavigationRoute.Skillspace.route,
)

@Composable
fun HomeBottomNavigationBar(
    navController: NavHostController,
    buttonsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<HomeBottomNavigationViewModel>()
    val state by viewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val visible = isBottomBarVisible(navController)

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Surface(shadowElevation = HorizonElevation.level5) {
            NavigationBar(
                containerColor = HorizonColors.Surface.pageSecondary(),
                modifier = modifier
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    val isDisabledOffline = state.isOffline && item.route in offlineDisabledRoutes
                    val itemEnabled = buttonsEnabled && !isDisabledOffline
                    SelectableNavigationItem(
                        item,
                        selected,
                        itemEnabled,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true

                                // Do not restore screen state when navigating to Dashboard screen
                                // Restore when navigating between other screens
                                restoreState = item.route != HomeNavigationRoute.Dashboard.route ||
                                        (item.route == HomeNavigationRoute.Dashboard.route && currentDestination?.route == HomeNavigationRoute.Dashboard.route)
                            }
                        },
                        modifier = Modifier.offlineDisabled(isDisabledOffline)
                    )
                }
            }
        }
    }
}