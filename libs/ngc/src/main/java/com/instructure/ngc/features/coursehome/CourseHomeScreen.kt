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

package com.instructure.ngc.features.coursehome

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.navigation.CollapsingTopBar
import com.instructure.instui.compose.navigation.SegmentedControl
import com.instructure.ngc.R
import com.instructure.ngc.designsystem.Image
import com.instructure.ngc.features.coursehome.modules.CourseModulesScreen
import com.instructure.ngc.features.coursehome.mywork.CourseMyWorkScreen
import com.instructure.ngc.features.coursehome.navigation.CourseNavigationScreen
import com.instructure.ngc.features.coursehome.overview.CourseOverviewScreen
import com.instructure.pandautils.utils.ViewStyler

@Composable
fun CourseHomeScreen(
    onNavigateBack: () -> Unit,
    viewModel: CourseHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CourseHomeScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTabSelected = viewModel::onTabSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseHomeScreenContent(
    uiState: CourseHomeUiState,
    onNavigateBack: () -> Unit,
    onTabSelected: (CourseHomeTab) -> Unit,
) {
    val courseColor = LocalCourseColor.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    val context = LocalContext.current
    val isCollapsed = collapsedFraction >= 0.5f
    val courseColorArgb = courseColor.toArgb()
    SideEffect {
        val activity = context as? Activity ?: return@SideEffect
        if (isCollapsed) {
            ViewStyler.setStatusBarDark(activity, courseColorArgb)
        } else {
            ViewStyler.setStatusBarLight(activity)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsingTopBar(
                title = uiState.courseName,
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
                leading = {
                    Image(
                        imageUrl = uiState.courseImageUrl,
                        backgroundColor = courseColor,
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val tabLabels = listOf(
                stringResource(R.string.ngc_courseHomeTabHome),
                stringResource(R.string.ngc_courseHomeTabModules),
                stringResource(R.string.ngc_courseHomeTabMyWork),
                stringResource(R.string.ngc_courseHomeTabMore),
            )
            SegmentedControl(
                tabs = tabLabels,
                selectedIndex = uiState.selectedTab.ordinal,
                onTabSelected = { index -> onTabSelected(CourseHomeTab.entries[index]) },
                modifier = Modifier.fillMaxWidth(),
            )

            when (uiState.selectedTab) {
                CourseHomeTab.HOME -> CourseOverviewScreen(modifier = Modifier.fillMaxSize())
                CourseHomeTab.MODULES -> CourseModulesScreen(modifier = Modifier.fillMaxSize())
                CourseHomeTab.MY_WORK -> CourseMyWorkScreen(modifier = Modifier.fillMaxSize())
                CourseHomeTab.MORE -> CourseNavigationScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview
@Composable
private fun CourseHomeScreenPreview() {
    InstUITheme(courseColor = Color(0xFFBF5811)) {
        CourseHomeScreenContent(
            uiState = CourseHomeUiState(
                courseName = "Introduction to Space Stations with an aggressively long name to test collapsing behavior.",
                selectedTab = CourseHomeTab.HOME,
                isLoading = false,
            ),
            onNavigateBack = {},
            onTabSelected = {},
        )
    }
}