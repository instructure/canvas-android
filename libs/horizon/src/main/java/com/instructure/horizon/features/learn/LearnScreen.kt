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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.note.LearnNotesScreen
import com.instructure.horizon.features.learn.overview.LearnOverviewScreen
import com.instructure.horizon.features.learn.progress.LearnProgressScreen
import com.instructure.horizon.features.learn.score.LearnScoreScreen
import com.instructure.horizon.horizonui.HorizonTheme
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.organisms.tabrow.TabRow
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(viewModel: LearnViewModel) {
    val state by viewModel.state.collectAsState()
    HorizonTheme {
        Scaffold(
            containerColor = HorizonColors.Surface.pagePrimary(),
        ) { padding ->
            LoadingStateWrapper(state.screenState, Modifier.padding(padding)) {
                LearnScreenWrapper(state, Modifier.fillMaxSize())
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
        ) { index ->
            when (index) {
                0 -> LearnProgressScreen()
                1 -> LearnOverviewScreen(state.course?.course)
                2 -> LearnScoreScreen()
                3 -> LearnNotesScreen()
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