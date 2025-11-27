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
package com.instructure.horizon.features.notebook.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.instructure.horizon.features.notebook.NotebookScreen
import com.instructure.horizon.features.notebook.NotebookViewModel
import com.instructure.horizon.features.notebook.addedit.AddEditNoteScreen
import com.instructure.horizon.features.notebook.addedit.add.AddNoteViewModel
import com.instructure.horizon.features.notebook.addedit.edit.EditNoteViewModel
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.mainEnterTransition
import com.instructure.horizon.horizonui.animation.mainExitTransition
import com.instructure.horizon.horizonui.animation.overlayEnterTransition
import com.instructure.horizon.horizonui.animation.overlayExitTransition
import com.instructure.horizon.horizonui.animation.overlayPopEnterTransition
import com.instructure.horizon.horizonui.animation.overlayPopExitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.orDefault

fun NavGraphBuilder.notebookNavigation(
    navController: NavHostController,
    onShowSnackbar: (String?, () -> Unit) -> Unit,
) {
    navigation(
        route = MainNavigationRoute.Notebook.route,
        startDestination = NotebookRoute.Notebook.route,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition },
    ) {
        composable(
            route = NotebookRoute.Notebook.route,
            enterTransition = { overlayEnterTransition(::isOverlayEnterTransition) },
            exitTransition = { overlayExitTransition(::isOverlayExitTransition) },
            popEnterTransition = { overlayPopEnterTransition(::isOverlayPopEnterTransition) },
            popExitTransition = { overlayPopExitTransition(::isOverlayPopExitTransition) },
            arguments = listOf(
                navArgument(NotebookRoute.Notebook.COURSE_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(NotebookRoute.Notebook.OBJECT_TYPE) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(NotebookRoute.Notebook.OBJECT_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(NotebookRoute.Notebook.SHOW_TOP_BAR) {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument(NotebookRoute.Notebook.SHOW_FILTERS) {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument(NotebookRoute.Notebook.NAVIGATE_TO_EDIT) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
        ) {
            val viewModel = hiltViewModel<NotebookViewModel>()
            NotebookScreen(navController, viewModel)
        }
        composable<NotebookRoute.AddNotebook>(
            enterTransition = { mainEnterTransition },
            exitTransition = { mainExitTransition },
            popEnterTransition = { mainEnterTransition },
            popExitTransition = { mainExitTransition },
        ) {
            val viewModel = hiltViewModel<AddNoteViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AddEditNoteScreen(navController, uiState, onShowSnackbar)
        }
        composable<NotebookRoute.EditNotebook>(
            enterTransition = { mainEnterTransition },
            exitTransition = { mainExitTransition },
            popEnterTransition = { mainEnterTransition },
            popExitTransition = { mainExitTransition },
        ) {
            val viewModel = hiltViewModel<EditNoteViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AddEditNoteScreen(navController, uiState, onShowSnackbar)
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isOverlayEnterTransition(): Boolean {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    val targetIsNotebook = targetRoute?.startsWith(NotebookRoute.Notebook.route).orDefault()
    val isFromEditNotebook = initialRoute?.startsWith(NotebookRoute.EditNotebook::class.java.name.replace("$", ".")).orDefault()
    val isFromModuleItemSequence = initialRoute?.startsWith(MainNavigationRoute.ModuleItemSequence::class.java.name.replace("$", ".")).orDefault()

    return targetIsNotebook && (isFromEditNotebook || isFromModuleItemSequence)
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isOverlayExitTransition(): Boolean {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    val isFromNotebook = initialRoute?.startsWith(NotebookRoute.Notebook.route).orDefault()
    val targetIsEditNotebook = targetRoute?.startsWith(NotebookRoute.EditNotebook::class.java.name.replace("$", ".")).orDefault()

    return isFromNotebook && targetIsEditNotebook
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isOverlayPopEnterTransition(): Boolean {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    val targetIsNotebook = targetRoute?.startsWith(NotebookRoute.Notebook.route).orDefault()
    val isFromEditNotebook = initialRoute?.startsWith(NotebookRoute.EditNotebook::class.java.name.replace("$", ".")).orDefault()
    val isFromModuleItemSequence = initialRoute?.startsWith(MainNavigationRoute.ModuleItemSequence::class.java.name.replace("$", ".")).orDefault()

    return targetIsNotebook && isFromEditNotebook && !isFromModuleItemSequence
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isOverlayPopExitTransition(): Boolean {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    val isFromNotebook = initialRoute?.startsWith(NotebookRoute.Notebook.route).orDefault()
    val targetIsModuleItemSequence = targetRoute?.startsWith(MainNavigationRoute.ModuleItemSequence::class.java.name.replace("$", ".")).orDefault()
    val targetIsEditNotebook = targetRoute?.startsWith(NotebookRoute.EditNotebook::class.java.name.replace("$", ".")).orDefault()

    return isFromNotebook && (targetIsModuleItemSequence || targetIsEditNotebook)
}