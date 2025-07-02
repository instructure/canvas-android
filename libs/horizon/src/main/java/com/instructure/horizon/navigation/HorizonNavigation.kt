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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.instructure.horizon.features.home.HomeScreen
import com.instructure.horizon.features.home.HomeViewModel
import com.instructure.horizon.features.moduleitemsequence.ModuleItemSequenceScreen
import com.instructure.horizon.features.moduleitemsequence.ModuleItemSequenceViewModel
import com.instructure.horizon.features.notebook.NotebookScreen
import com.instructure.horizon.features.notebook.NotebookViewModel
import com.instructure.horizon.features.notebook.addedit.AddEditNoteScreen
import com.instructure.horizon.features.notebook.addedit.add.AddNoteViewModel
import com.instructure.horizon.features.notebook.addedit.edit.EditNoteViewModel
import com.instructure.horizon.features.notification.NotificationScreen
import com.instructure.horizon.features.notification.NotificationViewModel
import com.instructure.horizon.horizonui.animation.enterTransition
import com.instructure.horizon.horizonui.animation.exitTransition
import com.instructure.horizon.horizonui.animation.popEnterTransition
import com.instructure.horizon.horizonui.animation.popExitTransition
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
sealed class MainNavigationRoute(val route: String) {
    data object Home : MainNavigationRoute("home")
    data object Notification : MainNavigationRoute("notification")
    data object Notebook : MainNavigationRoute("notebook")

    @Serializable
    data class AddNotebook(
        val courseId: String,
        val objectType: String,
        val objectId: String,
        val highlightedTextStartOffset: Int,
        val highlightedTextEndOffset: Int,
        val highlightedTextStartContainer: String,
        val highlightedTextEndContainer: String,
        val textSelectionStart: Int,
        val textSelectionEnd: Int,
        val highlightedText: String,
    ): MainNavigationRoute("add_notebook")

    @Serializable
    data class EditNotebook(
        val noteId: String,
        val highlightedTextStartOffset: Int,
        val highlightedTextEndOffset: Int,
        val highlightedTextStartContainer: String,
        val highlightedTextEndContainer: String,
        val textSelectionStart: Int,
        val textSelectionEnd: Int,
        val highlightedText: String,
        val noteType: String,
        val userComment: String,
    ): MainNavigationRoute("edit_notebook")

    @Serializable
    data class ModuleItemSequence(
        val courseId: Long,
        val moduleItemId: Long? = null,
        val moduleItemAssetType: String? = null,
        val moduleItemAssetId: String? = null
    ) :
        MainNavigationRoute("module_item_sequence")
}

@Composable
fun HorizonNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
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
            composable(MainNavigationRoute.Notebook.route) {
                val viewModel = hiltViewModel<NotebookViewModel>()
                val uiState by viewModel.uiState.collectAsState()
                NotebookScreen(navController, uiState)
            }
            composable<MainNavigationRoute.AddNotebook> {
                val viewModel = hiltViewModel<AddNoteViewModel>()
                val uiState by viewModel.uiState.collectAsState()
                AddEditNoteScreen(navController, uiState) { snackbarMessage, onDismiss ->
                    scope.launch {
                        if (snackbarMessage != null) {
                            val result = snackbarHostState.showSnackbar(snackbarMessage)
                            if (result == SnackbarResult.Dismissed) {
                                onDismiss()
                            }
                        }
                    }
                }
            }
            composable<MainNavigationRoute.EditNotebook> {
                val viewModel = hiltViewModel<EditNoteViewModel>()
                val uiState by viewModel.uiState.collectAsState()
                AddEditNoteScreen(navController, uiState) { snackbarMessage, onDismiss ->
                    scope.launch {
                        if (snackbarMessage != null) {
                            val result = snackbarHostState.showSnackbar(snackbarMessage)
                            if (result == SnackbarResult.Dismissed) {
                                onDismiss()
                            }
                        }
                    }
                }
            }
        }
    }
}