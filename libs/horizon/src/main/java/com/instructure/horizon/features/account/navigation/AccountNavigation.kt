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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.features.account.AccountScreen
import com.instructure.horizon.features.account.AccountViewModel
import com.instructure.horizon.features.account.advanced.AccountAdvancedScreen
import com.instructure.horizon.features.account.calendarfeed.AccountCalendarFeedScreen
import com.instructure.horizon.features.account.notifications.AccountNotificationsScreen
import com.instructure.horizon.features.account.notifications.AccountNotificationsViewModel
import com.instructure.horizon.features.account.password.AccountPasswordScreen
import com.instructure.horizon.features.account.profile.AccountProfileScreen
import com.instructure.horizon.features.account.profile.AccountProfileViewModel

@Composable
fun AccountNavigation(
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController,
        startDestination = AccountRoute.Account.route,
        modifier = modifier
    ) {
        composable(AccountRoute.Account.route) {
            val viewModel = hiltViewModel<AccountViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AccountScreen(uiState, navController, mainNavController)
        }

        composable(AccountRoute.Profile.route) {
            val viewModel = hiltViewModel<AccountProfileViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AccountProfileScreen(uiState, navController, mainNavController)
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
            AccountCalendarFeedScreen()
        }

        composable(AccountRoute.Advanced.route) {
            AccountAdvancedScreen()
        }
    }
}