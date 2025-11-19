/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.home.HomeScreen
import com.instructure.horizon.features.home.HomeViewModel
import com.instructure.horizon.features.inbox.navigation.horizonInboxNavigation
import com.instructure.horizon.features.moduleitemsequence.ModuleItemSequenceScreen
import com.instructure.horizon.features.moduleitemsequence.ModuleItemSequenceViewModel
import com.instructure.horizon.features.notebook.navigation.notebookNavigation
import com.instructure.horizon.features.notification.NotificationScreen
import com.instructure.horizon.features.notification.NotificationViewModel
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition
import com.instructure.horizon.navigation.MainNavigationRoute.Companion.ASSIGNMENT_ID
import com.instructure.horizon.navigation.MainNavigationRoute.Companion.COURSE_ID
import com.instructure.horizon.navigation.MainNavigationRoute.Companion.PAGE_ID
import com.instructure.horizon.navigation.MainNavigationRoute.Companion.QUIZ_ID
import com.instructure.horizon.util.zeroScreenInsets
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
sealed class MainNavigationRoute(val route: String) {
    data object Home : MainNavigationRoute("home")
    data object Notification : MainNavigationRoute("notification")
    data object Notebook : MainNavigationRoute("notebook")
    data object Inbox : MainNavigationRoute("inbox")

    @Serializable
    data class ModuleItemSequence(
        val courseId: Long,
        val moduleItemId: Long? = null,
        val moduleItemAssetType: String? = null,
        val moduleItemAssetId: String? = null
    ) :
        MainNavigationRoute("module_item_sequence")

    data object AssignmentDetailsDeepLink : MainNavigationRoute("courses/{$COURSE_ID}assignments/{$ASSIGNMENT_ID}")
    data object QuizDetailsDeepLink : MainNavigationRoute("courses/{$COURSE_ID}quizzes/{$QUIZ_ID}")
    data object PageDetailsDeepLink : MainNavigationRoute("courses/{$COURSE_ID}pages/{$PAGE_ID}")

    companion object {
        const val COURSE_ID = "courseId"
        const val ASSIGNMENT_ID = "assignmentId"
        const val QUIZ_ID = "quizId"
        const val PAGE_ID = "pageId"
    }
}

@Composable
fun HorizonNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        contentWindowInsets = WindowInsets.zeroScreenInsets,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition },
            modifier = modifier.padding(innerPadding),
            navController = navController,
            startDestination = MainNavigationRoute.Home.route
        ) {
            notebookNavigation(navController) { snackbarMessage, onDismiss ->
                scope.launch {
                    if (snackbarMessage != null) {
                        val result = snackbarHostState.showSnackbar(snackbarMessage)
                        if (result == SnackbarResult.Dismissed) {
                            onDismiss()
                        }
                    }
                }
            }
            horizonInboxNavigation(navController)
            composable(MainNavigationRoute.Home.route) {
                HomeScreen(navController, hiltViewModel<HomeViewModel>())
            }
            composable<MainNavigationRoute.ModuleItemSequence> {
                val viewModel = hiltViewModel<ModuleItemSequenceViewModel>()
                val uiState by viewModel.uiState.collectAsState()
                ModuleItemSequenceScreen(navController, uiState)
            }
            composable(MainNavigationRoute.Notification.route) {
                val viewModel = hiltViewModel<NotificationViewModel>()
                val uiState by viewModel.uiState.collectAsState()
                NotificationScreen(uiState, navController)
            }

            // Assignment Details deep link
            composable(
                route = MainNavigationRoute.AssignmentDetailsDeepLink.route,
                arguments = listOf(
                    navArgument(COURSE_ID) {
                        type = NavType.StringType
                    },
                    navArgument(ASSIGNMENT_ID) {
                        type = NavType.StringType
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern =
                            "${ApiPrefs.fullDomain}/courses/{${COURSE_ID}}/assignments/{${ASSIGNMENT_ID}}"
                    }
                )
            ) { backStackEntry ->
                LaunchedEffect(Unit) {
                    val courseId =
                        backStackEntry.arguments?.getString(COURSE_ID)
                            ?.toLongOrNull()
                    val assignmentId =
                        backStackEntry.arguments?.getString(ASSIGNMENT_ID)
                            ?.toLongOrNull()
                    val moduleType = "Assignment"
                    navController.navigate(
                        MainNavigationRoute.ModuleItemSequence(
                            courseId = courseId ?: -1L,
                            moduleItemAssetType = moduleType,
                            moduleItemAssetId = assignmentId?.toString()
                        )
                    ) {
                        popUpTo(MainNavigationRoute.AssignmentDetailsDeepLink.route) {
                            inclusive = true
                        }
                    }
                }
            }

            // Quiz Details deep link
            composable(
                route = MainNavigationRoute.QuizDetailsDeepLink.route,
                arguments = listOf(
                    navArgument(COURSE_ID) {
                        type = NavType.StringType
                    },
                    navArgument(QUIZ_ID) {
                        type = NavType.StringType
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern =
                            "${ApiPrefs.fullDomain}/courses/{${COURSE_ID}}/quizzes/{${QUIZ_ID}}"
                    }
                )
            ) { backStackEntry ->
                LaunchedEffect(Unit) {
                    val courseId =
                        backStackEntry.arguments?.getString(COURSE_ID)
                            ?.toLongOrNull()
                    val quizId =
                        backStackEntry.arguments?.getString(QUIZ_ID)
                            ?.toLongOrNull()
                    val moduleType = "Quiz"
                    navController.navigate(
                        MainNavigationRoute.ModuleItemSequence(
                            courseId = courseId ?: -1L,
                            moduleItemAssetType = moduleType,
                            moduleItemAssetId = quizId?.toString()
                        )
                    ) {
                        popUpTo(MainNavigationRoute.QuizDetailsDeepLink.route) {
                            inclusive = true
                        }
                    }
                }
            }

            // Page Details deep link
            composable(
                route = MainNavigationRoute.PageDetailsDeepLink.route,
                arguments = listOf(
                    navArgument(COURSE_ID) {
                        type = NavType.StringType
                    },
                    navArgument(PAGE_ID) {
                        type = NavType.StringType
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern =
                            "${ApiPrefs.fullDomain}/courses/{${COURSE_ID}}/pages/{${PAGE_ID}}"
                    }
                )
            ) { backStackEntry ->
                LaunchedEffect(Unit) {
                    val courseId =
                        backStackEntry.arguments?.getString(COURSE_ID)
                            ?.toLongOrNull()
                    val pageId = backStackEntry.arguments?.getString(PAGE_ID)
                    val moduleType = "Page"
                    navController.navigate(
                        MainNavigationRoute.ModuleItemSequence(
                            courseId = courseId ?: -1L,
                            moduleItemAssetType = moduleType,
                            moduleItemAssetId = pageId
                        )
                    ) {
                        popUpTo(MainNavigationRoute.PageDetailsDeepLink.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}
