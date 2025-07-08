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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.instructure.horizon.features.account.navigation.AccountNavigation
import com.instructure.horizon.features.dashboard.DashboardScreen
import com.instructure.horizon.features.dashboard.DashboardViewModel
import com.instructure.horizon.features.learn.LearnScreen
import com.instructure.horizon.features.learn.LearnViewModel
import com.instructure.horizon.features.skillspace.SkillspaceScreen
import com.instructure.horizon.features.skillspace.SkillspaceViewModel
import com.instructure.horizon.horizonui.animation.mainEnterTransition
import com.instructure.horizon.horizonui.animation.mainExitTransition
import com.instructure.horizon.horizonui.showroom.ShowroomContent
import com.instructure.horizon.horizonui.showroom.ShowroomItem
import com.instructure.horizon.horizonui.showroom.showroomItems
import kotlinx.serialization.Serializable

@Serializable
sealed class HomeNavigationRoute(val route: String) {
    data object Dashboard : HomeNavigationRoute("dashboard")
    data object Learn : HomeNavigationRoute("learn?courseId={courseId}") {
        fun withArgs(courseId: Long? = null) =
            if (courseId != null) "learn?courseId=$courseId" else "learn"
    }

    data object Skillspace : HomeNavigationRoute("skillspace")
    data object Account : HomeNavigationRoute("account")
}

@Composable
fun HomeNavigation(navController: NavHostController, mainNavController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController,
        enterTransition = { mainEnterTransition },
        exitTransition = { mainExitTransition },
        popEnterTransition = { mainEnterTransition },
        popExitTransition = { mainExitTransition },
        startDestination = HomeNavigationRoute.Dashboard.route, modifier = modifier
    ) {
        composable(HomeNavigationRoute.Dashboard.route) {
            val viewModel = hiltViewModel<DashboardViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            DashboardScreen(uiState, mainNavController, homeNavController = navController)
        }
        composable(
            route = HomeNavigationRoute.Learn.route, arguments = listOf(
                navArgument("courseId") {
                    type = NavType.LongType
                    defaultValue = -1
                }
            )) {
            val viewModel = hiltViewModel<LearnViewModel>()
            val uiState by viewModel.state.collectAsState()
            LearnScreen(uiState, mainNavController)
        }
        composable(HomeNavigationRoute.Skillspace.route) {
            val viewModel = hiltViewModel<SkillspaceViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            SkillspaceScreen(uiState, navController)
        }
        composable(HomeNavigationRoute.Account.route) {
            AccountNavigation(mainNavController)
        }
        showroomItems.filterIsInstance<ShowroomItem.Item>()
            .forEach { item ->
                composable(item.route) {
                    ShowroomContent(item.route, navController)
                }
            }
    }
}