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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.note.LearnNotesScreen
import com.instructure.horizon.features.learn.overview.LearnOverviewScreen
import com.instructure.horizon.features.learn.progress.LearnProgressScreen
import com.instructure.horizon.features.learn.score.LearnScoreScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.inputs.common.InputDropDownPopup
import com.instructure.horizon.horizonui.organisms.tabrow.TabRow
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull
import kotlinx.coroutines.launch

@Composable
fun LearnScreen(
    state: LearnUiState,
    mainNavController: NavHostController
) {
    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(Unit) {
        if (activity != null) ViewStyler.setStatusBarColor(activity, ContextCompat.getColor(activity, R.color.surface_pagePrimary))
    }

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
                else -> LearnScreenWrapper(state, mainNavController, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun LearnScreenWrapper(
    state: LearnUiState,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0) { state.availableTabs.size }
    val coroutineScope = rememberCoroutineScope()
    var appBarHeight by rememberSaveable { mutableIntStateOf(0) }
    var appBarOffset by rememberSaveable { mutableIntStateOf(0) }
    var nestedScrollConnection by remember { mutableStateOf(CollapsingAppBarNestedScrollConnection(appBarHeight, appBarOffset)) }
    LaunchedEffect(nestedScrollConnection.appBarOffset) {
        appBarOffset = nestedScrollConnection.appBarOffset
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(0, nestedScrollConnection.appBarOffset) }
                .onGloballyPositioned { coordinates ->
                    if (appBarHeight == 0) {
                        appBarHeight = coordinates.size.height
                        nestedScrollConnection =
                            CollapsingAppBarNestedScrollConnection(appBarHeight, appBarOffset)
                    }
                }
        ) {
            DropDownTitle(
                courses = state.courses,
                selectedCourse = state.selectedCourse ?: CourseWithProgress(
                    courseId = -1, courseName = "", courseSyllabus = "", progress = 0.0
                ),
                onSelect = { state.onSelectedCourseChanged(it) },
            )

            ProgressBar(
                progress = state.selectedCourse?.progress ?: 0.0,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
        val density = LocalDensity.current
        Column(
            modifier = Modifier
                .padding(top = with(density) { appBarHeight.toDp() } + with(density) { appBarOffset.toDp() })
        ) {
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
                        0 -> LearnOverviewScreen(
                            state.selectedCourse?.courseSyllabus,
                            Modifier
                                .clip(RoundedCornerShape(cornerAnimation))
                        )

                        1 -> LearnProgressScreen(
                            state.selectedCourse?.courseId ?: -1,
                            mainNavController,
                            Modifier.clip(RoundedCornerShape(cornerAnimation))
                        )

                        2 -> LearnScoreScreen(
                            state.selectedCourse?.courseId ?: -1,
                            mainNavController,
                            Modifier.clip(RoundedCornerShape(cornerAnimation))
                        )

                        3 -> LearnNotesScreen(
                            state.selectedCourse?.courseId ?: -1,
                            mainNavController,
                            Modifier.clip(RoundedCornerShape(cornerAnimation))
                        )
                    }
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
private fun DropDownTitle(courses: List<CourseWithProgress>, selectedCourse: CourseWithProgress, onSelect: (CourseWithProgress) -> Unit) {
    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 24.dp)
    ) {
        val showDropDown = courses.size > 1

        val localDensity = LocalDensity.current
        var heightInPx by remember { mutableIntStateOf(0) }
        var width by remember { mutableStateOf(0.dp) }
        var isMenuOpen by remember { mutableStateOf(false) }
        val iconRotation by animateFloatAsState(
            if (isMenuOpen) 180f else 0f,
            label = "DropDownIconRotation"
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    heightInPx = it.size.height
                    width = with(localDensity) { it.size.width.toDp() }
                }
                .clickable(enabled = showDropDown) { isMenuOpen = !isMenuOpen }
        ) {
            AnimatedContent(
                selectedCourse.courseName,
                label = "SelectedCourseName",
                modifier = Modifier
                    .weight(1f, fill = false)
            ) { courseName ->
                Text(
                    text = courseName,
                    style = HorizonTypography.h3,
                    color = HorizonColors.Text.title(),
                )
            }

            HorizonSpace(SpaceSize.SPACE_8)

            if (showDropDown) {
                Icon(
                    painter = painterResource(R.drawable.keyboard_arrow_down),
                    contentDescription = null,
                    tint = HorizonColors.Icon.default(),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(iconRotation)
                )
            }
        }

        InputDropDownPopup(
            isMenuOpen = isMenuOpen,
            options = courses.map { it.courseName },
            width = width,
            verticalOffsetPx = heightInPx,
            onMenuOpenChanged = { isMenuOpen = it },
            onOptionSelected = { selectedCourse ->
                onSelect(courses.first { it.courseName == selectedCourse })
            },
            item = { courseName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    if (courseName == selectedCourse.courseName) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = stringResource(R.string.a11y_selectedCourse),
                            tint = HorizonColors.Icon.default(),
                            modifier = Modifier
                                .size(18.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(18.dp))
                    }

                    HorizonSpace(SpaceSize.SPACE_8)

                    Text(
                        text = courseName,
                        style = HorizonTypography.p1,
                        color = HorizonColors.Text.body(),
                    )
                }
            },
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

private class CollapsingAppBarNestedScrollConnection(
    val appBarMaxHeight: Int,
    initialAppbarOffset: Int = 0,
) : NestedScrollConnection {

    var appBarOffset: Int by mutableIntStateOf(initialAppbarOffset)
        private set

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y.toInt()
        val newOffset = appBarOffset + delta
        val previousOffset = appBarOffset
        appBarOffset = newOffset.coerceIn(-appBarMaxHeight, 0)
        val consumed = appBarOffset - previousOffset
        return Offset(0f, consumed.toFloat())
    }
}

@Composable
@Preview
fun LearnScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(isLoading = true),
        selectedCourse = null,
        availableTabs = LearnTab.entries
    )
    LearnScreen(state, rememberNavController())
}

@Composable
@Preview
fun LearnScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(isError = true, errorMessage = "Error loading course"),
        selectedCourse = null,
        availableTabs = LearnTab.entries
    )
    LearnScreen(state, rememberNavController())
}

@Composable
@Preview
fun LearnScreenContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(),
        selectedCourse = CourseWithProgress(
            courseId = 123,
            courseName = "Course Name",
            courseSyllabus = "Course Overview",
            progress = 0.5,
        ),
        availableTabs = LearnTab.entries
    )
    LearnScreen(state, rememberNavController())
}