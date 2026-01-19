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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.instructure.horizon.features.account.navigation.accountNavigation
import com.instructure.horizon.features.dashboard.DashboardScreen
import com.instructure.horizon.features.dashboard.DashboardViewModel
import com.instructure.horizon.features.dashboard.widget.course.list.DashboardCourseListScreen
import com.instructure.horizon.features.dashboard.widget.course.list.DashboardCourseListViewModel
import com.instructure.horizon.features.learn.navigation.learnNavigation
import com.instructure.horizon.features.skillspace.SkillspaceScreen
import com.instructure.horizon.features.skillspace.SkillspaceViewModel
import com.instructure.horizon.horizonui.animation.NavigationTransitionAnimation
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition
import com.instructure.horizon.horizonui.showroom.ShowroomContent
import com.instructure.horizon.horizonui.showroom.ShowroomItem
import com.instructure.horizon.horizonui.showroom.showroomItems
import com.instructure.horizon.navigation.MainNavigationRoute
import kotlinx.serialization.Serializable

@Serializable
sealed class HomeNavigationRoute(val route: String) {
    data object Dashboard : HomeNavigationRoute("dashboard")
    data object CourseList: HomeNavigationRoute("courses")
    data object Learn : HomeNavigationRoute("learn")
    data object Skillspace : HomeNavigationRoute("skillspace")
    data object Account : HomeNavigationRoute("account")
}

fun NavGraphBuilder.horizonHomeNavigation(
    navController: NavHostController,
) {
    navigation(
        route = MainNavigationRoute.Home.route,
        startDestination = HomeNavigationRoute.Dashboard.route,
        enterTransition = { enterTransition(NavigationTransitionAnimation.SCALE) },
        exitTransition = { exitTransition(NavigationTransitionAnimation.SCALE) },
        popEnterTransition = { popEnterTransition(NavigationTransitionAnimation.SCALE) },
        popExitTransition = { popExitTransition(NavigationTransitionAnimation.SCALE) },
    ) {
        learnNavigation(navController)
        composable(HomeNavigationRoute.Dashboard.route) {
            val viewModel = hiltViewModel<DashboardViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            DashboardScreen(uiState, navController)
        }
        composable(HomeNavigationRoute.Skillspace.route) {
            val viewModel = hiltViewModel<SkillspaceViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            SkillspaceScreen(uiState)
        }
        composable(HomeNavigationRoute.CourseList.route) {
            val viewModel = hiltViewModel<DashboardCourseListViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            DashboardCourseListScreen(uiState, navController)
        }
        accountNavigation(navController)

        showroomItems.filterIsInstance<ShowroomItem.Item>()
            .forEach { item ->
                composable(item.route) {
                    ShowroomContent(item.route, navController)
                }
            }
    }
}