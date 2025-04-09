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
package com.instructure.horizon.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.instructure.horizon.features.account.AccountScreen
import com.instructure.horizon.features.dashboard.DashboardScreen
import com.instructure.horizon.features.dashboard.DashboardViewModel
import com.instructure.horizon.features.learn.LearnScreen
import com.instructure.horizon.features.learn.LearnViewModel
import com.instructure.horizon.features.skillspace.SkillspaceScreen
import com.instructure.horizon.horizonui.showroom.ShowroomContent
import com.instructure.horizon.horizonui.showroom.ShowroomItem
import com.instructure.horizon.horizonui.showroom.showroomItems

sealed class HomeNavigationRoute(val route: String) {
    data object Dashboard : HomeNavigationRoute("dashboard")
    data object Learn : HomeNavigationRoute("learn")
    data object Skillspace : HomeNavigationRoute("skillspace")
    data object Account : HomeNavigationRoute("account")
}

@Composable
fun HomeNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = HomeNavigationRoute.Dashboard.route, modifier = modifier) {
        composable(HomeNavigationRoute.Dashboard.route) {
            val viewModel = hiltViewModel<DashboardViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            DashboardScreen(uiState)
        }
        composable(HomeNavigationRoute.Learn.route) {
            val viewModel = hiltViewModel<LearnViewModel>()
            LearnScreen(viewModel)
        }
        composable(HomeNavigationRoute.Skillspace.route) {
            SkillspaceScreen()
        }
        composable(HomeNavigationRoute.Account.route) {
            AccountScreen()
        }
        showroomItems.filterIsInstance<ShowroomItem.Item>()
            .forEach { item ->
                composable(item.route) {
                    ShowroomContent(item.route, navController)
                }
            }
    }
}