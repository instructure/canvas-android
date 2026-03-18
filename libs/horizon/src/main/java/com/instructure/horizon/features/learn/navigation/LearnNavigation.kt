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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.features.learn.LearnScreen
import com.instructure.horizon.features.learn.LearnViewModel
import com.instructure.horizon.features.learn.course.details.CourseDetailsScreen
import com.instructure.horizon.features.learn.course.details.CourseDetailsViewModel
import com.instructure.horizon.features.learn.filter.LearnLearningLibraryFilterScreen
import com.instructure.horizon.features.learn.filter.LearnLearningLibraryFilterViewModel
import com.instructure.horizon.features.learn.learninglibrary.details.LearnLearningLibraryDetailsScreen
import com.instructure.horizon.features.learn.learninglibrary.details.LearnLearningLibraryDetailsViewModel
import com.instructure.horizon.features.learn.learninglibrary.enroll.LearnLearningLibraryEnrollScreen
import com.instructure.horizon.features.learn.learninglibrary.enroll.LearnLearningLibraryEnrollViewModel
import com.instructure.horizon.features.learn.learninglibrary.filter.LearnLearningLibraryFilterScreen
import com.instructure.horizon.features.learn.learninglibrary.filter.LearnLearningLibraryFilterViewModel
import com.instructure.horizon.features.learn.program.details.ProgramDetailsScreen
import com.instructure.horizon.features.learn.program.details.ProgramDetailsViewModel

fun NavGraphBuilder.learnNavigation(
    navController: NavHostController,
) {
    navigation(
        route = HomeNavigationRoute.Learn.route,
        startDestination = LearnRoute.LearnScreen.route
    ){
        composable(
            LearnRoute.LearnScreen.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${ApiPrefs.fullDomain}/{${LearnRoute.LearnScreen.route}}"
                }
            )
        ) {
            val viewModel = hiltViewModel<LearnViewModel>()
            val uiState by viewModel.state.collectAsState()

            LearnScreen(uiState, navController)
        }
        composable(
            route = LearnRoute.LearnCourseDetailsScreen.route,
            arguments = listOf(
                navArgument(LearnRoute.LearnCourseDetailsScreen.courseIdAttr) {
                    type = NavType.LongType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "${ApiPrefs.fullDomain}/${LearnRoute.LearnCourseDetailsScreen.route}"
                }
            )
        ) {
            val viewModel = hiltViewModel<CourseDetailsViewModel>()
            val state by viewModel.state.collectAsState()
            CourseDetailsScreen(state, navController)
        }
        composable(
            route = LearnRoute.LearnProgramDetailsScreen.route,
            arguments = listOf(
                navArgument(LearnRoute.LearnProgramDetailsScreen.programIdAttr) {
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "${ApiPrefs.fullDomain}/${LearnRoute.LearnProgramDetailsScreen.route}"
                }
            )
        ) {
            val viewModel = hiltViewModel<ProgramDetailsViewModel>()
            val state by viewModel.state.collectAsState()
            ProgramDetailsScreen(state, navController)
        }
    }
    composable(
        route = LearnRoute.LearnLearningLibraryDetailsScreen.route,
        arguments = listOf(
            navArgument(LearnRoute.LearnLearningLibraryDetailsScreen.collectionIdAttr) {
                type = NavType.StringType
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "${ApiPrefs.fullDomain}/${LearnRoute.LearnLearningLibraryDetailsScreen.route}"
            }
        )
    ) {
        val viewModel = hiltViewModel<LearnLearningLibraryDetailsViewModel>()
        val state by viewModel.uiState.collectAsState()
        LearnLearningLibraryDetailsScreen(state, navController)
    }
    composable(
        route = LearnRoute.LearnLearningLibraryEnrollScreen.route,
        arguments = listOf(
            navArgument(LearnRoute.LearnLearningLibraryEnrollScreen.learningLibraryIdAttr) {
                type = NavType.StringType
            }
        ),
    ) {
        val viewModel = hiltViewModel<LearnLearningLibraryEnrollViewModel>()
        val state by viewModel.state.collectAsState()
        LearnLearningLibraryEnrollScreen(state, navController)
    }
    composable(
        route = LearnRoute.LearnLearningLibraryFilterScreen.route,
        arguments = listOf(
            navArgument(LearnRoute.LearnLearningLibraryFilterScreen.screenTypeAttr) {
                type = NavType.StringType
            },
            navArgument(LearnRoute.LearnLearningLibraryFilterScreen.typeFilterAttr) {
                type = NavType.StringType
            },
            navArgument(LearnRoute.LearnLearningLibraryFilterScreen.sortOptionAttr) {
                type = NavType.StringType
            }
        )
    ) {
        val viewModel = hiltViewModel<LearnLearningLibraryFilterViewModel>()
        val state by viewModel.uiState.collectAsState()
        LearnLearningLibraryFilterScreen(state, navController)
    }
}