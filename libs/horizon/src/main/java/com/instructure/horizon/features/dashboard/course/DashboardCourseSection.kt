/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.dashboard.course

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardContent
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardError
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardLoading
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardState
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.navigation.MainNavigationRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

@Composable
fun DashboardCourseSection(
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>
) {
    val viewModel = hiltViewModel<DashboardCourseViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshState.update { it + true }
            state.onRefresh {
                refreshState.update { it - true }
            }
        }
    }

    DashboardCourseSection(state, mainNavController, homeNavController)
}

@Composable
private fun DashboardCourseSection(
    state: DashboardCourseUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    when(state.state) {
        DashboardItemState.LOADING -> {
            DashboardCourseCardLoading(Modifier.padding(horizontal = 24.dp))
        }
        DashboardItemState.ERROR -> {
            DashboardCourseCardError({state.onRefresh {} }, Modifier.padding(horizontal = 24.dp))
        }
        DashboardItemState.SUCCESS -> {
            DashboardCourseSectionContent(state, mainNavController, homeNavController)
        }
    }
}

@Composable
private fun DashboardCourseSectionContent(
    state: DashboardCourseUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    val pagerstate = rememberPagerState { state.courses.size }

    Column {

        state.programs.forEach { programCardState ->
            DashboardCourseItem(
                programCardState,
                mainNavController,
                homeNavController,
                Modifier
                    .padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        HorizontalPager(
            pagerstate,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 4.dp,
        ) {

            var cardWidthList by remember { mutableStateOf(emptyMap<Int, Float>()) }
            val scaleAnimation by animateFloatAsState(
                if (it == pagerstate.currentPage) {
                    (1 - abs(pagerstate.currentPageOffsetFraction.convertScaleRange()))
                } else {
                    (1f - (0.2f * 2)) + (abs(pagerstate.currentPageOffsetFraction.convertScaleRange()))
                },
                label = "DashboardCourseCardAnimation",
            )
            val animationDirection = when {
                it < pagerstate.currentPage -> 1
                it > pagerstate.currentPage -> -1
                else -> if (pagerstate.currentPageOffsetFraction > 0) 1 else -1
            }
            DashboardCourseItem(
                state.courses[it],
                mainNavController,
                homeNavController,
                Modifier
                    .onGloballyPositioned { coordinates ->
                        cardWidthList = cardWidthList + (it to coordinates.size.width.toFloat())
                    }
                    .offset {
                        IntOffset(
                            (animationDirection * (((cardWidthList[it]
                                ?: 0f)) / 2 * (1 - scaleAnimation))).toInt(),
                            0
                        )
                    }
                    .scale(scaleAnimation)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(Modifier.height(8.dp))

        DashboardCourseCardIndicator(pagerstate)

        Spacer(Modifier.height(16.dp))
    }
}

private fun Float.convertScaleRange(): Float {
    val oldMin = -0.5f
    val oldMax = 0.5f
    val newMin = -0.2f
    val newMax = 0.2f
    return ((this - oldMin) / (oldMax - oldMin) ) * (newMax - newMin) + newMin
}

@Composable
private fun DashboardCourseItem(
    cardState: DashboardCourseCardState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    DashboardCourseCardContent(
        cardState, { handleClickAction(it, mainNavController, homeNavController) }, modifier
    )
}

@Composable
private fun DashboardCourseCardIndicator(pagerState: PagerState) {
    val selectedIndex = pagerState.currentPage
    val offset = pagerState.currentPageOffsetFraction

    var scrollToIndex: Int? by remember { mutableStateOf(null) }
    LaunchedEffect(scrollToIndex) {
        if (scrollToIndex != null) {
            pagerState.animateScrollToPage(scrollToIndex ?: return@LaunchedEffect)
            scrollToIndex = null
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(pagerState.pageCount) { itemIndex ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .padding(5.dp)
                    .border(1.dp, HorizonColors.Icon.medium(), CircleShape)
                    .clip(CircleShape)
                    .clickable { scrollToIndex = itemIndex }
            ) {
                if (itemIndex == selectedIndex) {
                    Box(
                        modifier = Modifier
                            .size(10.dp * (1 - abs(offset)))
                            .clip(CircleShape)
                            .background(HorizonColors.Icon.medium())
                    )
                } else if (itemIndex == selectedIndex + (1 * if (offset > 0) 1 else -1)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp * (abs(offset)))
                            .clip(CircleShape)
                            .background(HorizonColors.Icon.medium())
                    )
                }
            }
        }
    }
}

private fun handleClickAction(
    action: CardClickAction?,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    when(action) {
        is CardClickAction.Action -> {
            action.onClick()
        }
        is CardClickAction.NavigateToCourse -> {
            homeNavController.navigate(HomeNavigationRoute.Learn.withCourse(action.courseId)) {
                popUpTo(homeNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = false
            }
        }
        is CardClickAction.NavigateToModuleItem -> {
            mainNavController.navigate(
                MainNavigationRoute.ModuleItemSequence(
                    action.courseId,
                    action.moduleItemId
                )
            )
        }
        is CardClickAction.NavigateToProgram -> {
            homeNavController.navigate(HomeNavigationRoute.Learn.withProgram(action.programId)) {
                popUpTo(homeNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = false
            }
        }
        else -> {}
    }
}