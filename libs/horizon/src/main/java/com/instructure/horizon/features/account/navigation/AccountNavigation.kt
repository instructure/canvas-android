/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.instructure.horizon.features.account.AccountScreen
import com.instructure.horizon.features.account.AccountViewModel
import com.instructure.horizon.features.account.advanced.AccountAdvancedScreen
import com.instructure.horizon.features.account.advanced.AccountAdvancedViewModel
import com.instructure.horizon.features.account.calendarfeed.AccountCalendarFeedScreen
import com.instructure.horizon.features.account.calendarfeed.AccountCalendarFeedViewModel
import com.instructure.horizon.features.account.notifications.AccountNotificationsScreen
import com.instructure.horizon.features.account.notifications.AccountNotificationsViewModel
import com.instructure.horizon.features.account.password.AccountPasswordScreen
import com.instructure.horizon.features.account.profile.AccountProfileScreen
import com.instructure.horizon.features.account.profile.AccountProfileViewModel
import com.instructure.horizon.features.account.reportabug.ReportABugWebView
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.animation.NavigationTransitionAnimation
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition

fun NavGraphBuilder.accountNavigation(
    navController: NavHostController,
) {
    navigation(
        route = HomeNavigationRoute.Account.route,
        startDestination = AccountRoute.Account.route,
        enterTransition = { enterTransition(NavigationTransitionAnimation.SCALE) },
        exitTransition = { exitTransition(NavigationTransitionAnimation.SCALE) },
        popEnterTransition = { popEnterTransition(NavigationTransitionAnimation.SCALE) },
        popExitTransition = { popExitTransition(NavigationTransitionAnimation.SCALE) },
    ) {
        composable(
            route = AccountRoute.Account.route,
        ) {
            val viewModel = hiltViewModel<AccountViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AccountScreen(uiState, navController)
        }

        composable(AccountRoute.Profile.route) {
            val viewModel = hiltViewModel<AccountProfileViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AccountProfileScreen(uiState, navController)
        }

        composable(AccountRoute.Password.route) {
            AccountPasswordScreen()
        }

        composable(AccountRoute.Notifications.route) {
            val viewModel = hiltViewModel<AccountNotificationsViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AccountNotificationsScreen(uiState, navController)
        }

        composable(AccountRoute.CalendarFeed.route) {
            val viewModel = hiltViewModel<AccountCalendarFeedViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AccountCalendarFeedScreen(uiState, navController)
        }

        composable(AccountRoute.Advanced.route) {
            val viewModel = hiltViewModel<AccountAdvancedViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AccountAdvancedScreen(uiState, navController)
        }

        composable(AccountRoute.BugReportWebView.route) {
            ReportABugWebView(navController)
        }
    }
}