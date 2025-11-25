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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition
import com.instructure.horizon.navigation.MainNavigationRoute

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
        composable<NotebookRoute.AddNotebook> {
            val viewModel = hiltViewModel<AddNoteViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AddEditNoteScreen(navController, uiState, onShowSnackbar)
        }
        composable<NotebookRoute.EditNotebook> {
            val viewModel = hiltViewModel<EditNoteViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AddEditNoteScreen(navController, uiState, onShowSnackbar)
        }
    }
}