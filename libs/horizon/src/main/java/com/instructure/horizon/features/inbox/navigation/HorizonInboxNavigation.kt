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
package com.instructure.horizon.features.inbox.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachmentPickerViewModel
import com.instructure.horizon.features.inbox.compose.HorizonInboxComposeScreen
import com.instructure.horizon.features.inbox.compose.HorizonInboxComposeViewModel
import com.instructure.horizon.features.inbox.details.HorizonInboxDetailsScreen
import com.instructure.horizon.features.inbox.details.HorizonInboxDetailsViewModel
import com.instructure.horizon.features.inbox.list.HorizonInboxListScreen
import com.instructure.horizon.features.inbox.list.HorizonInboxListViewModel

@Composable
fun HorizonInboxNavigation(
    mainNavController: NavHostController,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HorizonInboxRoute.InboxList.route,
        modifier = modifier
    ) {
        composable(HorizonInboxRoute.InboxList.route) {
            val viewModel = hiltViewModel<HorizonInboxListViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            HorizonInboxListScreen(uiState, mainNavController, navController)
        }
        composable(
            HorizonInboxRoute.InboxDetails.route,
            arguments = listOf(
                navArgument(HorizonInboxRoute.InboxDetails.TYPE) {
                    type = androidx.navigation.NavType.StringType
                },
                navArgument(HorizonInboxRoute.InboxDetails.ID) {
                    type = androidx.navigation.NavType.LongType
                },
                navArgument(HorizonInboxRoute.InboxDetails.COURSE_ID) {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: HorizonInboxDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            HorizonInboxDetailsScreen(uiState, navController)
        }
        composable(HorizonInboxRoute.InboxCompose.route) {
            val viewModel: HorizonInboxComposeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            val pickerViewModel: HorizonInboxAttachmentPickerViewModel = hiltViewModel()
            val pickerState by pickerViewModel.uiState.collectAsState()

            HorizonInboxComposeScreen(uiState, pickerState, navController)
        }
    }
}