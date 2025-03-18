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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull

@Composable
fun HomeScreen(parentNavController: NavHostController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val activity = LocalContext.current.getActivityOrNull()
    if (activity != null) ViewStyler.setStatusBarColor(activity, R.color.surface_pagePrimary)
    HorizonTheme {
        Scaffold(content = { padding ->
            HomeNavigation(navController, Modifier.padding(padding))
        }, containerColor = colorResource(R.color.surface_pagePrimary), bottomBar = {
            BottomNavigationBar(navController, currentDestination, parentNavController)
        })
    }
}

data class BottomNavItem(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeNavigationRoute.Dashboard.route, R.string.bottom_nav_home, R.drawable.home, R.drawable.home_filled),
    BottomNavItem(HomeNavigationRoute.Courses.route, R.string.bottom_nav_learn, R.drawable.book_2, R.drawable.book_2_filled),
    BottomNavItem(MainNavigationRoute.AiAssistant.route, R.string.bottom_nav_ai_assist, R.drawable.ai, R.drawable.ai_filled),
    BottomNavItem(HomeNavigationRoute.Skillspace.route, R.string.bottom_nav_skillspace, R.drawable.hub, R.drawable.hub_filled),
    BottomNavItem(
        HomeNavigationRoute.Account.route,
        R.string.bottom_nav_account,
        R.drawable.account_circle,
        R.drawable.account_circle_filled
    )
)

@Composable
private fun BottomNavigationBar(
    homeNavController: NavController,
    currentDestination: NavDestination?,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Surface(shadowElevation = 8.dp) {
        NavigationBar(containerColor = colorResource(R.color.surface_pageSecondary), tonalElevation = 8.dp, modifier = modifier) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                if (item.route == "ai") {
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
            val colorResource = if (selected) R.color.text_surfaceInverseSecondary else R.color.text_body
            val fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            Text(label, color = colorResource(colorResource), fontSize = 12.sp, fontWeight = fontWeight)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = colorResource(R.color.icon_surfaceInverseSecondary),
            unselectedIconColor = colorResource(R.color.icon_medium),
            selectedTextColor = colorResource(R.color.text_surfaceInverseSecondary),
            unselectedTextColor = colorResource(R.color.text_body),
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
            brush = Brush.verticalGradient(
                colors = listOf(
                    colorResource(R.color.ai_gradient_start),
                    colorResource(R.color.ai_gradient_end)
                )
            ),
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
fun HomeScreenPreview() {
    HomeScreen(parentNavController = rememberNavController())
}