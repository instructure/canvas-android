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
package com.instructure.horizon.features.learn.course.details

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.course.details.lti.CourseToolsScreen
import com.instructure.horizon.features.learn.course.details.note.CourseNotesScreen
import com.instructure.horizon.features.learn.course.details.overview.CourseOverviewScreen
import com.instructure.horizon.features.learn.course.details.progress.CourseProgressScreen
import com.instructure.horizon.features.learn.course.details.score.CourseScoreScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle
import com.instructure.horizon.horizonui.organisms.scaffolds.CollapsableHeaderScreen
import com.instructure.horizon.horizonui.organisms.tabrow.TabRow
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.horizonui.selectable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    state: CourseDetailsUiState,
    navController: NavHostController
) {
    val pagerState = rememberPagerState(initialPage = 0) { state.availableTabs.size }
    val coroutineScope = rememberCoroutineScope()

    LoadingStateWrapper(state.loadingState){
        CollapsableHeaderScreen(
            statusBarColor = HorizonColors.Surface.pagePrimary(),
            headerContent = { paddingValues ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            iconRes = R.drawable.arrow_back,
                            contentDescription = stringResource(R.string.a11yNavigateBack),
                            color = IconButtonColor.Ghost,
                            size = IconButtonSize.SMALL,
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = state.courseName,
                            style = HorizonTypography.h3,
                            color = HorizonColors.Text.title()
                        )
                    }
                    HorizonSpace(SpaceSize.SPACE_16)
                    CourseProgress(state.courseProgress)
                }
            },
            bodyContent = { paddingValues ->
                Column(Modifier.padding(paddingValues)) {
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
                                    state.courseSyllabus,
                                    state.parentPrograms.map { it.name },
                                    Modifier
                                        .clip(RoundedCornerShape(cornerAnimation))
                                )

                                1 -> CourseProgressScreen(
                                    state.courseId,
                                    navController,
                                    Modifier.clip(RoundedCornerShape(cornerAnimation))
                                )

                                2 -> CourseScoreScreen(
                                    state.courseId,
                                    navController,
                                    Modifier.clip(RoundedCornerShape(cornerAnimation))
                                )

                                3 -> CourseNotesScreen(
                                    state.courseId,
                                    navController,
                                    Modifier.clip(RoundedCornerShape(cornerAnimation))
                                )

                                4 -> CourseToolsScreen(
                                    state.courseId,
                                    Modifier.clip(RoundedCornerShape(cornerAnimation))
                                )
                            }
                        }
                    }
                }
            }
        )
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
            .padding(bottom = 2.dp)
    ) {
        val context = LocalContext.current
        Text(
            stringResource(tab.titleRes),
            style = HorizonTypography.p1,
            color = color,
            modifier = Modifier
                .padding(top = 20.dp)
                .semantics {
                    role = Role.Tab
                    selectable(context, isSelected)
                }
        )
    }
}

@Composable
private fun CourseProgress(progress: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressBarSmall(
            progress = progress,
            style = ProgressBarStyle.Institution,
            showLabels = false,
            modifier = Modifier.weight(1f)
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            text = stringResource(R.string.progressBar_percent, progress.roundToInt()),
            style = HorizonTypography.p2,
            color = HorizonColors.Surface.institution(),
        )
    }
}