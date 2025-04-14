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
package com.instructure.horizon.features.learn

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.note.LearnNotesScreen
import com.instructure.horizon.features.learn.overview.LearnOverviewScreen
import com.instructure.horizon.features.learn.progress.LearnProgressScreen
import com.instructure.horizon.features.learn.score.LearnScoreScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.tabrow.TabRow
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.compose.composables.ErrorContent
import kotlinx.coroutines.launch

@Composable
fun LearnScreen(state: LearnUiState) {
    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.screenState.isError -> ErrorContent(state.screenState.errorMessage.orEmpty())
                state.screenState.isLoading -> LoadingContent()
                else -> LearnScreenWrapper(state, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun LearnScreenWrapper(state: LearnUiState, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(initialPage = 0) { state.availableTabs.size }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = state.course?.course?.name ?: stringResource(R.string.course),
            style = HorizonTypography.h3,
            color = HorizonColors.Text.title(),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 24.dp)
        )

        ProgressBar(
            progress = state.course?.progress ?: 0.0,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )

        TabRow(
            tabs = state.availableTabs,
            selectedIndex = pagerState.currentPage,
            onTabSelected = { coroutineScope.launch { pagerState.animateScrollToPage(it) } },
            tab = { tab, isSelected, modifier -> Tab(tab, isSelected, modifier) },
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        )
        HorizontalPager(
            pagerState,
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 8.dp),
            beyondViewportPageCount = 4
        ) { index ->
            val scaleAnimation by animateFloatAsState(if (index == pagerState.currentPage) 1f else 0.8f, label = "SelectedTabAnimation")
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
                    0 -> LearnOverviewScreen(
                        state.course?.course?.syllabusBody,
                        Modifier
                            .clip(RoundedCornerShape(cornerAnimation))
                    )
                    1 -> LearnProgressScreen(Modifier.clip(RoundedCornerShape(cornerAnimation)))
                    2 -> LearnScoreScreen(state.course?.course?.id ?: -1, Modifier.clip(RoundedCornerShape(cornerAnimation)))
                    3 -> LearnNotesScreen(Modifier.clip(RoundedCornerShape(cornerAnimation)))
                }
            }
        }
    }
}

@Composable
private fun Tab(tab: LearnTab, isSelected: Boolean, modifier: Modifier = Modifier) {
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

@Composable
private fun LoadingContent() {
    Spinner()
}

@Composable
private fun ErrorContent(errorText: String) {
    Text(text = errorText, style = HorizonTypography.h3)
}

@Composable
@Preview
fun LearnScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(isLoading = true),
        course = null,
        availableTabs = LearnTab.entries
    )
    LearnScreen(state)
}

@Composable
@Preview
fun LearnScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(isError = true, errorMessage = "Error loading course"),
        course = null,
        availableTabs = LearnTab.entries
    )
    LearnScreen(state)
}

@Composable
@Preview
fun LearnScreenContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(),
        course = CourseWithProgress(
            course = Course(
                id = 123,
                name = "Course Name",
                syllabusBody = "Course Overview",
            ),
            progress = 0.5,
            nextUpModuleItemId = null,
            nextUpModuleId = null,
        ),
        availableTabs = LearnTab.entries
    )
    LearnScreen(state)
}