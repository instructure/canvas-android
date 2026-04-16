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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.instructure.ngc.dashboard.NGCDashboardScreen
import com.instructure.ngc.splash.SplashScreen
import com.instructure.ngc.splash.SplashViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed class NGCNavigationRoute(val route: String) {
    data object Splash : NGCNavigationRoute("splash")
    data object Dashboard : NGCNavigationRoute("dashboard")
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
    }
}
