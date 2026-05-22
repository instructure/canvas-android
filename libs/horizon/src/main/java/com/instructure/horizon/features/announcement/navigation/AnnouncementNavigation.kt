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
 */
package com.instructure.horizon.features.announcement.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.instructure.horizon.features.announcement.details.AnnouncementDetailsScreen
import com.instructure.horizon.features.announcement.details.AnnouncementDetailsViewModel
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition

fun NavGraphBuilder.announcementNavigation(navController: NavHostController) {
    composable(
        route = AnnouncementRoute.Details.route,
        arguments = listOf(
            navArgument(AnnouncementRoute.KIND) {
                type = NavType.StringType
            },
            navArgument(AnnouncementRoute.ID) {
                type = NavType.LongType
            },
            navArgument(AnnouncementRoute.COURSE_ID) {
                type = NavType.LongType
                defaultValue = -1L
            },
        ),
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() },
    ) {
        val viewModel: AnnouncementDetailsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()
        AnnouncementDetailsScreen(uiState, navController)
    }
}
