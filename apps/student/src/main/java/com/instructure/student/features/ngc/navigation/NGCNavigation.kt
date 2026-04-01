/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.ngc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.instructure.student.features.ngc.dashboard.DashboardScreen
import com.instructure.student.features.ngc.splash.SplashScreen
import com.instructure.student.features.ngc.splash.SplashViewModel
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
            DashboardScreen(navController)
        }
    }
}