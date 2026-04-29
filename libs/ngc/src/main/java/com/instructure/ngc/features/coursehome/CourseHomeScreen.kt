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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import com.instructure.ngc.features.coursehome.modules.CourseModulesScreen
import com.instructure.ngc.features.coursehome.mywork.CourseMyWorkScreen
import com.instructure.ngc.features.coursehome.navigation.CourseNavigationScreen
import com.instructure.ngc.features.coursehome.overview.CourseOverviewScreen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.navigation.CollapsingTopBar
import com.instructure.instui.compose.navigation.SegmentedControl
import com.instructure.instui.token.semantic.InstUILayoutSizes

private val TAB_LABELS = listOf("Home", "Modules", "My Work", "More")

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun CourseHomeScreenContent(
    uiState: CourseHomeUiState,
    onNavigateBack: () -> Unit,
    onTabSelected: (CourseHomeTab) -> Unit,
) {
    val courseColor = LocalCourseColor.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    val view = LocalView.current
    val lightStatusBar = collapsedFraction < 0.5f
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = lightStatusBar
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CollapsingTopBar(
                title = uiState.courseName,
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
                leading = {
                    CourseImage(
                        imageUrl = uiState.courseImageUrl,
                        courseColor = courseColor,
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
            SegmentedControl(
                tabs = TAB_LABELS,
                selectedIndex = uiState.selectedTab.ordinal,
                onTabSelected = { index -> onTabSelected(CourseHomeTab.entries[index]) },
                modifier = Modifier.fillMaxWidth(),
            )

            when (uiState.selectedTab) {
                CourseHomeTab.HOME -> CourseOverviewScreen(modifier = Modifier.fillMaxSize())
                CourseHomeTab.MODULES -> CourseModulesScreen(modifier = Modifier.fillMaxSize())
                CourseHomeTab.MY_WORK -> CourseMyWorkScreen(courseId = uiState.courseId, modifier = Modifier.fillMaxSize())
                CourseHomeTab.MORE -> CourseNavigationScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CourseImage(
    imageUrl: String?,
    courseColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(InstUILayoutSizes.Size.Interactive.height_lg)
            .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Md.md))
            .background(courseColor),
    ) {
        if (imageUrl != null) {
            GlideImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
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