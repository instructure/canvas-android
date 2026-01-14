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
package com.instructure.horizon.features.learn.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.instructure.horizon.features.learn.course.details.CourseDetailsScreen
import com.instructure.horizon.features.learn.course.details.CourseDetailsViewModel
import com.instructure.horizon.features.learn.program.details.ProgramDetailsScreen
import com.instructure.horizon.features.learn.program.details.ProgramDetailsViewModel

fun NavGraphBuilder.learnNavigation(
    homeNavController: NavHostController,
    mainNavController: NavHostController,
) {
    composable(
        route = LearnRoute.LearnCourseDetailsScreen.route
    ) {
        val viewModel = hiltViewModel<CourseDetailsViewModel>()
        val state by viewModel.state.collectAsState()
        CourseDetailsScreen(state, mainNavController)
    }
    composable(
        route = LearnRoute.LearnProgramDetailsScreen.route
    ) {
        val viewModel = hiltViewModel<ProgramDetailsViewModel>()
        val state by viewModel.state.collectAsState()
        ProgramDetailsScreen(state, mainNavController)

    }
}