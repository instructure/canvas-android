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
package com.instructure.horizon.features.learn.program.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.features.learn.program.list.LearnProgramListScreen
import com.instructure.horizon.horizonui.animation.NavigationTransitionAnimation
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition

@Composable
fun LearnProgramNavigation(mainNavController: NavHostController) {
    val learnCourseNavController = rememberNavController()

    NavHost(
        learnCourseNavController,
        enterTransition = { enterTransition(NavigationTransitionAnimation.SLIDE) },
        exitTransition = { exitTransition(NavigationTransitionAnimation.SLIDE) },
        popEnterTransition = { popEnterTransition(NavigationTransitionAnimation.SLIDE) },
        popExitTransition = { popExitTransition(NavigationTransitionAnimation.SLIDE) },
        startDestination = HomeNavigationRoute.Dashboard.route,
    ) {
        composable(
            route = LearnProgramRoute.LearnProgramListRoute.route
        ) {
            LearnProgramListScreen()
        }
        composable(
            route = LearnProgramRoute.LearnProgramDetailsRoute.route,
        ) {
            TODO()
        }
    }
}