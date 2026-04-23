/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.ngc.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.instructure.canvasapi2.models.Course
import com.instructure.instui.compose.InstUITheme
import com.instructure.ngc.coursehome.CourseHomeScreen
import com.instructure.ngc.coursehome.CourseHomeViewModel
import com.instructure.ngc.dashboard.NGCDashboardScreen
import com.instructure.ngc.splash.SplashScreen
import com.instructure.ngc.splash.SplashViewModel
import com.instructure.pandautils.utils.ColorKeeper
import kotlinx.serialization.Serializable

@Serializable
sealed class NGCNavigationRoute(val route: String) {
    data object Splash : NGCNavigationRoute("splash")
    data object Dashboard : NGCNavigationRoute("dashboard")
    data object CourseHome : NGCNavigationRoute("courseHome/{${CourseHomeViewModel.ARG_COURSE_ID}}") {
        fun createRoute(courseId: Long) = "courseHome/$courseId"
    }
}

@Composable
fun NGCNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NGCNavigationRoute.Splash.route
    ) {
        composable(NGCNavigationRoute.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            SplashScreen(
                uiState = uiState,
                onThemeApplied = viewModel::onThemeApplied,
                onInitialDataLoaded = {
                    navController.navigate(NGCNavigationRoute.Dashboard.route) {
                        popUpTo(NGCNavigationRoute.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(NGCNavigationRoute.Dashboard.route) {
            NGCDashboardScreen(navController)
        }

        composable(
            route = NGCNavigationRoute.CourseHome.route,
            arguments = listOf(
                navArgument(CourseHomeViewModel.ARG_COURSE_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong(CourseHomeViewModel.ARG_COURSE_ID) ?: 0L
            val isDark = isSystemInDarkTheme()
            val themedColor = remember(courseId) {
                ColorKeeper.getOrGenerateColor(Course(id = courseId))
            }
            val courseColor = Color(if (isDark) themedColor.dark else themedColor.light)

            InstUITheme(courseColor = courseColor) {
                CourseHomeScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
