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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.instructure.horizon.R
import com.instructure.horizon.features.aiassistant.AiAssistantScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.organisms.navelements.SelectableNavigationItem

data class BottomNavItem(
    val route: String?,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
    val containsSubPages: Boolean = false
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeNavigationRoute.Dashboard.route, R.string.bottomNav_home, R.drawable.home, R.drawable.home_filled),
    BottomNavItem(HomeNavigationRoute.Learn.route, R.string.bottomNav_learn, R.drawable.book_2, R.drawable.book_2_filled, true),
    BottomNavItem(null, R.string.bottomNav_aiAssist, R.drawable.ai, R.drawable.ai_filled),
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
fun isBottomBarVisible(navController: NavController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    return bottomNavItems.map { it.route }.contains(currentDestination?.route)
            || bottomNavItems.filter { it.containsSubPages }.map { it.route }.contains(currentDestination?.parent?.route)
}

@Composable
fun HomeBottomNavigationBar(
    navController: NavController,
    buttonsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isAiAssistVisible by rememberSaveable { mutableStateOf(false) }
    if (isAiAssistVisible) {
        AiAssistantScreen { isAiAssistVisible = false }
    }
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
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == item.route } == true
                    if (item.route == null) {
                        AiAssistantItem(item, buttonsEnabled, onClick = {
                            isAiAssistVisible = true
                        })
                    } else {
                        SelectableNavigationItem(item, selected, buttonsEnabled, onClick = {
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
                        })
                    }
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