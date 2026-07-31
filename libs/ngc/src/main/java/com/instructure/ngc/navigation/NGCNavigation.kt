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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.instructure.instui.compose.InstUITheme
import com.instructure.ngc.features.assignmentdetails.NGCAssignmentDetailsScreen
import com.instructure.ngc.features.coursehome.CourseHomeScreen
import com.instructure.ngc.features.coursehome.CourseHomeViewModel
import com.instructure.ngc.features.coursehome.mywork.CourseMyWorkScreen
import com.instructure.ngc.features.dashboard.NGCDashboardScreen
import com.instructure.ngc.features.splash.SplashScreen
import com.instructure.ngc.features.splash.SplashViewModel
import com.instructure.ngc.features.submission.NGCSubmissionScreen
import com.instructure.ngc.features.submission.NGCSubmissionViewModel
import com.instructure.ngc.utils.rememberThemedCourseColor
import com.instructure.pandautils.features.grades.EXPERIENCE_KEY
import com.instructure.pandautils.features.grades.Experience
import com.instructure.pandautils.utils.Const
import kotlinx.serialization.Serializable

@Serializable
sealed class NGCNavigationRoute(val route: String) {
    data object Splash : NGCNavigationRoute("splash")
    data object Dashboard : NGCNavigationRoute("dashboard")
    data object CourseHome : NGCNavigationRoute("courses/{${CourseHomeViewModel.ARG_COURSE_ID}}") {
        fun createRoute(courseId: Long) = "courses/$courseId"
    }
    data object Grades : NGCNavigationRoute("courses/{${CourseHomeViewModel.ARG_COURSE_ID}}/grades") {
        fun createRoute(courseId: Long) = "courses/$courseId/grades"
    }
    data object Submission : NGCNavigationRoute(
        "courses/{${NGCSubmissionViewModel.ARG_COURSE_ID}}/assignments/{${NGCSubmissionViewModel.ARG_ASSIGNMENT_ID}}/submission" +
            "?${NGCSubmissionViewModel.ARG_SUBMISSION_TYPE}={${NGCSubmissionViewModel.ARG_SUBMISSION_TYPE}}"
    ) {
        fun createRoute(courseId: Long, assignmentId: Long, submissionType: String? = null): String {
            val base = "courses/$courseId/assignments/$assignmentId/submission"
            return if (submissionType != null) "$base?${NGCSubmissionViewModel.ARG_SUBMISSION_TYPE}=$submissionType" else base
        }
    }
    data object AssignmentDetails : NGCNavigationRoute(
        "courses/{${Const.COURSE_ID}}/assignments/{${Const.ASSIGNMENT_ID}}"
    ) {
        fun createRoute(courseId: Long, assignmentId: Long) = "courses/$courseId/assignments/$assignmentId"
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
                navArgument(CourseHomeViewModel.ARG_COURSE_ID) { type = NavType.LongType },
                navArgument(EXPERIENCE_KEY) {
                    type = NavType.StringType
                    defaultValue = Experience.NGC.name
                },
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://{domain}/courses/{${CourseHomeViewModel.ARG_COURSE_ID}}" },
                navDeepLink { uriPattern = "http://{domain}/courses/{${CourseHomeViewModel.ARG_COURSE_ID}}" },
                navDeepLink { uriPattern = "canvas-courses://{domain}/courses/{${CourseHomeViewModel.ARG_COURSE_ID}}" },
                navDeepLink { uriPattern = "canvas-student://{domain}/courses/{${CourseHomeViewModel.ARG_COURSE_ID}}" },
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong(CourseHomeViewModel.ARG_COURSE_ID) ?: 0L
            val courseColor = Color(rememberThemedCourseColor(courseId))

            InstUITheme(courseColor = courseColor) {
                CourseHomeScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onGradesClick = {
                        navController.navigate(NGCNavigationRoute.Grades.createRoute(courseId))
                    },
                )
            }
        }

        composable(
            route = NGCNavigationRoute.Grades.route,
            arguments = listOf(
                navArgument(CourseHomeViewModel.ARG_COURSE_ID) { type = NavType.LongType },
                navArgument(EXPERIENCE_KEY) {
                    type = NavType.StringType
                    defaultValue = Experience.NGC.name
                },
            ),
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong(CourseHomeViewModel.ARG_COURSE_ID) ?: 0L
            val courseColor = Color(rememberThemedCourseColor(courseId))

            InstUITheme(courseColor = courseColor) {
                CourseMyWorkScreen(
                    courseId = courseId,
                    onNavigateBack = { navController.popBackStack() },
                    onAssignmentClick = { cId, aId ->
                        navController.navigate(NGCNavigationRoute.AssignmentDetails.createRoute(cId, aId))
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composable(
            route = NGCNavigationRoute.AssignmentDetails.route,
            arguments = listOf(
                navArgument(Const.COURSE_ID) { type = NavType.LongType },
                navArgument(Const.ASSIGNMENT_ID) { type = NavType.LongType },
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://{domain}/courses/{${Const.COURSE_ID}}/assignments/{${Const.ASSIGNMENT_ID}}" },
                navDeepLink { uriPattern = "http://{domain}/courses/{${Const.COURSE_ID}}/assignments/{${Const.ASSIGNMENT_ID}}" },
                navDeepLink { uriPattern = "canvas-courses://{domain}/courses/{${Const.COURSE_ID}}/assignments/{${Const.ASSIGNMENT_ID}}" },
                navDeepLink { uriPattern = "canvas-student://{domain}/courses/{${Const.COURSE_ID}}/assignments/{${Const.ASSIGNMENT_ID}}" },
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong(Const.COURSE_ID) ?: 0L
            val assignmentId = backStackEntry.arguments?.getLong(Const.ASSIGNMENT_ID) ?: 0L
            val courseColor = Color(rememberThemedCourseColor(courseId))

            InstUITheme(courseColor = courseColor) {
                NGCAssignmentDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSubmission = { submissionType ->
                        navController.navigate(
                            NGCNavigationRoute.Submission.createRoute(courseId, assignmentId, submissionType.apiString)
                        )
                    },
                )
            }
        }

        composable(
            route = NGCNavigationRoute.Submission.route,
            arguments = listOf(
                navArgument(NGCSubmissionViewModel.ARG_COURSE_ID) { type = NavType.LongType },
                navArgument(NGCSubmissionViewModel.ARG_ASSIGNMENT_ID) { type = NavType.LongType },
                navArgument(NGCSubmissionViewModel.ARG_SUBMISSION_TYPE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong(NGCSubmissionViewModel.ARG_COURSE_ID) ?: 0L
            val courseColor = Color(rememberThemedCourseColor(courseId))

            InstUITheme(courseColor = courseColor) {
                NGCSubmissionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
