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
@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.horizon.features.learn

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.learn.course.list.LearnCourseListScreen
import com.instructure.horizon.features.learn.program.list.LearnProgramListScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.organisms.CollapsableScaffold
import com.instructure.horizon.horizonui.organisms.tabrow.TabRow

@Composable
fun LearnScreen(state: LearnUiState, mainNavController: NavHostController) {

    CollapsableScaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = {
            TabRow(
                tabs = state.tabs,
                selectedIndex = state.tabs.indexOf(state.selectedTab),
                onTabSelected = { state.updateSelectedTab(it) },
                tabAlignment = Alignment.Start,
                tabItemToLabel = { stringResource(it.labelRes) }
            )
        }
    ) {
        when(state.selectedTab) {
            LearnTab.COURSES -> {
                LearnCourseListScreen()
            }
            LearnTab.PROGRAMS -> {
                LearnProgramListScreen()
            }
        }
    }
}

@Composable
@Preview
private fun LearnScreenContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState()
    LearnScreen(state, rememberNavController())
}