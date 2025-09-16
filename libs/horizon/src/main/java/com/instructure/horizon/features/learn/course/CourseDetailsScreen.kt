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
package com.instructure.horizon.features.learn.course

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.features.learn.course.lti.CourseToolsScreen
import com.instructure.horizon.features.learn.course.note.CourseNotesScreen
import com.instructure.horizon.features.learn.course.overview.CourseOverviewScreen
import com.instructure.horizon.features.learn.course.progress.CourseProgressScreen
import com.instructure.horizon.features.learn.course.score.CourseScoreScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.organisms.tabrow.TabRow
import kotlinx.coroutines.launch

@Composable
fun CourseDetailsScreen(
    state: CourseDetailsUiState,
    mainNavController: NavHostController
) {
    val pagerState = rememberPagerState(initialPage = 0) { state.availableTabs.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ProgressBar(
                progress = state.selectedCourse?.progress ?: 0.0,
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp)
            )
            TabRow(
                tabs = state.availableTabs,
                selectedIndex = pagerState.currentPage,
                onTabSelected = { coroutineScope.launch { pagerState.animateScrollToPage(it) } },
                tab = { tab, isSelected, modifier -> Tab(tab, isSelected, modifier) },
                tabAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            )
            HorizontalPager(
                pagerState,
                pageSpacing = 16.dp,
            ) { index ->
                val scaleAnimation by animateFloatAsState(
                    if (index == pagerState.currentPage) 1f else 0.8f,
                    label = "SelectedTabAnimation"
                )
                val cornerAnimation by animateDpAsState(
                    if (index == pagerState.currentPage) {
                        0.dp
                    } else {
                        if (index == 2) 16.dp else 32.dp
                    },
                    label = "SelectedTabCornerAnimation"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scaleAnimation)
                ) {
                    when (index) {
                        0 -> CourseOverviewScreen(
                            state.selectedCourse?.courseSyllabus,
                            Modifier
                                .clip(RoundedCornerShape(cornerAnimation))
                        )

                        1 -> CourseProgressScreen(
                            state.selectedCourse?.courseId ?: -1,
                            mainNavController,
                            Modifier.clip(RoundedCornerShape(cornerAnimation))
                        )

                        2 -> CourseScoreScreen(
                            state.selectedCourse?.courseId ?: -1,
                            mainNavController,
                            Modifier.clip(RoundedCornerShape(cornerAnimation))
                        )

                        3 -> CourseNotesScreen(
                            state.selectedCourse?.courseId ?: -1,
                            mainNavController,
                            Modifier.clip(RoundedCornerShape(cornerAnimation))
                        )

                        4 -> CourseToolsScreen(
                            state.selectedCourse?.courseId ?: -1,
                            Modifier.clip(RoundedCornerShape(cornerAnimation))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Tab(tab: CourseDetailsTab, isSelected: Boolean, modifier: Modifier = Modifier) {
    val color = if (isSelected) {
        HorizonColors.Text.surfaceInverseSecondary()
    } else {
        HorizonColors.Text.body()
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text(
            stringResource(tab.titleRes),
            style = HorizonTypography.p1,
            color = color,
            modifier = Modifier
                .padding(top = 20.dp)
        )
    }
}