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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.instructure.horizon.features.aiassistant.AiAssistantScreen
import com.instructure.horizon.features.home.HomeScreen
import com.instructure.horizon.features.home.HomeViewModel
import com.instructure.horizon.features.moduleitemsequence.ModuleItemSequenceScreen
import com.instructure.horizon.features.moduleitemsequence.ModuleItemSequenceViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed class MainNavigationRoute(val route: String) {
    data object Home : MainNavigationRoute("home")
    data object AiAssistant : MainNavigationRoute("ai")

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
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = MainNavigationRoute.Home.route
    ) {
        composable(MainNavigationRoute.Home.route) {
            HomeScreen(navController, hiltViewModel<HomeViewModel>())
        }
        composable(MainNavigationRoute.AiAssistant.route) {
            AiAssistantScreen(navController)
        }
        composable<MainNavigationRoute.ModuleItemSequence> {
            val viewModel = hiltViewModel<ModuleItemSequenceViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            ModuleItemSequenceScreen(navController, uiState)
        }
    }
}